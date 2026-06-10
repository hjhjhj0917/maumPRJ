package com.example.maum.dto;

import com.example.maum.repository.entity.UserInfoEntity;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.EncryptUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserInfoDTO(

        String userNo,

        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(min = 4, max = 20, message = "아이디는 4~20자리로 입력해주세요.")
        String userId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$", message = "영문, 숫자, 특수문자를 모두 포함하여 8~20자리로 조합해주세요.")
        String password,

        @NotBlank(message = "이름을 입력해주세요.")
        @Pattern(regexp = "^[가-힣a-zA-Z]{2,10}$", message = "이름은 2~10자의 한글 또는 영문만 가능합니다.")
        String userName,

        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "유효한 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 최대 255글자까지 입력가능합니다.")
        String email,

        @NotBlank(message = "생년월일을 입력해주세요.")
        @Pattern(regexp = "^(19|20)\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$", message = "올바른 날짜 형식이 아닙니다.")
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

        /* 회원 정보와 암호화된 비밀번호 그리고 역할을 부여받아서 DTO를 다시 반환함 +*/
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

        /* DTO를 Entity로 변환 */
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

        /* Entity를 DTO로 변환 */
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