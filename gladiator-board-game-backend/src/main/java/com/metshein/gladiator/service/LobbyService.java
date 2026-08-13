package com.metshein.gladiator.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LobbyService {
    
    private final Set<String> activeLobbies = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Set<String>> lobbyPlayers = new ConcurrentHashMap<>();

    public String createLobby() {
        String lobbyId;

        do {
            // Generate a random 6-character alphanumeric lobby ID
            lobbyId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (activeLobbies.contains(lobbyId)); // ensures uniqueness

        activeLobbies.add(lobbyId);
        lobbyPlayers.putIfAbsent(lobbyId, ConcurrentHashMap.newKeySet());
        return lobbyId;
    }

    public boolean isValidLobby(String lobbyId) {
        // Check if the lobbyId is not null and exists in the activeLobbies set
        return lobbyId != null && activeLobbies.contains(lobbyId.toUpperCase());
    }

    public Set<String> addPlayerToLobby(String lobbyId, String playerName) {
        String normalizedLobby = normalizeLobbyId(lobbyId);
        String normalizedPlayer = normalizePlayerName(playerName);

        if (normalizedLobby == null || normalizedPlayer == null) {
            return Set.of(); // Return an empty set if either is null
        }

        if (!activeLobbies.contains(normalizedLobby)) {
            return Set.of();
        }

        lobbyPlayers.putIfAbsent(normalizedLobby, ConcurrentHashMap.newKeySet());
        lobbyPlayers.get(normalizedLobby).add(normalizedPlayer);

        return new LinkedHashSet<>(lobbyPlayers.get(normalizedLobby)); // Return a copy to avoid external modification
    }

    public Set<String> getPlayersInLobby(String lobbyId) {
        String normalizedLobby = normalizeLobbyId(lobbyId);

        if (normalizedLobby == null) {
            return Set.of();
        }

        Set<String> players = lobbyPlayers.get(normalizedLobby);

        if (players == null) {
            return Set.of();
        }

        return new LinkedHashSet<>(players);
    }

    private String normalizeLobbyId(String lobbyId) {
        if (lobbyId == null) {
            return null;
        }
        String value = lobbyId.trim().toUpperCase();
        return value.isEmpty() ? null : value;
    }

    private String normalizePlayerName(String playerName) {
        if (playerName == null) {
            return null;
        }
        String value = playerName.trim();
        return value.isEmpty() ? null : value;
    }
}
