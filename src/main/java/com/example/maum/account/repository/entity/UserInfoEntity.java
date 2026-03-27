package com.example.maum.account.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_INFO")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class UserInfoEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO")
    private Integer userNo;

    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "USER_NAME", nullable = false, length = 50)
    private String userName;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "BIRTH_DATE", nullable = false)
    private LocalDate birthDate;

    @Column(name = "ADDR", nullable = false, length = 255)
    private String addr;

    @Column(name = "DETAIL_ADDR", length = 255)
    private String detailAddr;

    @Column(name = "PROFILE_IMG_URL", nullable = false, length = 500)
    private String profileImgUrl;

    @Column(name = "USER_STATUS", length = 20)
    private String userStatus;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}