package com.metshein.gladiator.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.metshein.gladiator.entity.Player;
import com.metshein.gladiator.repository.PlayerRepository;
import com.metshein.gladiator.service.PlayerService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
public class PlayerController {
    
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;

    @GetMapping("players")
    public List<Player> getPlayers() {
        //System.out.println("Getting players");
        return playerRepository.findAll();
    }

    @PostMapping("addPlayer")
    public Player addPlayer(@RequestBody Player player) {
        playerService.validate(player);
        
        return playerRepository.save(player);
    }
    
}
