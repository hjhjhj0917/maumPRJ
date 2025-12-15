package com.example.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping(value = "/home")
public class HomeController {

    @GetMapping(value = "main")
    public String main() {

        log.info("{}.main Start!", this.getClass().getName());
        log.info("{}.main End!", this.getClass().getName());

        return "main";
    }
}
