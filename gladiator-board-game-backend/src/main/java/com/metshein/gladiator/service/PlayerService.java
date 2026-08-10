package com.metshein.gladiator.service;

import org.springframework.stereotype.Service;

import com.metshein.gladiator.entity.Player;

@Service
public class PlayerService {
    
    public boolean validate(Player player) {
        if (player.getName() == null || player.getName().isEmpty()) {
            return false;
        }
        
        if (player.getId() == null) {
            return false;
        }
        return true;

    }
}
