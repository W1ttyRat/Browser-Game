package com.metshein.gladiator.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.metshein.gladiator.dto.DiceRollEvent;
import com.metshein.gladiator.dto.JoinEvent;
import com.metshein.gladiator.service.LobbyService;

@Controller
public class DiceController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final LobbyService lobbyService;

    public DiceController(SimpMessagingTemplate messagingTemplate, LobbyService lobbyService) {
        this.messagingTemplate = messagingTemplate;
        this.lobbyService = lobbyService;
    }

    @MessageMapping("/lobby/{lobbyId}/join")
    public void joinLobby(@DestinationVariable String lobbyId, @Payload JoinEvent event) {
        String destination = "/topic/lobby/" + lobbyId;

        if (!lobbyService.isValidLobby(lobbyId)) {
            Map<String, Object> errorPayload = new LinkedHashMap<>();
            errorPayload.put("eventType", "ERROR");
            errorPayload.put("message", "Lobby not found");
            Object payload = errorPayload;
            messagingTemplate.convertAndSend(destination, payload);
            return;
        }

        Set<String> players = lobbyService.addPlayerToLobby(lobbyId, event.getPlayerName());

        for (String player : players) {
            Map<String, Object> joinPayload = new LinkedHashMap<>();
            joinPayload.put("eventType", "JOIN");
            joinPayload.put("playerName", player);
            joinPayload.put("timestamp", Instant.now().toString());
            Object payload = joinPayload;
            messagingTemplate.convertAndSend(destination, payload);
        }
    }

    @MessageMapping("/lobby/{lobbyId}/roll")
    public void rollDice(@DestinationVariable String lobbyId, @Payload DiceRollEvent event) {
        String destination = "/topic/lobby/" + lobbyId;

        if (!lobbyService.isValidLobby(lobbyId)) {
            Map<String, Object> errorPayload = new LinkedHashMap<>();
            errorPayload.put("eventType", "ERROR");
            errorPayload.put("message", "Lobby not found");
            Object payload = errorPayload;
            messagingTemplate.convertAndSend(destination, payload);
            return;
        }
        int roll = ThreadLocalRandom.current().nextInt(1, 7); // Simulate a dice roll (1-6)
        
        event.setRoll(roll);
        event.setTotal(roll);
        event.setEventType("ROLL");
        event.setTimestamp(Instant.now());

        messagingTemplate.convertAndSend(destination, event);
    }
}
