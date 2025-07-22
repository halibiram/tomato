"use client";

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import api from '../../../lib/axios';
import Cookies from 'js-cookie';
import QRCode from 'qrcode.react';

// Re-using the Client interface from the main page
interface Client {
    id: number;
    name: string;
    is_active: boolean;
    is_connected: boolean;
}

export default function ClientDetailPage() {
    const [client, setClient] = useState<Client | null>(null);
    const [config, setConfig] = useState<string>('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const params = useParams();
    const router = useRouter();
    const { id } = params;

    useEffect(() => {
        if (!id) return;

        const token = Cookies.get('access_token');
        if (!token) {
            router.push('/login');
            return;
        }

        const fetchClientData = async () => {
            try {
                const [clientRes, configRes] = await Promise.all([
                    api.get(`/clients/${id}`),
                    api.get(`/clients/${id}/config`),
                ]);
                setClient(clientRes.data);
                setConfig(configRes.data);
            } catch (err: any) {
                console.error(err);
                setError('Failed to fetch client data.');
            } finally {
                setLoading(false);
            }
        };

        fetchClientData();
    }, [id, router]);

    const handleToggleStatus = async () => {
        if (!client) return;

        try {
            const newStatus = !client.is_active;
            const response = await api.put(`/clients/${client.id}/status?status=${newStatus}`);
            setClient(response.data); // Update client state with the response
            alert(`Client has been ${newStatus ? 'enabled' : 'disabled'}.`);
        } catch (err) {
            console.error(err);
            alert('Failed to update client status.');
        }
    };

    if (loading) return <div className="text-center p-10">Loading client details...</div>;
    if (error) return <div className="text-center p-10 text-red-500">{error}</div>;
    if (!client) return <div className="text-center p-10">Client not found.</div>;

    return (
        <main className="container mx-auto p-8">
            <button onClick={() => router.back()} className="mb-6 text-indigo-400 hover:text-indigo-300">
                &larr; Back to Dashboard
            </button>
            <h1 className="text-3xl font-bold mb-4">Client: {client.name}</h1>

            <div className="grid md:grid-cols-2 gap-8">
                {/* Left side: QR Code and Status */}
                <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
                    <h2 className="text-xl font-semibold mb-4">Mobile Config (QR Code)</h2>
                    <div className="p-4 bg-white inline-block rounded-lg">
                        <QRCode value={config} size={256} />
                    </div>
                    <div className="mt-6">
                        <h3 className="text-lg font-semibold">Status</h3>
                        <div className="flex items-center mt-2">
                            <span className={`px-3 py-1 text-sm font-semibold rounded-full ${client.is_active ? 'bg-green-800 text-green-100' : 'bg-red-800 text-red-100'}`}>
                                {client.is_active ? 'Enabled' : 'Disabled'}
                            </span>
                            <button
                                onClick={handleToggleStatus}
                                className="ml-4 px-4 py-2 font-bold text-white bg-indigo-600 rounded-md hover:bg-indigo-700"
                            >
                                {client.is_active ? 'Disable' : 'Enable'} Client
                            </button>
                        </div>
                    </div>
                </div>

                {/* Right side: Config File */}
                <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
                    <h2 className="text-xl font-semibold mb-4">Desktop Config File</h2>
                    <textarea
                        readOnly
                        value={config}
                        className="w-full h-64 p-3 font-mono text-sm text-white bg-gray-900 border border-gray-600 rounded-md resize-none"
                    />
                    <button
                        onClick={() => navigator.clipboard.writeText(config)}
                        className="mt-4 px-4 py-2 font-bold text-white bg-gray-600 rounded-md hover:bg-gray-500"
                    >
                        Copy to Clipboard
                    </button>
                </div>
            </div>
        </main>
    );
}
