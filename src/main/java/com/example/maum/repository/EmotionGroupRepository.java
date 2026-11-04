package com.example.maum.repository;

import com.example.maum.repository.entity.EmotionGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionGroupRepository extends JpaRepository<EmotionGroupEntity, String> {
}
