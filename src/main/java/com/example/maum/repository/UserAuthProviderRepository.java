package com.example.maum.repository;

import com.example.maum.auth.AuthProvider;
import com.example.maum.repository.entity.UserAuthProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProviderEntity, Long> {

    Optional<UserAuthProviderEntity> findByUserIdAndProvider(Long userId, AuthProvider provider);

    boolean existsByUserIdAndProvider(Long userId, AuthProvider provider);

    // users 를 따로 조회하지 않고 외부 식별자(public_id)로 한 번에 찾음
    @Query("SELECT a FROM UserAuthProviderEntity a, UserEntity u " +
            "WHERE a.userId = u.id AND u.publicId = :publicId AND a.provider = :provider")
    Optional<UserAuthProviderEntity> findByPublicIdAndProvider(@Param("publicId") UUID publicId,
                                                               @Param("provider") AuthProvider provider);

    // 로그인 성공 시각 기록. 엔티티를 읽지 않고 UPDATE 한 번으로 끝냄
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserAuthProviderEntity a SET a.lastLoginAt = :now " +
            "WHERE a.provider = :provider AND a.userId = (SELECT u.id FROM UserEntity u WHERE u.publicId = :publicId)")
    int updateLastLoginAt(@Param("publicId") UUID publicId,
                          @Param("provider") AuthProvider provider,
                          @Param("now") Instant now);
}
