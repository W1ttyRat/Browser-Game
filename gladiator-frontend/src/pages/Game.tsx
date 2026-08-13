import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import type { DiceModel } from "../models/Dice.ts";
/*
const backendUrl = import.meta.env.VITE_BACKEND_URL || "http://10.10.10.163:8080";

function Game() {
    const [playerName, setPlayerName] = useState("");
    const [roll, setRoll] = useState(0);
    const [total, setTotal] = useState(0);
    const [eventType, setEventType] = useState("");
    const [timestamp, setTimestamp] = useState("");
    const [client, setClient] = useState<Client | null>(null);
    
    useEffect(() => {
        const socket = new WebSocket(`${backendUrl}/ws`);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
        });
    });

};

export default Game;
*/