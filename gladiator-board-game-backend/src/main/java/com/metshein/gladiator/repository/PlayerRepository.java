package com.metshein.gladiator.repository;

import com.metshein.gladiator.entity.Player;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    
}
