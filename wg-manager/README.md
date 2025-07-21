# WireGuard Manager with YouTube Tunneling

This project provides a modern, web-based management interface for a WireGuard VPN server. It is specifically configured to tunnel traffic through YouTube's IP ranges, allowing users with unlimited YouTube data plans to leverage them for general VPN usage.

The application features a FastAPI backend, a Next.js (React) frontend, and is designed for easy deployment on a Virtual Dedicated Server (VDS) using an automated installation script.

## Features

- **Modern Web UI:** Manage WireGuard clients through a clean and responsive web interface.
- **Dynamic Configuration:** Automatically generates and applies WireGuard server configurations (`wg0.conf`) when clients are added or removed.
- **YouTube IP Tunneling:** Comes pre-configured with a list of Google/YouTube IP ranges to be used in the `AllowedIPs` configuration for each client.
- **Secure Authentication:** The web interface is protected by a username/password login system using JWT (JSON Web Tokens).
- **Automated Installation:** A single `install.sh` script handles all dependencies, setup, and service configuration on a fresh Debian-based server.
- **QR Code Support (Future):** The groundwork is laid for generating QR codes for easy client setup on mobile devices.

## Technology Stack

- **Backend:** FastAPI (Python)
- **Frontend:** Next.js (React) & Tailwind CSS
- **Database:** SQLite
- **Web Server:** Nginx (as a reverse proxy)
- **Process Manager:** systemd

## Installation

These instructions are for a fresh Debian or Ubuntu-based server. You will need `root` access.

### 1. Clone the Repository

First, connect to your server via SSH. Then, clone this repository. You will need `git` installed (`apt-get install git`).

```bash
git clone <repository_url>
cd wg-manager
```

### 2. Run the Installation Script

The provided script will automate the entire setup process. It will:
- Update your system and install necessary packages (`python3`, `npm`, `nginx`, `wireguard`).
- Set up the project files in `/opt/wg-manager`.
- Install backend and frontend dependencies.
- Prompt you to create a secure admin username and password for the web UI.
- Configure and start all necessary services (`nginx`, `wireguard`, and the backend application).

To run the script, navigate to the `scripts` directory and execute it with `root` privileges:

```bash
cd scripts
sudo bash install.sh
```

Follow the on-screen prompts to set your admin credentials. The script will handle the rest.

### 3. Access the Web Interface

Once the installation is complete, you can access the web interface by navigating to your server's IP address in a web browser:

`http://<your_vds_ip>`

Log in with the username and password you created during the installation.

## Usage Guide

## Usage Guide

### Dashboard Overview

After logging in, you will be greeted with a dashboard that lists all configured WireGuard clients. The table provides real-time information about each client:

- **Status:** A colored dot indicates the connection status.
    - **Green:** The client is connected (had a handshake within the last 3 minutes).
    - **Red:** The client is currently disconnected.
    - The text indicates if the client is `Enabled` or `Disabled` in the configuration.
- **Name:** The name you assigned to the client.
- **Endpoint IP:** The actual IP address the client is connecting from.
- **Data Transfer (DL / UL):** Shows the total data downloaded and uploaded by the client since the WireGuard interface was last started.
- **Latest Handshake:** How long ago the client last communicated with the server. `Never` indicates no connection has been made.

### Adding a New Client

1.  Log in to the web interface.
2.  *(Functionality to be added)* Click the "Add New Client" button.
3.  Enter a name for the new client (e.g., `my-phone`) and click "Create".

The backend will automatically generate keys, update the WireGuard configuration, and reload the service. The new client will appear in the dashboard.

### Configuring Your Device

*(Functionality to be added)* After adding a client, you will be able to click on it to view its configuration file (`.conf`) and a QR code for easy setup on mobile devices.

## Project Structure

- **/backend:** Contains the FastAPI application, including API endpoints, database models, and WireGuard management logic.
- **/frontend:** Contains the Next.js application for the web interface.
- **/scripts:** Holds the `install.sh` script for automated deployment.
- **README.md:** This file.
