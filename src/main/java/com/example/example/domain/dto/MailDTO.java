package com.example.example.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailDTO {
    private String title;
    private String content;
    private String sender;
    private String receiver;
}
