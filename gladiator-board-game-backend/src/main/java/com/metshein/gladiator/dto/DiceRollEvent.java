package com.metshein.gladiator.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class DiceRollEvent {
    
    private String playerName;
    private int roll;
    private int total;
    private String eventType = "DICE_ROLL";
    private Instant timestamp;

}
    