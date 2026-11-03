package com.example.maum.repository.entity;

import com.example.maum.auth.AuthProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_auth_provider")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class UserAuthProviderEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20, updatable = false)
    private AuthProvider provider;

    // LOCAL 은 users.public_id 문자열, 소셜은 제공자의 고유 ID(sub)
    @Column(name = "provider_user_id", nullable = false, length = 255, updatable = false)
    private String providerUserId;

    // LOCAL 일 때만 존재하는 BCrypt 해시
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
