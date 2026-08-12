import { Link } from "react-router-dom"

export default function Navbar() {
    return (
        <div className="nav">
            <Link to="/">Home</Link>
            <Link to="/players">Players</Link>
        </div>
    )
}