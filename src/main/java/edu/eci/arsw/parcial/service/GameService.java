package edu.eci.arsw.parcial.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import edu.eci.arsw.parcial.Game;
import edu.eci.arsw.parcial.GameState;

@Service
public class GameService {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public GameState snapshot(Game g) {
        return new GameState(g);
    }

    public GameState snapshotEmpty(Game g) {
        return new GameState(new Game(g.getId()));
    }

    public Game getOrCreate(String id) {
        return games.computeIfAbsent(id, Game::new);
    }

}
