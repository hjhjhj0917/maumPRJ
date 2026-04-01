package com.example.maum.account.repository;

import com.example.maum.account.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, Integer> {

    // 아이디 중복 여부
    Optional<UserInfoEntity> findByUserId(String userId);

    // 이메일 중복 여부
    Optional<UserInfoEntity> findByEmail(String Email);

    // 로그인
    Optional<UserInfoEntity> findByUserIdAndPassword(String userId, String password);

    // 이메일과 이름으로 아이디 찾기
    Optional<UserInfoEntity> findByEmailAndUserName(String email, String userName);
}
