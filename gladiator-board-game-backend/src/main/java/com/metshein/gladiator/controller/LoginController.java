package com.metshein.gladiator.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
public class LoginController {
    
    @GetMapping("/")
    public String home() {
        return "Hello home!";
    }

    @GetMapping("/login")
    public String login() {
        return "Hello login! ";
    }
}
