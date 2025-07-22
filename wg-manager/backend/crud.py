from sqlalchemy.orm import Session
from . import models, schemas

def get_client(db: Session, client_id: int):
    return db.query(models.ClientConfig).filter(models.ClientConfig.id == client_id).first()

def get_client_by_name(db: Session, name: str):
    return db.query(models.ClientConfig).filter(models.ClientConfig.name == name).first()

def get_clients(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.ClientConfig).offset(skip).limit(limit).all()

def create_client(db: Session, client: schemas.ClientCreate, public_key: str, preshared_key: str, private_key: str):
    db_client = models.ClientConfig(
        name=client.name,
        public_key=public_key,
        preshared_key=preshared_key,
        private_key=private_key, # This should be encrypted before storing
        allowed_ips=client.allowed_ips
    )
    db.add(db_client)
    db.commit()
    db.refresh(db_client)
    return db_client

def update_client_status(db: Session, client_id: int, is_active: bool) -> models.ClientConfig:
    db_client = get_client(db, client_id=client_id)
    if not db_client:
        return None
    db_client.is_active = is_active
    db.commit()
    db.refresh(db_client)
    return db_client
