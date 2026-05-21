package com.example.maum.service.impl;

import com.example.maum.auth.AuthInfo;
import com.example.maum.dto.ExistsDTO;
import com.example.maum.dto.MailDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.UserInfoDTO;
import com.example.maum.repository.UserInfoRepository;
import com.example.maum.repository.entity.UserInfoEntity;
import com.example.maum.service.IUserInfoService;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.EncryptUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoService implements IUserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final MailService mailService;
    private final RedisService redisService;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Value("${secure.jwt.token.access.name}")
    private String accessCookieName;

    @Value("${secure.jwt.token.refresh.name}")
    private String refreshCookieName;


    /* [Account Management] */

    @Transactional
    @Override
    public int insertUserInfo(UserInfoDTO pDTO) {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        int res;

        log.info("pDTO: {}", pDTO);

        try {
            boolean exists = userInfoRepository.findByUserId(pDTO.userId()).isPresent();

            if (exists) {
                res = 2;
            } else {
                UserInfoEntity pEntity = UserInfoDTO.of(pDTO);

                userInfoRepository.save(pEntity);

                res = 1;
            }
        } catch (Exception e) {
            log.error("insertUserInfo error", e);
            res = 0;
        }

        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return res;
    }


    @Override
    public UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserInfo Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(pDTO.userNo());

        log.info("userNo: {}", userNo);

        UserInfoDTO rDTO = UserInfoDTO.from(userInfoRepository.findById(userNo).orElseThrow());

        log.info("{}.getUserInfo End!", this.getClass().getName());

        return rDTO;
    }


    @Transactional(readOnly = true)
    @Override
    public UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        UserInfoDTO rDTO = userInfoRepository.findByUserId(pDTO.userId())
                .map(e -> UserInfoDTO.builder()
                        .existsYn("Y")
                        .build())
                .orElseGet(() -> UserInfoDTO.builder()
                        .existsYn("N")
                        .build());

        log.info("exists: {}", rDTO.existsYn());

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return rDTO;
    }


    /* [Authentication & Verification] */

    @Override
    public MsgDTO verifyEmailCode(UserInfoDTO pDTO) throws Exception {

        log.info("{}.verifyEmailCode Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String code = CmmUtil.nvl(pDTO.code());

        String encEmail = EncryptUtil.encAES128BCBC(email);
        String redisKey = "AUTH:" + encEmail;

        String storedAuthCode = redisService.getValues(redisKey);

        MsgDTO rDTO;

        if (storedAuthCode != null && storedAuthCode.equals(code)) {
            rDTO = MsgDTO.builder()
                    .result(1)
                    .msg("인증에 성공하였습니다.")
                    .build();
        } else {
            rDTO = MsgDTO.builder()
                    .result(0)
                    .msg("인증번호가 일치하지 않거나 만료되었습니다.")
                    .build();
        }

        log.info("{}.verifyEmailCode End!", this.getClass().getName());

        return rDTO;
    }


    @Override
    public List<ResponseCookie> logout(String accessToken, String userNo) throws Exception {

        log.info("{}.logout Start!", this.getClass().getName());

        if (accessToken != null) {
            long atExpirationMillis = 3600000L;
            redisService.setValues("AT:" + accessToken, "logout", atExpirationMillis);
            log.info("Access Token 블랙리스트 등록: {}", accessToken);
        }

        if (userNo != null) {
            redisService.deleteValues("RT:" + userNo);
            log.info("Refresh Token 삭제: {}", userNo);
        }

        ResponseCookie accessCookie = ResponseCookie.from(accessCookieName, "")
                .maxAge(0).path("/").httpOnly(true).secure(true).sameSite("Lax").build();

        ResponseCookie refreshCookie = ResponseCookie.from(refreshCookieName, "")
                .maxAge(0).path("/").httpOnly(true).secure(true).sameSite("Lax").build();

        List<ResponseCookie> rList = List.of(accessCookie, refreshCookie);

        log.info("{}.logout End!", this.getClass().getName());

        return rList;
    }


    /* [Account Recovery] */

    @Override
    public UserInfoDTO getUserId(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userName = CmmUtil.nvl(pDTO.userName());
        String code = CmmUtil.nvl(pDTO.code());

        String redisKey = "AUTH:" + EncryptUtil.encAES128BCBC(email);
        String storedAuthCode = redisService.getValues(redisKey);

        log.info("email: {}, code: {}, storedAuthCode: {}", email, code, storedAuthCode);

        UserInfoDTO rDTO = UserInfoDTO.builder().build();

        if (storedAuthCode != null && storedAuthCode.equals(code)) {
            UserInfoDTO searchDTO = UserInfoDTO.builder()
                    .email(EncryptUtil.encAES128BCBC(email))
                    .userName(userName)
                    .build();

            Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmailAndUserName(
                    searchDTO.email(), searchDTO.userName());

            if (rEntity.isPresent()) {
                rDTO = UserInfoDTO.builder()
                        .userId(rEntity.get().getUserId())
                        .build();

                redisService.deleteValues(redisKey);
                log.info("인증 성공 및 Redis 인증번호 삭제 완료: {}", redisKey);
            }
        }

        log.info("{}.getUserId End!", this.getClass().getName());

        return rDTO;
    }


    @Override
    public ExistsDTO findUserId(UserInfoDTO pDTO) throws Exception {

        log.info("{}.findUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userName = CmmUtil.nvl(pDTO.userName());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmailAndUserName(email, userName);

        boolean exists = rEntity.isPresent();
        int authNumber = 0;

        if (exists) {
            authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);

            MailDTO mailDTO = new MailDTO();
            mailDTO.setTitle("아이디 찾기 인증번호 발송 메일");
            mailDTO.setContent("인증번호는 " + authNumber + " 입니다.");
            mailDTO.setReceiver(EncryptUtil.decAES128BCBC(email));
            mailService.doSendMail(mailDTO);

            redisService.setValues("AUTH:" + email, String.valueOf(authNumber), 180000L);
            log.info("아이디 찾기 Redis 저장 완료: AUTH:{}", email);
        }

        ExistsDTO rDTO = ExistsDTO.builder()
                .exists(exists)
                .authNumber(authNumber)
                .build();

        log.info("{}.findUserId End!", this.getClass().getName());

        return rDTO;
    }


    @Override
    public ExistsDTO findUserPw(UserInfoDTO pDTO) throws Exception {

        log.info("{}.findUserPw Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userId = CmmUtil.nvl(pDTO.userId());
        String encUserId = EncryptUtil.encAES128BCBC(userId);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmailAndUserId(email, userId);

        boolean exists = rEntity.isPresent();
        int authNumber = 0;

        if (exists) {
            authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);

            MailDTO mailDTO = new MailDTO();
            mailDTO.setTitle("비밀번호 찾기 인증번호 발송 메일");
            mailDTO.setContent("인증번호는 " + authNumber + " 입니다.");
            mailDTO.setReceiver(EncryptUtil.decAES128BCBC(email));
            mailService.doSendMail(mailDTO);

            redisService.setValues("AUTH:" + email, String.valueOf(authNumber), 180000L);
            redisService.setValues("PW_RESET:" + email, encUserId, 180000L);
            log.info("비밀번호 찾기 Redis 저장 완료: AUTH:{}, PW_RESET:{}", email, email);
        }

        ExistsDTO rDTO = ExistsDTO.builder()
                .exists(exists)
                .authNumber(authNumber)
                .build();

        log.info("{}.findUserPw End!", this.getClass().getName());

        return rDTO;
    }


    @Override
    public ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmail(email);
        boolean exists = rEntity.isPresent();
        int authNumber = 0;

        if (!exists) {
            authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);

            MailDTO mailDTO = new MailDTO();
            mailDTO.setTitle("이메일 중복 확인 인증번호 발송 메일");
            mailDTO.setContent("인증번호는 " + authNumber + " 입니다.");
            mailDTO.setReceiver(EncryptUtil.decAES128BCBC(email));
            mailService.doSendMail(mailDTO);

            redisService.setValues("AUTH:" + email, String.valueOf(authNumber), 180000L);

            log.info("Redis에 인증번호 저장 완료 (3분): AUTH:{}", email);
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

        String userNo = CmmUtil.nvl(pDTO.userNo());
        String pProfileImageUrl = CmmUtil.nvl(pDTO.profileImgUrl());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserNo(userNo);

        int res;
        if (rEntity.isPresent()) {
            UserInfoEntity entity = rEntity.get();
            String rProfileImgUrl = pProfileImageUrl;

            if (pProfileImageUrl != null && pProfileImageUrl.startsWith("/images/account/profile") && pProfileImageUrl.endsWith(".png")) {
                rProfileImgUrl = pProfileImageUrl;
            }

            entity.updateProfileImg(rProfileImgUrl);
            res = 1;
        } else {
            res = 0;
        }

        log.info("{}.updateProfileImg End!", this.getClass().getName());

        return res;
    }


    @Transactional
    @Override
    public int updatePassword(UserInfoDTO pDTO) throws Exception {

        log.info("{}.updatePassword Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String password = CmmUtil.nvl(pDTO.password());
        String code = CmmUtil.nvl(pDTO.code());

        String encEmail = EncryptUtil.encAES128BCBC(email);

        String storedCode = redisService.getValues("AUTH:" + encEmail);
        String targetUserId = redisService.getValues("PW_RESET:" + encEmail);

        int res = 0;

        if (storedCode != null && storedCode.equals(code) && targetUserId != null) {

            String userId = EncryptUtil.decAES128BCBC(targetUserId);
            Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

            if (rEntity.isPresent()) {
                rEntity.get().updatePassword(bCryptPasswordEncoder.encode(password));
                res = 1;

                redisService.deleteValues("AUTH:" + encEmail);
                redisService.deleteValues("PW_RESET:" + encEmail);
                log.info("비밀번호 변경 성공 및 Redis 데이터 초기화 완료: {}", userId);
            }
        } else {
            log.warn("비밀번호 변경 실패: 인증번호 불일치 혹은 만료 (email: {})", email);
        }

        log.info("{}.updatePassword End!", this.getClass().getName());

        return res;
    }


    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        log.info("{}.loadUserByUsername Start!", this.getClass().getName());

        log.info("userId: {}", userId);

        UserInfoEntity rEntity = userInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId + " Not Found User"));

        UserInfoDTO rDTO = UserInfoDTO.from(rEntity);

        UserDetails res = new AuthInfo(rDTO);

        log.info("{}.loadUserByUsername End!", this.getClass().getName());

        return res;
    }

}