import './App.css';
import Navbar from './components/Navbar.js';
import { Routes, Route} from 'react-router-dom';
import Players from './pages/Player.js';
import Home from './pages/Home.js';

function App() {
  return (
    <div className="App">
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/players" element={<Players />} />
        </Routes>
      </main>
    </div>
  )
}

export default App