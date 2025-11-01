/* global React, ReactDOM */
function wsURL() {
  const loc = globalThis.location;
  const scheme = (loc.protocol === 'https:') ? 'wss' : 'ws';
  return scheme + '://' + loc.host + '/parcial/tictac';
}

class TTTChannel {
  constructor(onMessage) {
    this.ws = new WebSocket(wsURL());
    this.ws.onopen = () => console.log('WS open');
    this.ws.onmessage = (evt) => {
      try {
        const msg = JSON.parse(evt.data);
        if (onMessage) { onMessage(msg); }
      } catch (e) { console.error('Bad message', e, evt.data); }
    };
    this.ws.onerror = (e) => console.error('WS error', e);
    this.ws.onclose = () => console.log('WS closed');
  }
  send(obj) { if (this.ws && this.ws.readyState === WebSocket.OPEN) { this.ws.send(JSON.stringify(obj)); } }
  join(room) { this.send({ type:'join', room }); }
  move(room, index) { this.send({ type:'move', room, index }); }
  leave() { this.send({ type:'leave' }); }
}

function Square({ value, onSquareClick, disabled }) {
  return (
    <button
      className={"square" + (value === 'O' ? " o" : "")}
      disabled={disabled}
      onClick={onSquareClick}
    >
      {value || ''}
    </button>
  );
}

function reset(){
  globalThis.location.reload();
}

function App(){
  const [ws, setWs] = React.useState(null);
  const [room, setRoom] = React.useState('sala-1');
  const [joined, setJoined] = React.useState(false);
  const [symbol, setSymbol] = React.useState(null);
  const [board, setBoard] = React.useState(new Array(9).fill(null));
  const [turn, setTurn] = React.useState('X');
  const [winner, setWinner] = React.useState(null);
  const [log, setLog] = React.useState([]);

  React.useEffect(()=>{
    const chan = new TTTChannel(msg => {
      if (msg.type === 'hello') {
        // ignore
      } else if (msg.type === 'joined') {
        setJoined(true);
        setSymbol(msg.symbol);
        setBoard(msg.board);
        setTurn(msg.turn);
        pushLog(`Entraste a la sala "${msg.room}" como ${msg.symbol}`);
      } else if (msg.type === 'state') {
        setBoard(msg.board);
        setTurn(msg.turn);
        if (msg.winner) setWinner(msg.winner);
      } else if (msg.type === 'opponent_left') {
        pushLog('Tu oponente salió de la sala.');
      } else if (msg.type === 'error') {
        pushLog('Error: ' + msg.error);
        alert(msg.error);
      }
    });
    setWs(chan);
    return () => chan.leave();
  },[]);

  function pushLog(s){
    setLog(prev => [s, ...prev].slice(0,8));
  }

  function doJoin(){
    if (!room) return;
    if (ws) { ws.join(room); }
  }
  function clickCell(i){
    if (!joined || winner) return;
    if (board[i] != null) return;
    if (turn !== symbol) return;
    if (ws) { ws.move(room, i); }
  }

  let statusMsg;
  if (joined) {
    let winnerMsg = '';
    if (winner) {
      winnerMsg = winner === 'DRAW' ? 'Empate' : 'Ganó ' + winner;
    }
    statusMsg = <span> Eres: {symbol}. Turno: {turn}. {winnerMsg}</span>;
  } else {
    statusMsg = <span className="muted">Crea o entra a una sala para comenzar</span>;
  }

  return (
    <div className="card">
      <div className="row">
        <input placeholder="id de sala" value={room} onChange={e => setRoom(e.target.value)}/>
        <button onClick={doJoin} disabled={joined}>Crear/Entrar</button>
        <span className="badge">/parcial/tictac</span>
      </div>
      <div className="status">
        {statusMsg}
      </div>
      <div className="board-row">
        <Square value={board[0]} disabled={!joined || winner || (turn!==symbol) || board[0]!=null} onSquareClick={()=>clickCell(0)} />
        <Square value={board[1]} disabled={!joined || winner || (turn!==symbol) || board[1]!=null} onSquareClick={()=>clickCell(1)} />
        <Square value={board[2]} disabled={!joined || winner || (turn!==symbol) || board[2]!=null} onSquareClick={()=>clickCell(2)} />
      </div>
      <div className="board-row">
        <Square value={board[3]} disabled={!joined || winner || (turn!==symbol) || board[3]!=null} onSquareClick={()=>clickCell(3)} />
        <Square value={board[4]} disabled={!joined || winner || (turn!==symbol) || board[4]!=null} onSquareClick={()=>clickCell(4)} />
        <Square value={board[5]} disabled={!joined || winner || (turn!==symbol) || board[5]!=null} onSquareClick={()=>clickCell(5)} />
      </div>
      <div className="board-row">
        <Square value={board[6]} disabled={!joined || winner || (turn!==symbol) || board[6]!=null} onSquareClick={()=>clickCell(6)} />
        <Square value={board[7]} disabled={!joined || winner || (turn!==symbol) || board[7]!=null} onSquareClick={()=>clickCell(7)} />
        <Square value={board[8]} disabled={!joined || winner || (turn!==symbol) || board[8]!=null} onSquareClick={()=>clickCell(8)} />
      </div>
      <div style={{ marginTop: 12 }}>
        <button onClick={reset}>Reiniciar</button>
        <div style={{ fontSize: 12, color: '#666', marginTop: 6 }}>Abre esta misma URL en otra pestaña/otro navegador para el segundo jugador.</div>
      </div>
      <div style={{ marginTop: 12, fontSize: 12, color: '#666' }}>
        <div>Registro:</div>
        <ul>
          {log.map((l) => (
            <li key={typeof l === 'string' ? l : JSON.stringify(l)}>{l}</li>
          ))}
        </ul>
      </div>
    </div>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App/>);
