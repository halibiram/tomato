from sqlalchemy import Column, Integer, String, Boolean, DateTime
from sqlalchemy.sql import func
from .database import Base

class ClientConfig(Base):
    __tablename__ = "client_configs"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True, nullable=False)
    public_key = Column(String, unique=True, nullable=False)
    preshared_key = Column(String, unique=True, nullable=False)
    private_key = Column(String, unique=True, nullable=False) # Stored encrypted
    allowed_ips = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
