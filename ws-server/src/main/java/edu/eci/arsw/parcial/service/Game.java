package edu.eci.arsw.parcial.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import jakarta.websocket.Session;

public class Game {
    private final String[] board = new String[9];
    private String turn = "X";
    private String winner = null;
    private final Map<Session, String> players = new HashMap<>();

    public synchronized String addPlayer(Session s) {
        if (players.containsKey(s)) return players.get(s);
        if (players.size() >= 2) return null;
        String symbol = players.containsValue("X") ? "O" : "X";
        players.put(s, symbol);
        return symbol;
    }

    public synchronized void removePlayer(Session s) {
        players.remove(s);
    }

    public synchronized boolean applyMove(Session s, int idx) {
        if (winner != null) return false;
        String symbol = players.get(s);
        if (symbol == null) return false;
        if (!Objects.equals(symbol, turn)) return false;
        if (idx < 0 || idx >= 9) return false;
        if (board[idx] != null) return false;
        board[idx] = symbol;
        turn = symbol.equals("X") ? "O" : "X";
        checkWinner();
        return true;
    }

    private void checkWinner() {
        int[][] lines = {
            {0,1,2},{3,4,5},{6,7,8},
            {0,3,6},{1,4,7},{2,5,8},
            {0,4,8},{2,4,6}
        };
        for (int[] l : lines) {
            String a = board[l[0]];
            String b = board[l[1]];
            String c = board[l[2]];
            if (a != null && a.equals(b) && a.equals(c)) {
                winner = a;
                return;
            }
        }
        boolean full = true;
        for (String s : board) if (s == null) { full = false; break; }
        if (full) winner = "DRAW";
    }

    public synchronized String[] getBoard() {
        return Arrays.copyOf(board, board.length);
    }
    public synchronized String getTurn() { return turn; }
    public synchronized String getWinner() { return winner; }
    public synchronized String getSymbol(Session s) { return players.get(s); }
}
