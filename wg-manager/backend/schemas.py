from pydantic import BaseModel, Field
from datetime import datetime

class ClientBase(BaseModel):
    name: str = Field(..., description="Name of the client")
    allowed_ips: str = Field("0.0.0.0/0, ::/0", description="Allowed IPs for the client")

class ClientCreate(ClientBase):
    pass

from typing import Optional

class Client(ClientBase):
    id: int
    public_key: str
    preshared_key: str
    created_at: datetime
    updated_at: Optional[datetime] = None
    is_active: bool

    # Dynamic data from 'wg' command
    latest_handshake_at: Optional[datetime] = None
    transfer_rx: int = 0
    transfer_tx: int = 0
    endpoint_ip: Optional[str] = None
    is_connected: bool = False
    is_streaming: bool = False

    class Config:
        from_attributes = True

class ServerStatus(BaseModel):
    is_running: bool
    public_key: str
    listen_port: int
    total_clients: int
    active_clients: int

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    username: Optional[str] = None

class User(BaseModel):
    username: str
