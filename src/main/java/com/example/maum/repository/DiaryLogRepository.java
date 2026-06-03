package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryLogRepository extends MongoRepository<DiaryLogDocument, String> {

    void deleteByUserNo(Integer userNo);

    List<DiaryLogDocument> findByUserNo(Integer userNo);
}