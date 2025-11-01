import { createServer } from 'http';
import { Server } from 'socket.io';

const httpServer = createServer();
const io = new Server(httpServer, { cors: { origin: '*' } });

const rooms = new Map();

const lines = [
  [0,1,2],[3,4,5],[6,7,8],
  [0,3,6],[1,4,7],[2,5,8],
  [0,4,8],[2,4,6]
];
function winner(sq){
  for (const [a,b,c] of lines)
    if (sq[a] && sq[a]===sq[b] && sq[a]===sq[c]) return sq[a];
  return null;
}
const emptyState = () => ({
  squares: Array(9).fill(null),
  xIsNext: true,
  winner: null,
  players: { X:null, O:null }
});

io.on('connection', (socket) => {
  socket.on('join', ({ roomId }) => {
    socket.join(roomId);
    let room = rooms.get(roomId);
    if (!room) { room = emptyState(); rooms.set(roomId, room); }

    if (!room.players.X) room.players.X = socket.id;
    else if (!room.players.O && socket.id !== room.players.X) room.players.O = socket.id;

    io.to(roomId).emit('state', room);
  });

  socket.on('play', ({ roomId, index }) => {
    const room = rooms.get(roomId);
    if (!room || room.winner || room.squares[index]) return;

    const turn = room.xIsNext ? 'X' : 'O';
    const isX = room.players.X === socket.id;
    const isO = room.players.O === socket.id;
    if ((turn === 'X' && !isX) || (turn === 'O' && !isO)) return;

    room.squares[index] = turn;
    room.xIsNext = !room.xIsNext;
    room.winner = winner(room.squares);
    io.to(roomId).emit('state', room);
  });

  socket.on('reset', ({ roomId }) => {
    rooms.set(roomId, emptyState());
    io.to(roomId).emit('state', rooms.get(roomId));
  });

  socket.on('disconnecting', () => {
    for (const roomId of socket.rooms) {
      if (roomId === socket.id) continue;
      const room = rooms.get(roomId);
      if (!room) continue;

      if (room.players.X === socket.id) room.players.X = null;
      if (room.players.O === socket.id) room.players.O = null;

      if (!room.players.X && !room.players.O) {
        rooms.delete(roomId);
      } else {
        io.to(roomId).emit('state', room);
      }
    }
  });
});

httpServer.listen(3001, () => {
  console.log('listening on *:3001');
});