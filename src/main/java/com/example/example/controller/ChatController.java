package com.example.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping(value = "/chat")
@RequiredArgsConstructor
public class ChatController {

    @GetMapping(value = "chat")
    public String chat() {

        log.info("{}.chat Start!", this.getClass().getName());
        log.info("{}.chat End!", this.getClass().getName());

        return "chat/chat";
    }
}
