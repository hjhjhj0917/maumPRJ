package com.example.maum.account.repository;

import com.example.maum.account.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, Integer> {

    Optional<UserInfoEntity> findByUserId(String userId);

    Optional<UserInfoEntity> findByEmail(String Email);

    Optional<UserInfoEntity> findByUserIdAndPassword(String userId, String password);
}
