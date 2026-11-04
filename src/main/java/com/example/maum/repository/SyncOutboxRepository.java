package com.example.maum.repository;

import com.example.maum.repository.entity.SyncOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncOutboxRepository extends JpaRepository<SyncOutboxEntity, Long> {
}
