package com.example.maum.account.service.impl;

import com.example.maum.account.dto.ExistsDTO;
import com.example.maum.account.dto.MailDTO;
import com.example.maum.account.dto.UserInfoDTO;
import com.example.maum.account.repository.UserInfoRepository;
import com.example.maum.account.repository.entity.UserInfoEntity;
import com.example.maum.account.service.IUserInfoService;
import com.example.maum.global.util.CmmUtil;
import com.example.maum.global.util.EncryptUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoService implements IUserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final MailService mailService;

    /*
    아이디 중복 확인
    */
    @Override
    public ExistsDTO getUserIdExists(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        log.info("pDTO: {}", pDTO);

        String userId = CmmUtil.nvl(pDTO.userId());

        boolean exists = userInfoRepository.findByUserId(userId).isPresent();

        ExistsDTO rDTO = ExistsDTO.builder()
                .exists(exists)
                .build();

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return rDTO;
    }

    /*
    이메일 중복 확인
    */
    @Override
    public ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());

        boolean exists = userInfoRepository.findByEmail(email).isPresent();
        int authNumber = 0;

        log.info("exists : {}", exists);

        if (!exists) {
            authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
            log.info("authNumber : {}", authNumber);

            MailDTO mailDTO = new MailDTO();
            mailDTO.setTitle("이메일 중복 확인 인증번호 발송 메일");
            mailDTO.setContent("인증번호는 " + authNumber + " 입니다. ");
            mailDTO.setReceiver(EncryptUtil.decAES128BCBC(email));

            mailService.doSendMail(mailDTO);
        }

        ExistsDTO rDTO = ExistsDTO.builder()
                .exists(exists)
                .authNumber(authNumber)
                .build();

        log.info("rDTO : {}", rDTO);

        log.info("{}.getEmailExists End!", this.getClass().getName());

        return rDTO;
    }

    /*
    회원가입
    */
    @Transactional
    @Override
    public int insertUserInfo(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        log.info("pDTO: {}", pDTO);

        int res;

        String userId = CmmUtil.nvl(pDTO.userId());
        String birthDate = CmmUtil.nvl(pDTO.birthDate());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            res = 2;
        } else {
            String profileImgUrl = "/images/account/profile1.png";

            if (birthDate.length() >= 4) {
                try {
                    int birthYear = Integer.parseInt(birthDate.substring(0, 4));

                    int zodiacNum = ((birthYear - 4) % 12) + 1;
                    if (zodiacNum < 1) {
                        zodiacNum += 12;
                    }

                    profileImgUrl = "/images/account/profile" + zodiacNum + ".png";

                } catch (NumberFormatException e) {
                    log.error("생년월일 파싱 오류: {}", e.getMessage());
                }
            }

            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userId(userId)
                    .password(CmmUtil.nvl(pDTO.password()))
                    .userName(CmmUtil.nvl(pDTO.userName()))
                    .email(CmmUtil.nvl(pDTO.email()))
                    .birthDate(LocalDate.parse(birthDate))
                    .addr(CmmUtil.nvl(pDTO.addr()))
                    .detailAddr(CmmUtil.nvl(pDTO.detailAddr()))
                    .profileImgUrl(profileImgUrl)
                    .build();

            userInfoRepository.save(pEntity);

            res = 1;
        }

        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return res;
    }

    /*
    회원 프로필 이미지 수정
    */
    @Transactional
    @Override
    public int updateProfileImg(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.updateProfileImg Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId());
        String profileImage = CmmUtil.nvl(pDTO.profileImgUrl());

        int res = userInfoRepository.findByUserId(userId)
                .map(entity -> {
                    String profileImgUrl = profileImage;

                    if (profileImage != null && profileImage.startsWith("/images/account/profile") && profileImage.endsWith(".png")) {
                        profileImgUrl = profileImage;
                    }

                    entity.updateProfileImg(profileImgUrl);
                    return 1;
                }).orElse(0);

        log.info("{}.updateProfileImg End!", this.getClass().getName());

        return res;
    }

    /*
    로그인
    */
    @Override
    public int getUserLogin(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserLogin Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId());
        String password = CmmUtil.nvl(pDTO.password());

        log.info("userId: {}, password: {}", userId, password);

        boolean res = userInfoRepository.findByUserIdAndPassword(userId, password).isPresent();

        log.info("{}.getUserLogin End!", this.getClass().getName());

        return res ? 1 : 0;
    }

    /*
    아이디 찾기
    */
    @Override
    public ExistsDTO findUserId(UserInfoDTO pDTO) throws Exception {

        log.info("{}.findUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userName = CmmUtil.nvl(pDTO.userName());

        boolean exists = userInfoRepository.findByEmailAndUserName(email, userName).isPresent();
        int authNumber = 0;

        log.info("exists : {}", exists);

        if (exists) {
            authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
            log.info("authNumber : {}", authNumber);

            MailDTO mailDTO = new MailDTO();
            mailDTO.setTitle("아이디 찾기 인증번호 발송 메일");
            mailDTO.setContent("인증번호는 " + authNumber + " 입니다. ");
            mailDTO.setReceiver(EncryptUtil.decAES128BCBC(email));

            mailService.doSendMail(mailDTO);
        }

        ExistsDTO rDTO = ExistsDTO.builder()
                .exists(exists)
                .authNumber(authNumber)
                .build();

        log.info("rDTO : {}", rDTO);

        log.info("{}.findUserId End!", this.getClass().getName());

        return rDTO;
    }

    /*
    메일과 이름으로 아이디 조회
    */
    @Override
    public UserInfoDTO getUserId(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userName = CmmUtil.nvl(pDTO.userName());

        UserInfoDTO rDTO = userInfoRepository.findByEmailAndUserName(email, userName)
                .map(entity -> UserInfoDTO.builder()
                        .userId(entity.getUserId())
                        .build())
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.getUserId End!", this.getClass().getName());

        return rDTO;
    }

    /*
    비밀번호 찾기
    */
    @Override
    public ExistsDTO findUserPw(UserInfoDTO pDTO) throws Exception {

        log.info("{}.findUserPw Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userId = CmmUtil.nvl(pDTO.userId());

        boolean exists = userInfoRepository.findByEmailAndUserId(email, userId).isPresent();
        int authNumber = 0;

        log.info("exists : {}", exists);

        if (exists) {
            authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
            log.info("authNumber : {}", authNumber);

            MailDTO mailDTO = new MailDTO();
            mailDTO.setTitle("비밀번호 찾기 인증번호 발송 메일");
            mailDTO.setContent("인증번호는 " + authNumber + " 입니다. ");
            mailDTO.setReceiver(EncryptUtil.decAES128BCBC(email));

            mailService.doSendMail(mailDTO);
        }

        ExistsDTO rDTO = ExistsDTO.builder()
                .exists(exists)
                .authNumber(authNumber)
                .build();

        log.info("rDTO : {}", rDTO);

        log.info("{}.findUserPw End!", this.getClass().getName());

        return rDTO;
    }

    /*
    비밀번호 수정
    */
    @Transactional
    @Override
    public int updatePassword(UserInfoDTO pDTO) throws Exception {

        log.info("{}.updatePassword Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId());
        String password = CmmUtil.nvl(pDTO.password());

        log.info("userId: {}", userId);

        int res = userInfoRepository.findByUserId(userId)
                .map(entity -> {
                    entity.updatePassword(password);
                    return 1;
                }).orElse(0);

        log.info("{}.updatePassword End!", this.getClass().getName());

        return res;
    }

    /*
    아이디로 모든 회원 정보를 조회
    */
    @Override
    public UserInfoDTO getUserInfo(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserInfo Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId());

        log.info("userId: {}", userId);

        UserInfoDTO rDTO = userInfoRepository.findByUserId(userId)
                .map(entity -> {
                    log.info("회원 정보 조회 성공: {}", userId);
                    return UserInfoDTO.builder()
                            .userNo(entity.getUserNo())
                            .userId(entity.getUserId())
                            .userName(entity.getUserName())
                            .profileImgUrl(entity.getProfileImgUrl())
                            .build();
                }).orElseGet(() -> {
                    log.info("회원 정보를 찾을 수 없음: {}", userId);
                    return UserInfoDTO.builder().build();
                });

        log.info("{}.getUserInfo End!", this.getClass().getName());

        return rDTO;
    }
}