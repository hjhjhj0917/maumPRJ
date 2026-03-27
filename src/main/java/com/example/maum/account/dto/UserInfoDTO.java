package com.example.maum.account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserInfoDTO(
        Integer userNo,
        String userId,
        String password,
        String userName,
        String email,
        String birthDate,
        String addr,
        String detailAddr,
        String profileImgUrl,
        String userStatus,
        String createdAt,
        String updatedAt
) {
}
