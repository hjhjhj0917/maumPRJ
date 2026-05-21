package com.example.maum.repository;

import com.example.maum.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    /*
    아이디 중복 여부
    */
    Optional<UserInfoEntity> findByUserId(String userId);


    Optional<UserInfoEntity> findByUserNo(String userNo);

    /*
    이메일 중복 여부
    */
    Optional<UserInfoEntity> findByEmail(String Email);

    /*
    로그인
    */
    Optional<UserInfoEntity> findByUserIdAndPassword(String userId, String password);

    /*
    이메일과 이름으로 아이디 찾기
    */
    Optional<UserInfoEntity> findByEmailAndUserName(String email, String userName);

    /*
    이메일과 아이디로 회원 조회
    */
    Optional<UserInfoEntity> findByEmailAndUserId(String email, String userId);
}
