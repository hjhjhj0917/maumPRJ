package com.example.maum.repository;

import com.example.maum.repository.entity.EmotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionRepository extends JpaRepository<EmotionEntity, Short> {
}
