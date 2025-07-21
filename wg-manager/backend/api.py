from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from typing import List
import subprocess

from . import crud, models, schemas, security
from .database import SessionLocal, engine, get_db
from .wg_utils import generate_keys, generate_wireguard_config, apply_wireguard_config
from .config import settings

models.Base.metadata.create_all(bind=engine)

# --- Authentication Setup ---
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="api/v1/auth/token")

# --- Routers ---
auth_router = APIRouter()
client_router = APIRouter()


# --- Helper Functions for Auth ---
def authenticate_user(username: str, password: str) -> bool:
    """Authenticate the admin user."""
    if username == settings.ADMIN_USERNAME and security.verify_password(password, settings.ADMIN_PASSWORD_HASH):
        return True
    return False

async def get_current_user(token: str = Depends(oauth2_scheme)):
    """Dependency to get the current user from a token."""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    payload = security.decode_access_token(token)
    if payload is None:
        raise credentials_exception
    username: str = payload.get("sub")
    if username is None or username != settings.ADMIN_USERNAME:
        raise credentials_exception
    return schemas.User(username=username)


# --- Auth Endpoints ---
@auth_router.post("/token", response_model=schemas.Token)
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends()):
    user_authenticated = authenticate_user(form_data.username, form_data.password)
    if not user_authenticated:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token = security.create_access_token(data={"sub": form_data.username})
    return {"access_token": access_token, "token_type": "bearer"}


# --- Client Endpoints (Now Protected) ---
from .wg_utils import get_peer_statuses
from datetime import datetime, timedelta

@client_router.get("/", response_model=List[schemas.Client])
def read_clients_with_status(
    skip: int = 0, limit: int = 100, db: Session = Depends(get_db), current_user: schemas.User = Depends(get_current_user)
):
    db_clients = crud.get_clients(db, skip=skip, limit=limit)
    live_statuses = get_peer_statuses()

    enriched_clients = []
    for db_client in db_clients:
        client_data = schemas.Client.from_orm(db_client)

        live_info = live_statuses.get(db_client.public_key)
        if live_info:
            client_data.latest_handshake_at = live_info.get("latest_handshake")
            client_data.transfer_rx = live_info.get("transfer_rx", 0)
            client_data.transfer_tx = live_info.get("transfer_tx", 0)
            client_data.endpoint_ip = live_info.get("endpoint", "N/A").split(':')[0]

            # Check if connected (handshake in the last 3 minutes)
            if client_data.latest_handshake_at:
                handshake_delta = datetime.utcnow() - client_data.latest_handshake_at
                if handshake_delta < timedelta(minutes=3):
                    client_data.is_connected = True

                # Estimate if streaming (naive approach)
                # If there's been significant data transfer since the last handshake,
                # we can assume some activity. A true speed test is more complex.
                # Let's define "streaming" as > 1MB of download since last handshake.
                if client_data.transfer_rx > 1_000_000 and handshake_delta < timedelta(seconds=60):
                     client_data.is_streaming = True

        enriched_clients.append(client_data)

    return enriched_clients

@client_router.post("/", response_model=schemas.Client)
def create_client(
    client: schemas.ClientCreate, db: Session = Depends(get_db), current_user: schemas.User = Depends(get_current_user)
):
    db_client = crud.get_client_by_name(db, name=client.name)
    if db_client:
        raise HTTPException(status_code=400, detail="Client name already registered")

    private_key, public_key = generate_keys()
    preshared_key = subprocess.run(["wg", "genpsk"], capture_output=True, text=True, check=True).stdout.strip()

    new_client = crud.create_client(
        db=db,
        client=client,
        public_key=public_key,
        preshared_key=preshared_key,
        private_key=private_key
    )

    # Regenerate and apply config
    all_clients = crud.get_clients(db)
    with open(settings.WG_SERVER_PRIVATE_KEY_PATH, 'r') as f:
        server_private_key = f.read().strip()

    config_content = generate_wireguard_config(all_clients, server_private_key)

    try:
        apply_wireguard_config(config_content)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Client created, but failed to apply WireGuard config: {e}")

    return new_client
