package com.example.maum.repository.entity;

import com.example.maum.auth.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class UserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 외부(JWT, API)에 노출하는 식별자. 내부 PK(id)는 밖으로 내보내지 않음
    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

    // 소문자·trim 으로 정규화된 값만 저장 (DB CHECK 와 동일 규칙)
    @Column(name = "email", nullable = false, length = 254, updatable = false)
    private String email;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "nickname", length = 30)
    private String nickname;

    @Column(name = "birth_year")
    private Integer birthYear;

    // NULL 이면 기본 이미지
    @Column(name = "profile_image_key", length = 500)
    private String profileImageKey;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;

    // 영구 삭제 예정 시각 (탈퇴 유예 만료)
    @Column(name = "purge_at")
    private Instant purgeAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // DB 트리거(set_updated_at)가 갱신하므로 애플리케이션에서는 쓰지 않음
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public void changeProfile(String nickname, Integer birthYear) {
        this.nickname = nickname;
        this.birthYear = birthYear;
    }

    public void changeProfileImage(String profileImageKey) {
        this.profileImageKey = profileImageKey;
    }

    // 탈퇴 유예 상태로 전환. DB 의 ck_users_withdrawal 제약상 withdrawn_at, purge_at 이 함께 있어야 함
    public void withdraw(Instant withdrawnAt, Instant purgeAt) {
        this.status = UserStatus.WITHDRAWN_PENDING;
        this.withdrawnAt = withdrawnAt;
        this.purgeAt = purgeAt;
    }
}
