package com.example.maum.repository;

import com.example.maum.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    Optional<UserInfoEntity> findByUserId(String userId);

    Optional<UserInfoEntity> findByUserNo(String userNo);

    Optional<UserInfoEntity> findByEmail(String Email);

    Optional<UserInfoEntity> findByUserIdAndPassword(String userId, String password);

    Optional<UserInfoEntity> findByEmailAndUserName(String email, String userName);

    Optional<UserInfoEntity> findByEmailAndUserId(String email, String userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE USER_INFO SET PROFILE_IMG_URL = :profileImgUrl WHERE USER_NO = :userNo", nativeQuery = true)
    int updateProfileImgDirectly(@Param("userNo") String userNo, @Param("profileImgUrl") String profileImgUrl);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE USER_INFO SET PASSWORD = :password WHERE USER_NO = :userNo", nativeQuery = true)
    int updatePasswordDirectly(@Param("userNo") String userNo, @Param("password") String password);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE USER_INFO SET EMAIL = :email WHERE USER_NO = :userNo", nativeQuery = true)
    int updateEmailDirectly(@Param("userNo") String userNo, @Param("email") String email);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE USER_INFO SET ADDR = :addr, DETAIL_ADDR = :detailAddr WHERE USER_NO = :userNo", nativeQuery = true)
    int updateAddressDirectly(@Param("userNo") String userNo, @Param("addr") String addr, @Param("detailAddr") String detailAddr);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE USER_INFO SET USER_STATUS = :userStatus WHERE USER_NO = :userNo", nativeQuery = true)
    int updateUserStatusDirectly(@Param("userNo") String userNo, @Param("userStatus") String userStatus);
}
