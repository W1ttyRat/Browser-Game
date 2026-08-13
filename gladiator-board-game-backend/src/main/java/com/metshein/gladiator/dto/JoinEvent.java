package com.metshein.gladiator.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class JoinEvent {
    
    private String playerName;
    private String eventType = "JOIN";
    private Instant timestamp;
}
