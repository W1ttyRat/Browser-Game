import { Client } from "@stomp/stompjs";
import { useState, useEffect } from "react";
import type { DiceModel } from "../models/Dice.ts";

export const GameManager = () => {
    const [lobbyId, setLobbyId] = useState<string | null>(null);
    const [inputCode, setInputCode] = useState("");
    const [playerName, setPlayerName] = useState("");
    const [error, setError] = useState("");

    // host creates a new lobby 

    const createLobby = async () => {
        if (!playerName.trim()) return setError("Player name is required to create a lobby.");

        try {
            const res = await fetch("http://10.10.10.163:8080/lobby", { method: "POST" });
            const data = await res.json();
            setError(""); // clear any previous errors
            setLobbyId(data.lobbyId); // triggers transition to the game page
        } catch (err) {
            setError("Failed to create lobby.");
        }
    };

    const handleJoinLobby = async () => {
        if (!playerName.trim()) {
            return setError("Player name is required to join a lobby.");
        }
        if (!inputCode.trim()) {
            return setError("Lobby code is required to join a lobby.");
        }

        const cleanCode = inputCode.trim().toUpperCase();

        try {
            const res = await fetch(`http://10.10.10.163:8080/lobby/${cleanCode}/exists`);
            const data = await res.json();

            if (data.exists) {
                setError(""); // clear any previous errors
                setLobbyId(cleanCode); // triggers transition to the game page
            } else {
                setError("lobby not found");
            }
        } catch (err) {
            setError("Failed to validate lobby");
        }
    };

    // render the lobby creation/joining UI or the game page based on lobbyId
    if (!lobbyId) {
        return (
        <div style={{ padding: "20px" }}>
            <h2>Multiplayer Dice Game</h2>
            {error && <p style={{ color: "red" }}>{error}</p>}

            <input
            type="text"
            placeholder="Enter your name"
            value={playerName}
            onChange={(e) => setPlayerName(e.target.value)}
            /><br /><br />

            <button onClick={createLobby}>Create New Lobby</button>

            <hr />

            <input
            type="text"
            placeholder="Enter 6-char Lobby Code"
            value={inputCode}
            onChange={(e) => setInputCode(e.target.value)}
            />
            <button onClick={handleJoinLobby}>Join Lobby</button>
        </div>
        );
    }

    return <ActiveLobbyRoom lobbyId={lobbyId} playerName={playerName} />;
};

// active game component
const ActiveLobbyRoom = ({lobbyId, playerName }: { lobbyId: string; playerName: string }) => {
    const [players, setPlayers] = useState<string[]>([]);
    const [latestRoll, setLatestRoll] = useState<DiceModel | null>(null);
    const [stompClient, setStompClient] = useState<Client | null>(null);

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new WebSocket(`ws://10.10.10.163:8080/ws`),
            onConnect: () => {
                // subscribes to lobby id for updates
                client.subscribe(`/topic/lobby/${lobbyId}`, (message) => {
                    const payload = JSON.parse(message.body) as Partial<DiceModel> & {type?: string};

                    const eventType = payload.eventType ?? payload.type ?? "UNKNOWN";

                    if (eventType === "ROLL") {
                        setLatestRoll({
                            playerName: payload.playerName ?? playerName,
                            roll: payload.roll ?? 0,
                            total: payload.total ?? payload.roll ?? 0, // fallback to roll if total is not provided
                            eventType,
                            timestamp: payload.timestamp ?? new Date().toISOString(),
                        });
                    } else if (eventType === "JOIN" && payload.playerName) {
                        setPlayers((prev) => prev.includes(payload.playerName!) ? prev : [...prev, payload.playerName!]);
                    }
                });

                // notify others of joining
                client.publish({
                    destination: `/app/lobby/${lobbyId}/join`,
                    body: JSON.stringify({ playerName }),
                });
            },
        });

        client.activate();
        setStompClient(client);

        return () => {
            client.deactivate();
        };
    }, [lobbyId, playerName]);

    const rollDice = () => {
        if (!stompClient) return;
        
        stompClient.publish({
            destination: `/app/lobby/${lobbyId}/roll`,
            body: JSON.stringify({ playerName }),
        });
    };

    return (
    <div style={{ padding: "20px" }}>
      <h3>
        Lobby Code: <span style={{ color: "blue" }}>{lobbyId}</span>
      </h3>
      <p>Share this code with friends so they can join!</p>

      <button onClick={rollDice}>🎲 Roll Dice</button>

      <h4>Connected players</h4>
      <ul>
        {players.length === 0 ? (
          <li>No one else has joined yet.</li>
        ) : (
          players.map((name) => <li key={name}>{name}</li>)
        )}
      </ul>

      {latestRoll && (
        <h4>
          {latestRoll.playerName} rolled a {latestRoll.roll}!
        </h4>
      )}
    </div>
  );
}

export default GameManager;