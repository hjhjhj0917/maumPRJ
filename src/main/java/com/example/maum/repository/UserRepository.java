package com.example.maum.repository;

import com.example.maum.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.maum.auth.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByPublicId(UUID publicId);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findByStatusAndPurgeAtLessThanEqual(UserStatus status, Instant purgeAt);

    // 탈퇴 유예가 끝난 계정을 한 번에 삭제. 하위 테이블은 DB의 ON DELETE CASCADE 가 정리함
    @Modifying
    @Query("DELETE FROM UserEntity u WHERE u.status = com.example.maum.auth.UserStatus.WITHDRAWN_PENDING AND u.purgeAt <= :now")
    int deleteWithdrawnUsersPurgedUntil(@Param("now") Instant now);
}
