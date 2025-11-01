
package edu.eci.arsw.parcial.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;
import jakarta.websocket.Session;

public class RoomsRegistry {
    private RoomsRegistry() {}
    private static final ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Set<Session>> roomSessions = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Session, String> sessionRoom = new ConcurrentHashMap<>();

    public static synchronized Game getOrCreate(String room) {
        games.putIfAbsent(room, new Game());
        roomSessions.putIfAbsent(room, ConcurrentHashMap.newKeySet());
        return games.get(room);
    }

    public static synchronized String join(String room, Session s) {
        Game g = getOrCreate(room);
        String symbol = g.addPlayer(s);
        roomSessions.get(room).add(s);
        sessionRoom.put(s, room);
        return symbol;
    }

    public static synchronized void leave(Session s) {
        String room = sessionRoom.remove(s);
        if (room == null) return;
        Set<Session> sessions = roomSessions.get(room);
        if (sessions != null) {
            sessions.remove(s);
        }
        Game g = games.get(room);
        if (g != null) {
            g.removePlayer(s);
            if (sessions == null || sessions.isEmpty()) {
                games.remove(room);
                roomSessions.remove(room);
            }
        }
    }

    public static Set<Session> sessionsIn(String room) {
        return roomSessions.getOrDefault(room, Set.of());
    }

    public static String roomOf(Session s) {
        return sessionRoom.get(s);
    }
}
