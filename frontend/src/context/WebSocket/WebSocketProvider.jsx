import React, {createContext, useContext, useEffect, useState} from 'react';
import {Client} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {useAuthContext} from "../Auth/AuthContext.jsx";
import {useNotification} from "../Notification/NotificationProvider.jsx";
import {useStorageOperations} from "../Files/FileOperationsProvider.jsx";

const WebSocketContext = createContext(null);

export const useWebSocket = () => useContext(WebSocketContext);

export const WebSocketProvider = ({children}) => {
    const {auth} = useAuthContext();
    const {showInfo, showError} = useNotification();

    const checkPendingArchives = async () => {
        const pending = JSON.parse(localStorage.getItem('pendingArchives') || "[]");
        if (pending.length === 0) return;

        const stillPending = [];

        for (const ticket of pending) {
            try {
                const response = await fetch(`${window.APP_CONFIG.baseApi}/resource/download/status?ticket=${ticket}`);

                if (response.status === 200) {
                    const data = await response.json();
                    if (data.status === 'READY') {
                        downloadFileByUrl(data.downloadUrl);
                        showInfo("Архив, который вы заказывали ранее, готов!");
                    } else if (data.status === 'ERROR') {
                        showError("Не удалось создать один из архивов.");
                    }
                } else {
                    stillPending.push(ticket);
                }
            } catch (e) {
                stillPending.push(ticket);
            }
        }

        localStorage.setItem('pendingArchives', JSON.stringify(stillPending));
    };

    const downloadFileByUrl = (url) => {
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', '');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    const handleSocketMessage = (msg) => {
        const body = JSON.parse(msg.body);
        const ticket = body.ticket;

        const pending = JSON.parse(localStorage.getItem('pendingArchives') || "[]");
        const newPending = pending.filter(t => t !== ticket);
        localStorage.setItem('pendingArchives', JSON.stringify(newPending));

        if (body.status === 'READY') {
            downloadFileByUrl(body.url);
            showInfo("Архив готов! Скачивание началось.");

            window.dispatchEvent(new CustomEvent('archive-ready', { detail: ticket }));

        } else if (body.status === 'ERROR') {
            showError("Ошибка при создании архива: " + body.message);
            window.dispatchEvent(new CustomEvent('archive-error', { detail: ticket }));
        }
    };

    useEffect(() => {
        if (auth.isAuthenticated) {
            checkPendingArchives();

            const socketUrl = `${window.APP_CONFIG.baseUrl}${window.APP_CONFIG.baseApi}/../ws`;

            const client = new Client({
                webSocketFactory: () => new SockJS(socketUrl),
                reconnectDelay: 5000,
                onConnect: () => {
                    console.log("✅ WebSocket Connected");
                    client.subscribe('/user/queue/archive', handleSocketMessage);
                },
            });

            client.activate();

            return () => {
                client.deactivate();
            };
        }
    }, [auth.isAuthenticated]);

    return (
        <WebSocketContext.Provider value={{}}>
            {children}
        </WebSocketContext.Provider>
    );
};