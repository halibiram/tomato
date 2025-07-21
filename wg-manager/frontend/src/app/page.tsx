"use client";

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Cookies from 'js-cookie';
import api from '../lib/axios'; // Use our configured axios instance

// Helper functions for formatting
function formatBytes(bytes: number, decimals = 2) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

function timeAgo(date: string | null): string {
    if (!date) return 'Never';
    const seconds = Math.floor((new Date().getTime() - new Date(date).getTime()) / 1000);
    let interval = seconds / 31536000;
    if (interval > 1) return Math.floor(interval) + " years ago";
    interval = seconds / 2592000;
    if (interval > 1) return Math.floor(interval) + " months ago";
    interval = seconds / 86400;
    if (interval > 1) return Math.floor(interval) + " days ago";
    interval = seconds / 3600;
    if (interval > 1) return Math.floor(interval) + " hours ago";
    interval = seconds / 60;
    if (interval > 1) return Math.floor(interval) + " minutes ago";
    return Math.floor(seconds) + " seconds ago";
}

// Updated client type
interface Client {
  id: number;
  name: string;
  public_key: string;
  is_active: boolean;
  is_connected: boolean;
  latest_handshake_at: string | null;
  transfer_rx: number;
  transfer_tx: number;
  endpoint_ip: string | null;
}

export default function Home() {
  const [clients, setClients] = useState<Client[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    const token = Cookies.get('access_token');
    if (!token) {
      router.push('/login');
      return;
    }

    setLoading(true);

    // Determine WebSocket protocol
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${wsProtocol}//${window.location.host}/ws/client-status`;

    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      console.log('WebSocket connection established');
      setLoading(false); // Connection is open, we can stop loading
      setError(null);
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        setClients(data);
      } catch (e) {
        console.error('Failed to parse WebSocket message:', e);
      }
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      setError('WebSocket connection failed. Is the backend running?');
      setLoading(false);
    };

    ws.onclose = () => {
      console.log('WebSocket connection closed');
      // Optional: attempt to reconnect
    };

    // Clean up the connection when the component unmounts
    return () => {
      ws.close();
    };
  }, [router]);

  // Show loading indicator until the first message is received or connection fails
  if (loading) {
    return <div className="flex min-h-screen items-center justify-center">Connecting to server...</div>;
  }

  return (
    <main className="flex min-h-screen flex-col items-center p-12">
      <div className="z-10 w-full max-w-5xl items-center justify-between font-mono text-sm lg:flex">
        <h1 className="text-4xl font-bold mb-8">WireGuard Manager</h1>
      </div>

      <div className="w-full max-w-5xl">
        <h2 className="text-2xl mb-4">Clients</h2>
        {loading && <p>Loading clients...</p>}
        {error && <p className="text-red-500">{error}</p>}
        {!loading && !error && (
          <div className="overflow-x-auto bg-gray-800 rounded-lg shadow">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-700">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Status</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Name</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Endpoint IP</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Data Transfer (DL / UL)</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Latest Handshake</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-700">
                {clients.length > 0 ? clients.map((client) => (
                  <tr key={client.id} className="hover:bg-gray-700/50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className={`h-2.5 w-2.5 rounded-full ${client.is_connected ? 'bg-green-500' : 'bg-red-500'}`}></div>
                        <span className="ml-2 text-sm text-gray-300">{client.is_active ? 'Enabled' : 'Disabled'}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-white">{client.name}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{client.endpoint_ip || 'N/A'}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">
                      {formatBytes(client.transfer_rx)} / {formatBytes(client.transfer_tx)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{timeAgo(client.latest_handshake_at)}</td>
                  </tr>
                )) : (
                  <tr>
                    <td colSpan={5} className="text-center py-8 text-gray-400">No clients found.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </main>
  );
}
