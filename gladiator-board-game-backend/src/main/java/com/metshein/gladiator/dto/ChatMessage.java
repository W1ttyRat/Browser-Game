package com.metshein.gladiator.dto;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Data
@ToString
public class ChatMessage {
    
    private String nickname;
    private String content;
    private Date timestamp;
}
