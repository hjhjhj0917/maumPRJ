package com.example.maum.controller;

import com.example.maum.dto.MentalInstDTO;
import com.example.maum.service.IMentalInstService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/map")
@RequiredArgsConstructor
public class MapController {

    private final IMentalInstService mentalInstService;

    @GetMapping("/institutions")
    public List<MentalInstDTO> getInstitutions() throws Exception {

        log.info("{}.getInstitutions Start!", this.getClass().getName());

        List<MentalInstDTO> rList = Optional.ofNullable(mentalInstService.getAllInstitutions())
                        .orElseGet(ArrayList::new);

        log.info("{}.getInstitutions End!", this.getClass().getName());

        return rList;
    }
}
