from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    WG_CONF_PATH: str = "/etc/wireguard/wg0.conf"
    WG_SERVER_PRIVATE_KEY_PATH: str = "/etc/wireguard/server_private_key"
    WG_SERVER_PUBLIC_KEY_PATH: str = "/etc/wireguard/server_public_key"
    WG_SERVER_LISTEN_PORT: int = 51820
    WG_SERVER_ADDRESS: str = "10.0.0.1/24"
    YOUTUBE_IPS_FILE: str = "youtube_ips.txt"

    # --- Security Settings ---
    SECRET_KEY: str = "a_very_secret_key_that_should_be_changed"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60

    # --- Admin User Settings ---
    ADMIN_USERNAME: str
    ADMIN_PASSWORD_HASH: str

    class Config:
        env_file = ".env"
        # In a real app, you'd want to use a .env file to populate these
        # For example:
        # ADMIN_USERNAME=admin
        # ADMIN_PASSWORD_HASH=...

settings = Settings()
