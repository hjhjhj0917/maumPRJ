package com.example.maum.service;

import com.example.maum.dto.MentalInstDTO;

import java.util.List;

public interface IMentalInstService {

    List<MentalInstDTO> getAllInstitutions() throws Exception;
}
