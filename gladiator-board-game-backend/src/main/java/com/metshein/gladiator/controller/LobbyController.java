package com.metshein.gladiator.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.metshein.gladiator.service.LobbyService;

@RestController
@RequestMapping("/lobby")
@CrossOrigin(origins = "*")
public class LobbyController {
    
    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    // host creates a new lobby
    @PostMapping
    public ResponseEntity<Map<String, String>> createLobby() {
        String lobbyId = lobbyService.createLobby();
        return ResponseEntity.ok(Map.of("lobbyId", lobbyId));
    }

    // quest validates a lobby code before joining
    @GetMapping("/{lobbyId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkLobby(@PathVariable String lobbyId) {
        boolean exists = lobbyService.isValidLobby(lobbyId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
    
}
