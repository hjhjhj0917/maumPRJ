package com.example.maum.diary.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping(value = "/diary")
@RequiredArgsConstructor
public class DiaryController {

    @GetMapping(value = "write")
    public String write() {

        log.info("{}.write Start!", this.getClass().getName());
        log.info("{}.write End!", this.getClass().getName());

        return "diary/write";
    }

    @GetMapping(value = "list")
    public String list() {

        log.info("{}.list Start!", this.getClass().getName());
        log.info("{}.list End!", this.getClass().getName());

        return "diary/list";
    }

    @GetMapping(value = "analysis")
    public String analysis() {

        log.info("{}.analysis Start!", this.getClass().getName());
        log.info("{}.analysis End!", this.getClass().getName());

        return "diary/analysis";
    }
}
