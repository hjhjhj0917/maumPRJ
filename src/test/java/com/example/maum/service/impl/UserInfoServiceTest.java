package com.example.maum.service.impl;

import com.example.maum.auth.AuthInfo;
import com.example.maum.auth.AuthProvider;
import com.example.maum.auth.UserStatus;
import com.example.maum.dto.UserInfoDTO;
import com.example.maum.repository.UserAuthProviderRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.repository.entity.UserAuthProviderEntity;
import com.example.maum.repository.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// DB·Redis·메일 없이 UserInfoService 의 판단 로직(가입 검증, 로그인 가능 상태, 탈퇴 유예, 입력 검증)만 확인함.
// 저장소는 Mockito 가짜로 대체하고, 트랜잭션·SQL 은 검증 대상이 아님
class UserInfoServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String VERIFIED_KEY = "VERIFIED:" + EMAIL;

    UserRepository userRepository = mock(UserRepository.class);
    UserAuthProviderRepository userAuthProviderRepository = mock(UserAuthProviderRepository.class);
    RedisService redisService = mock(RedisService.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    UserInfoService service;

    @BeforeEach
    void setUp() {
        service = new UserInfoService(userRepository, userAuthProviderRepository,
                mock(MailService.class), redisService, passwordEncoder);
    }

    private UserEntity user(UserStatus status) {
        return UserEntity.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .email(EMAIL)
                .nickname("마음이")
                .birthYear(1999)
                .role("USER")
                .status(status)
                .build();
    }

    private UserInfoDTO signupDTO() {
        // 컨트롤러가 이메일을 정규화하고 비밀번호를 해시한 뒤 서비스에 넘기는 모습과 같게 만듦
        return UserInfoDTO.createUser(
                UserInfoDTO.builder().email("  User@Example.com ").nickname("마음이").birthYear(1999).build(),
                "HASHED", "USER");
    }

    @Test
    void 이메일_인증_기록이_없으면_가입을_거절한다() {
        when(redisService.getValues(VERIFIED_KEY)).thenReturn(null);

        assertEquals(3, service.insertUserInfo(signupDTO()));

        verify(userRepository, never()).save(any());
    }

    @Test
    void 이미_가입된_이메일이면_가입을_거절한다() {
        when(redisService.getValues(VERIFIED_KEY)).thenReturn("Y");
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertEquals(2, service.insertUserInfo(signupDTO()));

        verify(userRepository, never()).save(any());
    }

    @Test
    void 가입하면_ACTIVE_사용자와_LOCAL_인증수단을_저장하고_인증기록을_지운다() {
        when(redisService.getValues(VERIFIED_KEY)).thenReturn("Y");
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(1, service.insertUserInfo(signupDTO()));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(EMAIL, userCaptor.getValue().getEmail());
        assertEquals(UserStatus.ACTIVE, userCaptor.getValue().getStatus());
        assertNotNull(userCaptor.getValue().getPublicId());

        ArgumentCaptor<UserAuthProviderEntity> authCaptor = ArgumentCaptor.forClass(UserAuthProviderEntity.class);
        verify(userAuthProviderRepository).save(authCaptor.capture());
        assertEquals(AuthProvider.LOCAL, authCaptor.getValue().getProvider());
        assertEquals("HASHED", authCaptor.getValue().getPasswordHash());
        assertEquals(userCaptor.getValue().getPublicId().toString(), authCaptor.getValue().getProviderUserId());

        verify(redisService).deleteValues(VERIFIED_KEY);
    }

    @Test
    void 탈퇴_유예_중인_계정은_로그인할_수_없다() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(UserStatus.WITHDRAWN_PENDING)));

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(EMAIL));
    }

    @Test
    void 로그인용_사용자정보는_이메일과_비밀번호_해시를_담는다() {
        UserEntity active = user(UserStatus.ACTIVE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(active));
        when(userAuthProviderRepository.findByUserIdAndProvider(1L, AuthProvider.LOCAL)).thenReturn(Optional.of(
                UserAuthProviderEntity.builder().userId(1L).provider(AuthProvider.LOCAL).passwordHash("HASHED").build()));

        // 대소문자·공백이 섞인 입력도 같은 계정으로 찾아야 함
        UserDetails details = service.loadUserByUsername(" User@Example.com ");

        assertEquals(EMAIL, details.getUsername());
        assertEquals("HASHED", details.getPassword());
        assertEquals(active.getPublicId().toString(), ((AuthInfo) details).userInfoDTO().publicId());
    }

    @Test
    void 탈퇴하면_14일_뒤_삭제되도록_유예_상태로_바뀐다() throws Exception {
        UserEntity active = user(UserStatus.ACTIVE);
        when(userRepository.findByPublicId(active.getPublicId())).thenReturn(Optional.of(active));

        int res = service.deleteUser(UserInfoDTO.builder().publicId(active.getPublicId().toString()).build());

        assertEquals(1, res);
        assertEquals(UserStatus.WITHDRAWN_PENDING, active.getStatus());
        assertEquals(Duration.ofDays(14), Duration.between(active.getWithdrawnAt(), active.getPurgeAt()));
        assertTrue(active.getWithdrawnAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void 형식이_틀린_닉네임으로는_계정을_수정하지_않는다() throws Exception {
        UserEntity active = user(UserStatus.ACTIVE);
        when(userRepository.findByPublicId(active.getPublicId())).thenReturn(Optional.of(active));

        int res = service.updateAccount(UserInfoDTO.builder()
                .publicId(active.getPublicId().toString())
                .nickname("가")
                .build());

        assertEquals(0, res);
        assertEquals("마음이", active.getNickname());
    }

    @Test
    void 닉네임과_출생연도를_수정하고_비워둔_값은_유지한다() throws Exception {
        UserEntity active = user(UserStatus.ACTIVE);
        when(userRepository.findByPublicId(active.getPublicId())).thenReturn(Optional.of(active));

        int res = service.updateAccount(UserInfoDTO.builder()
                .publicId(active.getPublicId().toString())
                .nickname("새이름")
                .build());

        assertEquals(1, res);
        assertEquals("새이름", active.getNickname());
        assertEquals(1999, active.getBirthYear());
        verify(userAuthProviderRepository, never()).findByPublicIdAndProvider(any(), ArgumentMatchers.any());
    }
}
