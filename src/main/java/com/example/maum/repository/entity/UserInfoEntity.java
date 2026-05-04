package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

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
    private String userNo;

    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "USER_NAME", nullable = false, length = 50)
    private String userName;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "BIRTH_DATE", nullable = false)
    private String birthDate;

    @Column(name = "ADDR", nullable = false, length = 255)
    private String addr;

    @Column(name = "DETAIL_ADDR", length = 255)
    private String detailAddr;

    @Column(name = "PROFILE_IMG_URL", nullable = false, length = 500)
    private String profileImgUrl;

    @Column(name = "USER_STATUS", length = 20)
    private String userStatus;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private String createdAt;

    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private String updatedAt;

    @Column(name = "ROLES")
    private String roles;

    public void updateProfileImg(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public void updatePassword(String password) { this.password = password; }
}