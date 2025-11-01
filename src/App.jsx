import { useEffect, useMemo, useState } from 'react';
import { io } from 'socket.io-client';
import { v4 as uuid } from 'uuid';

function getRoomIdFromURL() {
  const params = new URLSearchParams(window.location.search);
  return params.get('room') || null;
}
function setRoomIdInURL(id) {
  const params = new URLSearchParams(window.location.search);
  params.set('room', id);
  window.history.replaceState({}, '', `?${params.toString()}`);
}

export default function App() {
  const [roomId, setRoomId] = useState(getRoomIdFromURL());
  const [state, setState] = useState(null); 

  const socket = useMemo(() => io('http://localhost:3001'), []);

  useEffect(() => {
    if (!roomId) return;
    socket.emit('join', { roomId });
    socket.on('state', setState);
    return () => {
      socket.off('state');
    };
  }, [socket, roomId]);

  function createRoom() {
    const id = uuid().slice(0, 8);
    setRoomIdInURL(id);
    setRoomId(id);
  }

  function clickSquare(i) {
    socket.emit('move', { roomId, square: i });
  }

  function reset() {
    socket.emit('reset', { roomId });
  }

  if (!roomId) {
    return (
      <div className="container">
        <h1>Tic Tac Toe</h1>
        <button onClick={createRoom}>Create Room</button>
      </div>
    );
  }

  if (!state) {
    return (
      <div className="container">
        <h1>Tic Tac Toe</h1>
        <p>Loading game...</p>
        <p>Room ID: {roomId}</p>
      </div>
    );
  }

  const { squares, xIsNext, winner, players } = state;
  const status = winner
    ? `Winner: ${winner}`
    : players < 2
    ? 'Waiting for another player...'
    : `Next player: ${xIsNext ? 'X' : 'O'}`;

  return (
    <div className="container">
      <h1>Tic Tac Toe</h1>
      <p>Room ID: {roomId}</p>
      <div className="status">{status}</div>
      <div className="board">
        <div className="board-row">
          {squares.slice(0, 3).map((v, i) => (
            <button key={i} className="square" onClick={() => clickSquare(i)}>{v}</button>
          ))}
        </div>
        <div className="board-row">
          {squares.slice(3, 6).map((v, i) => (
            <button key={i + 3} className="square" onClick={() => clickSquare(i + 3)}>{v}</button>
          ))}
        </div>
        <div className="board-row">
          {squares.slice(6, 9).map((v, i) => (
            <button key={i + 6} className="square" onClick={() => clickSquare(i + 6)}>{v}</button>
          ))}
        </div>
        <div style={{ marginTop: 12 }}>
          <button onClick={reset}>Restart</button>
        </div>
      </div>
    </div>
  );
}