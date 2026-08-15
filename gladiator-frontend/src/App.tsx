import './App.css';
import Navbar from './components/Navbar.js';
import { Routes, Route} from 'react-router-dom';
import Players from './pages/Player.tsx';
import Home from './pages/Home.tsx';
import Chat from './pages/Chat.tsx';
import Lobby from './pages/Lobby.tsx';

function App() {
  return (
    <div className="App">
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/players" element={<Players />} />
          <Route path="/chat" element={<Chat />} />
          <Route path="/lobby" element={<Lobby />} />
        </Routes>
      </main>
    </div>
  )
}

export default App