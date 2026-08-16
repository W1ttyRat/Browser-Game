import { Link } from "react-router-dom";
import { GoogleLogin } from "@react-oauth/google";

export default function Navbar() {
  const handleGoogleSuccess = async (credentialResponse: any) => {
    const token = credentialResponse.credential;

    if (!token) {
        console.error("No token received from Google login");
        return;
    }

    const res = await fetch(`${import.meta.env.VITE_BACKEND_URL}/auth/google`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ credential: token }),
    });

    const data = await res.json();
    const accessToken = data.accessToken;

    sessionStorage.setItem("accessToken", accessToken);
  };

  const handleGoogleError = () => {
    console.log("Google login failed");
  };

  return (
    <nav className="nav" style={{ display: "flex", gap: "12px", alignItems: "center" }}>
      <Link to="/">Home</Link>
      <Link to="/players">Players</Link>
      <Link to="/chat">Chat</Link>
      <Link to="/lobby">Lobby</Link>

      <GoogleLogin
        onSuccess={handleGoogleSuccess}
        onError={handleGoogleError}
        text="signin_with"
      />
    </nav>
  );
}