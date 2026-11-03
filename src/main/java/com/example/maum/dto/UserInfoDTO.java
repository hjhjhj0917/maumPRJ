package com.example.maum.dto;

import com.example.maum.auth.UserStatus;
import com.example.maum.repository.entity.UserEntity;
import com.example.maum.util.CmmUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserInfoDTO(

        // 외부 식별자(users.public_id). JWT subject 로도 쓰임
        String publicId,

        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "유효한 이메일 형식이 아닙니다.")
        @Size(max = 254, message = "이메일은 최대 254글자까지 입력가능합니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp = UserInfoDTO.PASSWORD_REGEX, message = "영문, 숫자, 특수문자를 모두 포함하여 8~20자리로 조합해주세요.")
        String password,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Pattern(regexp = UserInfoDTO.NICKNAME_REGEX, message = "닉네임은 2~10자의 한글, 영문, 숫자만 가능합니다.")
        String nickname,

        @NotNull(message = "출생연도를 입력해주세요.")
        @Min(value = UserInfoDTO.MIN_BIRTH_YEAR, message = "올바른 출생연도가 아닙니다.")
        @Max(value = 2100, message = "올바른 출생연도가 아닙니다.")
        Integer birthYear,

        String profileImgUrl,
        String status,
        String createdAt,
        String roles,
        String code,

        String existsYn) {

        // 서비스의 수정 검증에서도 같은 규칙을 쓰려고 상수로 둠
        public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";
        public static final String NICKNAME_REGEX = "^[가-힣a-zA-Z0-9]{2,10}$";
        public static final int MIN_BIRTH_YEAR = 1900;

        public static UserInfoDTO createUser(UserInfoDTO pDTO, String encodedPassword, String roles) {

                return UserInfoDTO.builder()
                        .email(CmmUtil.normalizeEmail(pDTO.email()))
                        .password(encodedPassword)
                        .nickname(pDTO.nickname())
                        .birthYear(pDTO.birthYear())
                        .profileImgUrl(pDTO.profileImgUrl())
                        .roles(roles)
                        .build();
        }

        // 가입 직후 상태는 ACTIVE. 호출 전에 이메일 인증을 통과했는지 서비스가 확인함
        public static UserEntity of(UserInfoDTO dto) {

                return UserEntity.builder()
                        .publicId(UUID.randomUUID())
                        .email(dto.email())
                        .emailVerifiedAt(Instant.now())
                        .nickname(dto.nickname())
                        .birthYear(dto.birthYear())
                        .profileImageKey(dto.profileImgUrl())
                        .role(dto.roles())
                        .status(UserStatus.ACTIVE)
                        .build();
        }

        // 비밀번호는 응답에 실리지 않도록 담지 않음. 로그인 검증용으로는 toBuilder().password(..) 를 씀
        public static UserInfoDTO from(UserEntity entity) {

                return UserInfoDTO.builder()
                        .publicId(entity.getPublicId().toString())
                        .email(entity.getEmail())
                        .nickname(entity.getNickname())
                        .birthYear(entity.getBirthYear())
                        .profileImgUrl(entity.getProfileImageKey())
                        .status(entity.getStatus().name())
                        .createdAt(Optional.ofNullable(entity.getCreatedAt()).map(Instant::toString).orElse(null))
                        .roles(entity.getRole())
                        .build();
        }

}
