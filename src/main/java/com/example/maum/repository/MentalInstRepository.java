package com.example.maum.repository;

import com.example.maum.repository.entity.MentalInstDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentalInstRepository extends MongoRepository<MentalInstDocument, String> {
}