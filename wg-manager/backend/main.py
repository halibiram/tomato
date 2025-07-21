from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="WireGuard Manager API",
    description="API for managing a WireGuard server and its clients.",
    version="0.1.0",
)

# CORS middleware for allowing frontend to communicate with a different origin
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, you should restrict this to your frontend's domain
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


import asyncio
from fastapi import WebSocket, WebSocketDisconnect
from . import api
from .database import SessionLocal
from .api import read_clients_with_status, get_current_user # Re-use the logic

# Include the routers with their prefixes
app.include_router(api.auth_router, prefix="/api/v1/auth", tags=["Authentication"])
app.include_router(api.client_router, prefix="/api/v1/clients", tags=["Clients"])

class ConnectionManager:
    def __init__(self):
        self.active_connections: list[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)

    def disconnect(self, websocket: WebSocket):
        self.active_connections.remove(websocket)

    async def broadcast(self, message: str):
        for connection in self.active_connections:
            await connection.send_text(message)

manager = ConnectionManager()

async def broadcast_updates():
    """Periodically fetches and broadcasts client statuses."""
    while True:
        await asyncio.sleep(2) # Broadcast every 2 seconds
        db = SessionLocal()
        try:
            # We can't use Depends in a non-endpoint function, so we call the logic directly
            # This is a simplified approach; a more robust solution would involve a background task system
            # and dependency injection for non-request scopes.
            # For now, we'll just re-fetch the data. This part of the code assumes no auth for the websocket feed.
            clients = read_clients_with_status(db=db, current_user=None) # Bypassing auth for broadcast
            # Pydantic models need to be serialized to be sent over JSON
            client_list = [client.dict() for client in clients]
            await manager.broadcast(json.dumps(client_list, default=str))
        finally:
            db.close()

@app.on_event("startup")
async def startup_event():
    # Start the broadcasting task in the background
    asyncio.create_task(broadcast_updates())

@app.websocket("/ws/client-status")
async def websocket_endpoint(websocket: WebSocket):
    await manager.connect(websocket)
    try:
        while True:
            # Keep the connection alive
            await websocket.receive_text()
    except WebSocketDisconnect:
        manager.disconnect(websocket)

import json

@app.get("/")
async def root():
    """
    Root endpoint to check if the API is running.
    """
    return {"message": "Welcome to WireGuard Manager API"}
