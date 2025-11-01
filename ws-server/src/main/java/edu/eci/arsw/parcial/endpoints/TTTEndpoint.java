package edu.eci.arsw.parcial.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.eci.arsw.parcial.service.Game;
import edu.eci.arsw.parcial.service.RoomsRegistry;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@ServerEndpoint("/parcial/tictac")
public class TTTEndpoint {
    private static final Logger logger = Logger.getLogger(TTTEndpoint.class.getName());
    private static final ObjectMapper M = new ObjectMapper();
    private static final String ERROR_TYPE = "error";

    @OnOpen
    public void onOpen(Session session) {
        try {
            session.getBasicRemote().sendText("{\"type\":\"hello\",\"msg\":\"Connection established.\"}");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "onOpen send failed", e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonNode msg = M.readTree(message);
            String type = msg.path("type").asText();
            if ("join".equals(type)) {
                String room = msg.path("room").asText();
                Game g = RoomsRegistry.getOrCreate(room);
                String symbol = RoomsRegistry.join(room, session);
                if (symbol == null) {
                    send(session, obj("type", ERROR_TYPE, ERROR_TYPE, "Room full"));
                    return;
                }
                ObjectNode joined = obj("type","joined","room",room,"symbol",symbol);
                joined.putPOJO("board", g.getBoard());
                joined.put("turn", g.getTurn());
                send(session, joined);
                broadcastState(room, null, null);
            } else if ("move".equals(type)) {
                String room = msg.path("room").asText();
                int idx = msg.path("index").asInt(-1);
                Game g = RoomsRegistry.getOrCreate(room);
                boolean ok = g.applyMove(session, idx);
                if (!ok) {
                    send(session, obj("type", ERROR_TYPE, ERROR_TYPE, "Illegal move"));
                    return;
                }
                String winner = g.getWinner();
                broadcastState(room, idx, winner);
            } else if ("leave".equals(type)) {
                RoomsRegistry.leave(session);
            } else {
                send(session, obj("type", ERROR_TYPE, ERROR_TYPE, "Unknown type"));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "onMessage error", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        String room = RoomsRegistry.roomOf(session);
        RoomsRegistry.leave(session);
        if (room != null) {
            broadcast(room, obj("type","opponent_left"));
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        logger.log(Level.SEVERE, "WS error", t);
        RoomsRegistry.leave(session);
    }

    private void broadcastState(String room, Integer lastIdx, String winner) {
        Game g = RoomsRegistry.getOrCreate(room);
        ObjectNode payload = obj("type","state");
        payload.putPOJO("board", g.getBoard());
        payload.put("turn", g.getTurn());
        if (lastIdx != null) payload.put("lastIndex", lastIdx);
        if (winner != null) payload.put("winner", winner);
        broadcast(room, payload);
    }

    private void broadcast(String room, ObjectNode payload) {
        Set<Session> sessions = RoomsRegistry.sessionsIn(room);
        for (Session s : sessions) {
            send(s, payload);
        }
    }

    private static void send(Session s, ObjectNode node) {
        try {
            s.getBasicRemote().sendText(node.toString());
        } catch (IOException e) {
            logger.log(Level.WARNING, "send failed", e);
        }
    }

    private static ObjectNode obj(String k1, String v1) {
        ObjectNode n = M.createObjectNode();
        n.put(k1, v1);
        return n;
    }
    private static ObjectNode obj(String k1, String v1, String k2, String v2) {
        ObjectNode n = obj(k1, v1);
        n.put(k2, v2);
        return n;
    }
    private static ObjectNode obj(String k1, String v1, String k2, String v2, String k3, String v3) {
        ObjectNode n = obj(k1, v1, k2, v2);
        n.put(k3, v3);
        return n;
    }
}
