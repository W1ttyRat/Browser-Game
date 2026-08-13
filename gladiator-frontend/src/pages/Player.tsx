import { useEffect, useState } from 'react'
import type { PlayerModel } from '../models/PlayerModel.ts'

const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://10.10.10.163:8080'

function Player() {
    const [player, setPlayer] = useState<PlayerModel[]>([]);

    useEffect(() => {
        fetch(`${backendUrl}/players`)
            .then((res) => res.json())
            .then((data) => setPlayer(data))
    }, [])

    return (
        <div>
            <h1>Players</h1>
            <ul>
                {player.map((player) => (
                    <li key={player.id}>{player.name}</li>
                ))}
            </ul>
        </div>
    )
}

export default Player