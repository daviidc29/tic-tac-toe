package edu.eci.arsw.parcial;
import java.util.concurrent.locks.ReentrantLock;

public class Game {

    private final String id;

    public Game(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    private Character[] board = new Character[9]; // null, 'X', 'O'
    private String playerX, playerO;
    private char next = 'X';
    private String winner; // "X" | "O" | "DRAW" | null
    private final ReentrantLock lock = new ReentrantLock(true);

    public void assignPlayer(String playerId) {
        if (playerX == null) {
            playerX = playerId;
        } else if (playerO == null && !playerId.equals(playerX)) {
            playerO = playerId;
        }
    }

    public void move(int idx, String playerId) {
        if (winner != null) {
            throw new IllegalStateException("Game already finished");
        }
        if (idx < 0 || idx > 8) {
            throw new IllegalArgumentException("Index out of range");
        }
        if (board[idx] != null) {
            throw new IllegalArgumentException("Cell occupied");
        }
        if ((next == 'X' && !playerId.equals(playerX)) || (next == 'O' && !playerId.equals(playerO))) {
            throw new IllegalStateException("Not your turn");
        }

        char symbol = (playerId.equals(playerX)) ? 'X' : 'O';
        board[idx] = symbol;

        Character w = Rules.winner(board);
        if (w != null) {
            winner = String.valueOf(w);
            return;
        }

        boolean full = true;
        for (Character c : board) {
            if (c == null) {
                full = false;
                break;
            }
        }
        if (full) {
            winner = "DRAW";
            return;
        }

        next = (next == 'X') ? 'O' : 'X';
    }

    public Game getOrCreate(String id) {

        Game game = findGameById(id);

        if (game == null) {

            game = new Game(id);

            saveGame(game);

        }

        return game;

    }

    private Game findGameById(String id) {
        return GameRepository.findGameById(id);
    }

    private void saveGame(Game game) {
        GameRepository.saveGame(game);
    }

    public ReentrantLock getLock() {
        return lock;
    }

}

class GameRepository {

    private static final java.util.Map<String, Game> games = new java.util.concurrent.ConcurrentHashMap<>();

    public static Game getOrCreate(String id) {
        return games.computeIfAbsent(id, Game::new);
    }

    public static Game findGameById(String id) {
        return games.get(id);
    }

    public static void saveGame(Game game) {
        games.put(game.getId(), game);
    }
}
