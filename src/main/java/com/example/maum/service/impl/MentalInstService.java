package com.example.maum.service.impl;

import com.example.maum.dto.MentalInstDTO;
import com.example.maum.repository.MentalInstRepository;
import com.example.maum.service.IMentalInstService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MentalInstService implements IMentalInstService {

    private final MentalInstRepository repository;

    @Cacheable(value = "institutionsList")
    @Override
    public List<MentalInstDTO> getAllInstitutions() {

        log.info("{}.getAllInstitutions Start!", this.getClass().getName());

        List<MentalInstDTO> rList = repository.findAll();

        log.info("{}.getAllInstitutions End!", this.getClass().getName());

        return rList;
    }

}
