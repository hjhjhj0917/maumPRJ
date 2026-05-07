package com.example.maum.dto;

import com.example.maum.repository.entity.UserInfoEntity;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.EncryptUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserInfoDTO(

        String userNo,

        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(min = 4, message = "아이디는 최소 4글자 이상 입력해주세요.")
        String userId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 4, message = "비밀번호는 최소 8글자 이상 입력해주세요.")
        String password,

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 10, message = "이름은 최대 10글자까지 입력가능합니다.")
        String userName,

        @NotBlank(message = "이메일을 입력해주세요.")
        @Size(max = 40, message = "이메일은 최대 40글자까지 입력가능합니다..")
        String email,

        @NotBlank(message = "생년월일을 입력해주세요.")
        String birthDate,

        @NotBlank(message = "주소를 입력해주세요.")
        String addr,

        String detailAddr,
        String profileImgUrl,
        String userStatus,
        String createdAt,
        String updatedAt,
        String roles,
        String code,

        String existsYn) {

        public static UserInfoDTO createUser(UserInfoDTO pDTO, String password, String roles) throws Exception {

                return UserInfoDTO.builder()
                        .userId(pDTO.userId())
                        .password(password)
                        .userName(pDTO.userName())
                        .email(EncryptUtil.encAES128BCBC(pDTO.email()))
                        .birthDate(pDTO.birthDate())
                        .addr(pDTO.addr())
                        .detailAddr(CmmUtil.nvl(pDTO.detailAddr()))
                        .profileImgUrl(pDTO.profileImgUrl())
                        .roles(roles)
                        .build();
        }

        public static UserInfoEntity of(UserInfoDTO dto) {

                return UserInfoEntity.builder()
                        .userId(dto.userId())
                        .password(dto.password())
                        .userName(dto.userName())
                        .email(dto.email())
                        .birthDate(dto.birthDate())
                        .addr(dto.addr())
                        .detailAddr(dto.detailAddr())
                        .profileImgUrl(dto.profileImgUrl())
                        .roles(dto.roles())
                        .build();
        }

        public static UserInfoDTO from(UserInfoEntity entity) throws Exception {

                return UserInfoDTO.builder()
                        .userNo(entity.getUserNo())
                        .userId(entity.getUserId())
                        .password(entity.getPassword())
                        .userName(entity.getUserName())
                        .email(EncryptUtil.decAES128BCBC(CmmUtil.nvl(entity.getEmail())))
                        .birthDate(entity.getBirthDate())
                        .addr(entity.getAddr())
                        .detailAddr(entity.getDetailAddr())
                        .profileImgUrl(entity.getProfileImgUrl())
                        .userStatus(entity.getUserStatus())
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .roles(entity.getRoles())
                        .build();
        }

}
