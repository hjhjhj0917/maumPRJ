package com.example.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    @GetMapping(value = "/")
    public String index() {

        log.info("{}.index Start!", this.getClass().getName());
        log.info("{}.index End!", this.getClass().getName());

        return "index";
    }
}
