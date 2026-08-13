package com.metshein.gladiator.controller;

import java.util.Date;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.metshein.gladiator.dto.ChatMessage;


@Controller
public class ChatController {
    
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) throws Exception {
        chatMessage.setTimestamp(new Date());
        return chatMessage;
    }
}
