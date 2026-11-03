package com.example.maum.service.impl;

import com.example.maum.auth.AuthInfo;
import com.example.maum.auth.AuthProvider;
import com.example.maum.auth.UserStatus;
import com.example.maum.dto.ExistsDTO;
import com.example.maum.dto.MailDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.UserInfoDTO;
import com.example.maum.repository.UserAuthProviderRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.repository.entity.UserAuthProviderEntity;
import com.example.maum.repository.entity.UserEntity;
import com.example.maum.service.IUserInfoService;
import com.example.maum.util.CmmUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoService implements IUserInfoService {

    private static final long AUTH_CODE_TTL_MS = 180000L;
    private static final long VERIFIED_EMAIL_TTL_MS = 1800000L;
    private static final long WITHDRAW_GRACE_DAYS = 14L;

    private final UserRepository userRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final MailService mailService;
    private final RedisService redisService;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Value("${secure.jwt.token.access.name}")
    private String accessCookieName;

    @Value("${secure.jwt.token.refresh.name}")
    private String refreshCookieName;


    /* [Account Management] */

    /**
     * @return 1 가입 성공, 2 이미 가입된 이메일, 3 이메일 인증 미완료, 0 오류
     */
    @Transactional
    @Override
    public int insertUserInfo(UserInfoDTO pDTO) {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        int res;

        String email = CmmUtil.normalizeEmail(pDTO.email());
        String verifiedKey = "VERIFIED:" + email;

        log.info("email: {}", CmmUtil.maskEmail(email));

        try {
            if (redisService.getValues(verifiedKey) == null) {
                log.warn("가입 거절: 이메일 인증 기록 없음 ({})", CmmUtil.maskEmail(email));
                res = 3;
            } else if (userRepository.existsByEmail(email)) {
                res = 2;
            } else {
                UserEntity user = userRepository.save(UserInfoDTO.of(pDTO));

                userAuthProviderRepository.save(UserAuthProviderEntity.builder()
                        .userId(user.getId())
                        .provider(AuthProvider.LOCAL)
                        .providerUserId(user.getPublicId().toString())
                        .passwordHash(pDTO.password())
                        .build());

                redisService.deleteValues(verifiedKey);

                res = 1;
            }
        } catch (Exception e) {
            log.error("insertUserInfo error", e);
            res = 0;
        }

        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return res;
    }


    @Transactional(readOnly = true)
    @Override
    public UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserInfo Start!", this.getClass().getName());

        String publicId = CmmUtil.nvl(pDTO.publicId());

        log.info("publicId: {}", publicId);

        UserInfoDTO rDTO = UserInfoDTO.from(findUser(publicId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다.")));

        log.info("{}.getUserInfo End!", this.getClass().getName());

        return rDTO;
    }


    /* [Authentication & Verification] */

    @Override
    public MsgDTO verifyEmailCode(UserInfoDTO pDTO) throws Exception {

        log.info("{}.verifyEmailCode Start!", this.getClass().getName());

        String email = CmmUtil.normalizeEmail(pDTO.email());
        String code = CmmUtil.nvl(pDTO.code());

        // Redis 키는 평문 이메일을 그대로 씀 — 발송 시점과 확인 시점에 같은 키를 만들어야 하기 때문
        String redisKey = "AUTH:" + email;
        String pwResetKey = "PW_RESET:" + email;

        String storedAuthCode = redisService.getValues(redisKey);

        MsgDTO rDTO;

        if (storedAuthCode != null && storedAuthCode.equals(code)) {
            rDTO = MsgDTO.builder()
                    .result(1)
                    .msg("인증에 성공하였습니다.")
                    .build();

            // 회원가입 시 서버가 인증 여부를 다시 확인할 수 있도록 통과 기록을 남김
            redisService.setValues("VERIFIED:" + email, "Y", VERIFIED_EMAIL_TTL_MS);

            if (redisService.getValues(pwResetKey) == null) {
                redisService.deleteValues(redisKey);
                log.info("인증 성공: Redis 인증번호 즉시 삭제 완료 (일반 인증)");
            } else {
                log.info("인증 성공: 비밀번호 변경 프로세스이므로 AUTH 키 유지");
            }

        } else {
            rDTO = MsgDTO.builder()
                    .result(0)
                    .msg("인증번호가 일치하지 않거나 만료되었습니다.")
                    .build();
        }

        log.info("{}.verifyEmailCode End!", this.getClass().getName());

        return rDTO;
    }


    @Transactional
    @Override
    public int updateLastLogin(UserInfoDTO pDTO) {

        log.info("{}.updateLastLogin Start!", this.getClass().getName());

        int res = parsePublicId(pDTO.publicId())
                .map(publicId -> userAuthProviderRepository.updateLastLoginAt(publicId, AuthProvider.LOCAL, Instant.now()))
                .orElse(0);

        log.info("{}.updateLastLogin End!", this.getClass().getName());

        return res;
    }


    @Override
    public List<ResponseCookie> logout(String accessToken, String publicId, long remainingMilliSeconds) throws Exception {

        log.info("{}.logout Start!", this.getClass().getName());

        if (accessToken != null && remainingMilliSeconds > 0) {
            redisService.setValues("AT:" + accessToken, "logout", remainingMilliSeconds);
            log.info("Access Token 블랙리스트 등록 (유지시간: {}ms)", remainingMilliSeconds);
        }

        if (publicId != null) {
            redisService.deleteValues("RT:" + publicId);
            log.info("Refresh Token 삭제: {}", publicId);

            redisService.deleteValues("diaryCache::" + publicId);
            log.info("일기 최신 리스트 삭제: {}", publicId);

            redisService.deleteValues("chat:" + publicId);
            log.info("챗봇 대화 내역 삭제 완료: {}", publicId);
        }

        ResponseCookie accessCookie = ResponseCookie.from(accessCookieName, "")
                .maxAge(0).path("/").httpOnly(true).secure(true).sameSite("Lax").build();

        ResponseCookie refreshCookie = ResponseCookie.from(refreshCookieName, "")
                .maxAge(0).path("/").httpOnly(true).secure(true).sameSite("Lax").build();

        ResponseCookie loginFlagCookie = ResponseCookie.from("isLoggedIn", "")
                .maxAge(0).path("/").httpOnly(false).secure(false).sameSite("Lax").build();

        List<ResponseCookie> rList = List.of(accessCookie, refreshCookie, loginFlagCookie);

        log.info("{}.logout End!", this.getClass().getName());

        return rList;
    }


    /* [Account Recovery] */

    // 메일 발송(외부 통신) 동안 DB 커넥션을 잡고 있지 않도록 트랜잭션을 걸지 않음
    @Override
    public ExistsDTO findUserPw(UserInfoDTO pDTO) throws Exception {

        log.info("{}.findUserPw Start!", this.getClass().getName());

        String email = CmmUtil.normalizeEmail(pDTO.email());

        // 활성 계정이면서 비밀번호 로그인(LOCAL)을 쓰는 계정만 재설정 대상
        Optional<UserEntity> rEntity = userRepository.findByEmail(email)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> userAuthProviderRepository.existsByUserIdAndProvider(user.getId(), AuthProvider.LOCAL));

        boolean exists = rEntity.isPresent();

        int authNumber = 0;

        if (exists) {
            authNumber = sendAuthCode("비밀번호 찾기 인증번호 발송 메일", email);

            // 확인 단계인 updatePassword 가 어느 계정의 비밀번호를 바꿀지 알 수 있도록 함께 저장
            redisService.setValues("PW_RESET:" + email, rEntity.get().getPublicId().toString(), AUTH_CODE_TTL_MS);
            log.info("비밀번호 찾기 Redis 저장 완료: PW_RESET:{}", CmmUtil.maskEmail(email));
        }

        ExistsDTO rDTO = ExistsDTO.builder()
                .exists(exists)
                .authNumber(authNumber)
                .build();

        log.info("{}.findUserPw End!", this.getClass().getName());

        return rDTO;
    }


    // 메일 발송(외부 통신) 동안 DB 커넥션을 잡고 있지 않도록 트랜잭션을 걸지 않음
    @Override
    public ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.normalizeEmail(pDTO.email());

        boolean exists = userRepository.existsByEmail(email);
        int authNumber = 0;

        if (!exists) {
            authNumber = sendAuthCode("이메일 중복 확인 인증번호 발송 메일", email);
        }

        ExistsDTO rDTO = ExistsDTO.builder()
                .exists(exists)
                .authNumber(authNumber)
                .build();

        log.info("{}.getEmailExists End!", this.getClass().getName());

        return rDTO;
    }


    /* [Profile & Security] */

    @Transactional
    @Override
    public int updateProfileImg(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.updateProfileImg Start!", this.getClass().getName());

        String pProfileImageUrl = CmmUtil.nvl(pDTO.profileImgUrl());

        int res = 0;

        Optional<UserEntity> rEntity = findUser(pDTO.publicId());

        if (rEntity.isPresent()) {
            rEntity.get().changeProfileImage(pProfileImageUrl);
            res = 1;
        }

        log.info("{}.updateProfileImg End!", this.getClass().getName());

        return res;
    }


    @Transactional
    @Override
    public int updatePassword(UserInfoDTO pDTO) throws Exception {

        log.info("{}.updatePassword Start!", this.getClass().getName());

        String email = CmmUtil.normalizeEmail(pDTO.email());
        String password = CmmUtil.nvl(pDTO.password());
        String code = CmmUtil.nvl(pDTO.code());

        // findUserPw 가 저장할 때도 평문 이메일로 키를 만들므로 여기도 동일하게 평문으로 조회함
        String storedCode = redisService.getValues("AUTH:" + email);
        String targetPublicId = redisService.getValues("PW_RESET:" + email);

        int res = 0;

        if (!password.matches(UserInfoDTO.PASSWORD_REGEX)) {
            log.warn("비밀번호 변경 실패: 비밀번호 형식 오류 (email: {})", CmmUtil.maskEmail(email));

        } else if (storedCode != null && storedCode.equals(code) && targetPublicId != null) {

            Optional<UserAuthProviderEntity> rAuth = parsePublicId(targetPublicId)
                    .flatMap(publicId -> userAuthProviderRepository.findByPublicIdAndProvider(publicId, AuthProvider.LOCAL));

            if (rAuth.isPresent()) {
                rAuth.get().changePasswordHash(bCryptPasswordEncoder.encode(password));
                res = 1;

                redisService.deleteValues("AUTH:" + email);
                redisService.deleteValues("PW_RESET:" + email);
                log.info("비밀번호 변경 성공 및 Redis 데이터 초기화 완료: {}", CmmUtil.maskEmail(email));
            }
        } else {
            log.warn("비밀번호 변경 실패: 인증번호 불일치 혹은 만료 (email: {})", CmmUtil.maskEmail(email));
        }

        log.info("{}.updatePassword End!", this.getClass().getName());

        return res;
    }


    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String loginEmail) throws UsernameNotFoundException {

        log.info("{}.loadUserByUsername Start!", this.getClass().getName());

        String email = CmmUtil.normalizeEmail(loginEmail);

        log.info("email: {}", CmmUtil.maskEmail(email));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(CmmUtil.maskEmail(email) + " Not Found User"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("로그인할 수 없는 계정입니다. (status: " + user.getStatus() + ")");
        }

        UserAuthProviderEntity auth = userAuthProviderRepository.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .orElseThrow(() -> new UsernameNotFoundException("비밀번호 로그인을 사용할 수 없는 계정입니다."));

        UserInfoDTO rDTO = UserInfoDTO.from(user).toBuilder()
                .password(auth.getPasswordHash())
                .build();

        UserDetails res = new AuthInfo(rDTO);

        log.info("{}.loadUserByUsername End!", this.getClass().getName());

        return res;
    }


    @Transactional(readOnly = true)
    @Override
    public MsgDTO verifyCurrentPassword(UserInfoDTO pDTO) throws Exception {

        log.info("{}.verifyCurrentPassword Start!", this.getClass().getName());

        String password = CmmUtil.nvl(pDTO.password());

        Optional<UserAuthProviderEntity> rAuth = parsePublicId(pDTO.publicId())
                .flatMap(publicId -> userAuthProviderRepository.findByPublicIdAndProvider(publicId, AuthProvider.LOCAL));

        MsgDTO rDTO;
        if (rAuth.isPresent() && bCryptPasswordEncoder.matches(password, rAuth.get().getPasswordHash())) {
            rDTO = MsgDTO.builder().result(1).msg("비밀번호가 일치합니다.").build();
        } else {
            rDTO = MsgDTO.builder().result(0).msg("비밀번호가 일치하지 않습니다.").build();
        }

        log.info("{}.verifyCurrentPassword End!", this.getClass().getName());

        return rDTO;
    }


    /**
     * 비밀번호, 닉네임, 출생연도 중 값이 온 것만 바꿈. 이메일은 변경을 지원하지 않음
     */
    @Transactional
    @Override
    public int updateAccount(UserInfoDTO pDTO) throws Exception {

        log.info("{}.updateAccount Start!", this.getClass().getName());

        String password = CmmUtil.nvl(pDTO.password());
        String nickname = CmmUtil.nvl(pDTO.nickname());
        Integer birthYear = pDTO.birthYear();

        boolean valid = (password.isEmpty() || password.matches(UserInfoDTO.PASSWORD_REGEX))
                && (nickname.isEmpty() || nickname.matches(UserInfoDTO.NICKNAME_REGEX))
                && (birthYear == null || (birthYear >= UserInfoDTO.MIN_BIRTH_YEAR && birthYear <= Year.now().getValue()));

        int updatedCount = 0;

        Optional<UserEntity> rEntity = findUser(pDTO.publicId());

        if (!valid) {
            log.warn("계정 수정 거절: 입력값 형식 오류");

        } else if (rEntity.isPresent()) {
            UserEntity user = rEntity.get();

            if (!nickname.isEmpty() || birthYear != null) {
                user.changeProfile(
                        nickname.isEmpty() ? user.getNickname() : nickname,
                        birthYear == null ? user.getBirthYear() : birthYear);
                updatedCount++;
            }

            if (!password.isEmpty()) {
                Optional<UserAuthProviderEntity> rAuth = userAuthProviderRepository
                        .findByPublicIdAndProvider(user.getPublicId(), AuthProvider.LOCAL);

                if (rAuth.isPresent()) {
                    rAuth.get().changePasswordHash(bCryptPasswordEncoder.encode(password));
                    updatedCount++;
                }
            }
        }

        int res = 0;
        if (updatedCount > 0) {
            res = 1;
        }

        log.info("{}.updateAccount End!", this.getClass().getName());

        return res;
    }


    // 메일 발송(외부 통신) 동안 DB 커넥션을 잡고 있지 않도록 트랜잭션을 걸지 않음
    @Override
    public MsgDTO sendWithdrawEmailCode(UserInfoDTO pDTO) throws Exception {

        log.info("{}.sendWithdrawEmailCode Start!", this.getClass().getName());

        MsgDTO rDTO;

        // 인증번호는 요청에 실린 주소가 아니라 DB에 저장된 본인 이메일로만 보냄
        Optional<UserEntity> rEntity = findUser(pDTO.publicId());

        if (rEntity.isPresent()) {
            sendAuthCode("회원 탈퇴 인증번호 발송 메일", rEntity.get().getEmail());

            rDTO = MsgDTO.builder().result(1).msg("인증 코드가 발송되었습니다.").build();
        } else {
            rDTO = MsgDTO.builder().result(0).msg("인증번호 발송 중 오류가 발생했습니다.").build();
        }

        log.info("{}.sendWithdrawEmailCode End!", this.getClass().getName());

        return rDTO;
    }


    /**
     * 즉시 삭제하지 않고 탈퇴 유예 상태로 전환. 유예(14일)가 끝나면 UserCleanupScheduler 가 삭제함
     */
    @Transactional
    @Override
    public int deleteUser(UserInfoDTO pDTO) throws Exception {

        log.info("{}.deleteUser Start!", this.getClass().getName());

        int res = 0;

        Optional<UserEntity> rEntity = findUser(pDTO.publicId());

        if (rEntity.isPresent()) {
            Instant now = Instant.now();

            rEntity.get().withdraw(now, now.plus(WITHDRAW_GRACE_DAYS, ChronoUnit.DAYS));
            res = 1;
        }

        log.info("{}.deleteUser End!", this.getClass().getName());

        return res;
    }


    /* [Private] */

    // 인증번호를 메일로 보내고 Redis(3분)에 저장한 뒤 번호를 돌려줌
    private int sendAuthCode(String title, String email) throws Exception {

        int authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);

        MailDTO mailDTO = MailDTO.builder()
                .title(title)
                .content("인증번호는 " + authNumber + " 입니다.")
                .receiver(email)
                .build();

        mailService.doSendMail(mailDTO);

        redisService.setValues("AUTH:" + email, String.valueOf(authNumber), AUTH_CODE_TTL_MS);
        log.info("Redis에 인증번호 저장 완료 (3분): AUTH:{}", CmmUtil.maskEmail(email));

        return authNumber;
    }

    // 형식이 틀린 publicId(예: 이전 버전 토큰의 숫자 subject)는 예외 대신 빈 값으로 처리함
    private Optional<UUID> parsePublicId(String publicId) {

        try {
            return Optional.of(UUID.fromString(CmmUtil.nvl(publicId)));
        } catch (IllegalArgumentException e) {
            log.warn("올바르지 않은 publicId 형식입니다.");
            return Optional.empty();
        }
    }

    private Optional<UserEntity> findUser(String publicId) {

        return parsePublicId(publicId).flatMap(userRepository::findByPublicId);
    }
}
