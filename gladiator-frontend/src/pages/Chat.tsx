import { useState, useEffect } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { TextField, Button, List, ListItem, Typography } from "@mui/material";
import type { ChatMessage } from "../models/ChatMessage.ts";

const WebSocketPage = () => {
    const [nickname, setNickname] = useState('');
    const [message, setMessage] = useState('');
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [client, setClient] = useState<Client | null>(null);

    useEffect(() => {
        const socket = new SockJS("http://10.10.10.163:8080/ws");
        const stompClient = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                stompClient.subscribe("/topic/messages", (msg) => {
                    const payload = JSON.parse(msg.body);
                    console.log("Received message:", JSON.stringify(payload, null, 2));

                    const nextMessage: ChatMessage = {
                        nickname: payload.nickname ?? "Unknown",
                        message: payload.content ?? payload.message ?? payload.text ?? "No message content",
                        timestamp: payload.timestamp ?? new Date().toISOString(),
                    };

                    setMessages((prevMessages) => [...prevMessages, nextMessage]);
                });
            },
        });

        stompClient.activate(); // activate the websocket connection
        setClient(stompClient);

        return () => {
            stompClient.deactivate(); // clean up when the component unmounts
        };
    }, []);

    const sendMessage = () => {
        if (client && message.trim() && nickname.trim()) {
            const chatMessage = {
                nickname,
                content: message,
                timestamp: new Date().toISOString(),
            };

            client.publish({
                destination: "/app/chat",
                body: JSON.stringify(chatMessage),
                headers: {
                    "content-type": "application/json",
                },
            });

            setMessage(""); // clear the input field
        }
    };

    return (
        <div style={{ maxWidth: 600, margin: "auto", padding: 20 }}>
            <Typography variant="h4" gutterBottom>
                Web Chat
            </Typography>
            <List>
                {messages.map((msg, index) => (
                    <ListItem key={index}>
                        <Typography variant="body1">
                            <strong>{msg.nickname ?? "Unknown"}:</strong> {msg.message ?? "(no message)"} ({msg.timestamp ?? "no time"})
                        </Typography>
                    </ListItem>
                ))}
            </List>
            <TextField
                label="Nickname"
                fullWidth
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                margin="normal"
            />
            <TextField
                label="Message"
                fullWidth
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                margin="normal"
            />
            <Button variant="contained" color="primary" onClick={sendMessage} fullWidth>
                Send
            </Button>
        </div>
    )
}

export default WebSocketPage;