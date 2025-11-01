package edu.eci.arsw.parcial.controller;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import edu.eci.arsw.parcial.Game;
import edu.eci.arsw.parcial.JoinMessage;
import edu.eci.arsw.parcial.MoveMessage;
import edu.eci.arsw.parcial.service.GameService;




@Controller
public class GameController {
  private final GameService gameService;
  private final SimpMessagingTemplate msg;

  public GameController(GameService gameService, SimpMessagingTemplate msg) {
    this.gameService = gameService; this.msg = msg;
  }

  @MessageMapping("/game/{id}/join")
  public void join(@DestinationVariable String id, JoinMessage join) {
    Game g = gameService.getOrCreate(id);
    g.getLock().lock();
    try {
      g.assignPlayer(join.playerId());
      msg.convertAndSend("/topic/game/" + id, gameService.snapshot(g));
    } finally { g.getLock().unlock(); }
  }

  @MessageMapping("/game/{id}/move")
  public void move(@DestinationVariable String id, MoveMessage move) {
    Game g = gameService.getOrCreate(id);
    g.getLock().lock();
    try {
      g.move(move.index(), move.playerId());
      msg.convertAndSend("/topic/game/" + id, gameService.snapshot(g));
    } catch (RuntimeException ex) {
      
      msg.convertAndSend("/topic/game/" + id, ex.getMessage());
    } finally { g.getLock().unlock(); }
  }
}

