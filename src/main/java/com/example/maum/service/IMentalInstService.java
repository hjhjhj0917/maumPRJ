package com.example.maum.service;

import com.example.maum.repository.entity.MentalInstDocument;

import java.util.List;

public interface IMentalInstService {

    List<MentalInstDocument> getAllInstitutions() throws Exception;

    boolean clearInstitutionsCache();
}
