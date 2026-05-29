package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryLogRepository extends MongoRepository<DiaryLogDocument, String> {

    void deleteByUserNo(Integer userNo);
}
