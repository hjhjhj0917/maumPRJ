package com.example.example.map.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping(value = "/map")
@RequiredArgsConstructor
public class MapController {

    @GetMapping(value = "centerMap")
    public String centerMap() {

        log.info("{}.centerMap Start!", this.getClass().getName());
        log.info("{}.centerMap End!", this.getClass().getName());

        return "map/center-map";
    }
}
