import subprocess
from typing import List
from . import models
from .config import settings

def generate_keys():
    """Generates a new WireGuard private and public key pair."""
    try:
        private_key = subprocess.run(
            ["wg", "genkey"], capture_output=True, text=True, check=True
        ).stdout.strip()
        public_key = subprocess.run(
            ["wg", "pubkey"], input=private_key, capture_output=True, text=True, check=True
        ).stdout.strip()
        return private_key, public_key
    except (subprocess.CalledProcessError, FileNotFoundError) as e:
        print(f"Error generating WireGuard keys: {e}")
        raise SystemExit("WireGuard tools not found or failed to execute.")

from cachetools import cached, TTLCache

# Cache the IP list for 1 hour to avoid frequent file reads
@cached(cache=TTLCache(maxsize=1, ttl=3600))
def get_youtube_ips() -> List[str]:
    """Reads YouTube IP ranges from the specified file, with caching."""
    print("Reading YouTube IPs from file (cache miss)...")
    try:
        filepath = os.path.join(os.path.dirname(__file__), settings.YOUTUBE_IPS_FILE)
        with open(filepath, 'r') as f:
            # Read lines, strip whitespace, and filter out empty lines or comments
            return [line.strip() for line in f if line.strip() and not line.startswith('#')]
    except FileNotFoundError:
        print(f"Warning: YouTube IPs file not found at '{filepath}'. Proceeding without it.")
        return []

def generate_wireguard_config(clients: List[models.ClientConfig], server_private_key: str) -> str:
    """Generates the full wg0.conf file content."""

    # [Interface] section
    config_lines = [
        "[Interface]",
        f"Address = {settings.WG_SERVER_ADDRESS}",
        f"ListenPort = {settings.WG_SERVER_LISTEN_PORT}",
        f"PrivateKey = {server_private_key}",
        "PostUp = iptables -A FORWARD -i %i -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE",
        "PostDown = iptables -D FORWARD -i %i -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE",
        ""
    ]

    # [Peer] sections for each client
    youtube_ips_str = ", ".join(get_youtube_ips())

    for client in clients:
        if client.is_active:
            config_lines.append("[Peer]")
            config_lines.append(f"PublicKey = {client.public_key}")
            if client.preshared_key:
                config_lines.append(f"PresharedKey = {client.preshared_key}")

            # Combine client-specific IPs with YouTube IPs
            allowed_ips = f"{client.allowed_ips}, {youtube_ips_str}" if youtube_ips_str else client.allowed_ips
            config_lines.append(f"AllowedIPs = {allowed_ips}")
            config_lines.append("")

    return "\n".join(config_lines)

def get_peer_statuses() -> dict:
    """
    Executes 'wg show wg0 dump' and parses the output to get peer statuses.
    Returns a dictionary mapping public keys to their status info.
    """
    statuses = {}
    try:
        # The 'dump' command is better for parsing than the default output
        result = subprocess.run(
            ["wg", "show", "wg0", "dump"],
            capture_output=True,
            text=True,
            check=True
        ).stdout.strip()

        lines = result.split('\n')
        # The first line is the server's own info, we skip it
        for line in lines[1:]:
            parts = line.split('\t')
            if len(parts) >= 5:
                public_key = parts[0]
                preshared_key = parts[1]
                endpoint = parts[2]
                latest_handshake = int(parts[3])
                transfer_rx = int(parts[4])
                transfer_tx = int(parts[5])

                statuses[public_key] = {
                    "preshared_key": preshared_key,
                    "endpoint": endpoint,
                    "latest_handshake": datetime.fromtimestamp(latest_handshake),
                    "transfer_rx": transfer_rx,
                    "transfer_tx": transfer_tx,
                }
    except (subprocess.CalledProcessError, FileNotFoundError, IndexError) as e:
        print(f"Could not get peer statuses: {e}. This might be normal if no peers are connected.")
        return {} # Return empty dict on error
    return statuses


def apply_wireguard_config(config_content: str):
    """Writes the configuration to the file and reloads WireGuard."""
    try:
        # Write the config file
        with open(settings.WG_CONF_PATH, 'w') as f:
            f.write(config_content)

        # Use wg syncconf for faster, more reliable config reloads
        subprocess.run(["wg", "syncconf", "wg0", settings.WG_CONF_PATH], check=True)

        print("WireGuard configuration applied successfully.")

    except (IOError, subprocess.CalledProcessError) as e:
        print(f"Error applying WireGuard configuration: {e}")
        raise
