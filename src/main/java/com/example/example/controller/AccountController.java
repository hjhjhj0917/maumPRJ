package com.example.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping(value = "/account")
@RequiredArgsConstructor
public class AccountController {

    @GetMapping(value = "login")
    public String login() {

        log.info("{}.login Start!", this.getClass().getName());
        log.info("{}.login End!", this.getClass().getName());

        return "account/login";
    }

    @GetMapping(value = "find-id")
    public String findId() {

        log.info("{}.findId Start!", this.getClass().getName());
        log.info("{}.findId End!", this.getClass().getName());

        return "account/find-id";
    }

    @GetMapping(value = "find-pw")
    public String findPw() {

        log.info("{}.findPw Start!", this.getClass().getName());
        log.info("{}.findPw End!", this.getClass().getName());

        return "account/find-pw";
    }
}
