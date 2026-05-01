package com.example.maum.service.impl;

import com.example.maum.auth.AuthInfo;
import com.example.maum.dto.ExistsDTO;
import com.example.maum.dto.MailDTO;
import com.example.maum.dto.UserInfoDTO;
import com.example.maum.repository.UserInfoRepository;
import com.example.maum.repository.entity.UserInfoEntity;
import com.example.maum.service.IUserInfoService;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.DateUtil;
import com.example.maum.util.EncryptUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoService implements IUserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final MailService mailService;


    @Transactional(readOnly = true)
    @Override
    public UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        return userInfoRepository.findByUserId(pDTO.userId())
                .map(e -> UserInfoDTO.builder()
                        .existsYn("Y")
                        .build())
                .orElseGet(() -> UserInfoDTO.builder()
                        .existsYn("N")
                        .build());
    }


    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        log.info("{}.loadUserByUsername Start!", this.getClass().getName());

        log.info("userId: {}", userId);

        UserInfoEntity rEntity = userInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId + " Not Found User"));

        UserInfoDTO rDTO = UserInfoDTO.from(rEntity);

        log.info("{}.loadUserByUsername End!", this.getClass().getName());

        return new AuthInfo(rDTO);
    }


    @Transactional
    @Override
    public int insertUserInfo(@NonNull UserInfoDTO pDTO) {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        int res;

        String userId = CmmUtil.nvl(pDTO.userId());
        String birthDate = CmmUtil.nvl(pDTO.birthDate()).trim();

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            res = 2;
        } else {
            String profileImgUrl = "/images/account/profile1.png";

            if (birthDate.length() >= 4) {
                try {
                    int birthYear = Integer.parseInt(birthDate.substring(0, 4));
                    int zodiacNum = ((birthYear - 4) % 12) + 1;
                    if (zodiacNum < 1) zodiacNum += 12;
                    profileImgUrl = "/images/account/profile" + zodiacNum + ".png";
                } catch (NumberFormatException e) {
                    log.error("생년월일 연도 파싱 오류: {}", e.getMessage());
                }
            }

            String parsedBirthDate = String.valueOf(DateUtil.parseLocalDate(birthDate, "yyyy년 MM월 dd일"));

            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userId(userId)
                    .password(CmmUtil.nvl(pDTO.password()))
                    .userName(CmmUtil.nvl(pDTO.userName()))
                    .email(CmmUtil.nvl(pDTO.email()))
                    .birthDate(parsedBirthDate)
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


    @Override
    public ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmail(email);

        boolean exists = rEntity.isPresent();
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


    @Transactional
    @Override
    public int updateProfileImg(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.updateProfileImg Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId());
        String profileImage = CmmUtil.nvl(pDTO.profileImgUrl());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        int res;
        if (rEntity.isPresent()) {
            UserInfoEntity entity = rEntity.get();
            String profileImgUrl = profileImage;

            if (profileImage != null && profileImage.startsWith("/images/account/profile") && profileImage.endsWith(".png")) {
                profileImgUrl = profileImage;
            }

            entity.updateProfileImg(profileImgUrl);
            res = 1;
        } else {
            res = 0;
        }

        log.info("{}.updateProfileImg End!", this.getClass().getName());

        return res;
    }


    @Override
    public int getUserLogin(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserLogin Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId());
        String password = CmmUtil.nvl(pDTO.password());

        log.info("userId: {}, password: {}", userId, password);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserIdAndPassword(userId, password);

        int res;
        if (rEntity.isPresent()) {
            res = 1;
        } else {
            res = 0;
        }

        log.info("{}.getUserLogin End!", this.getClass().getName());

        return res;
    }


    @Override
    public ExistsDTO findUserId(UserInfoDTO pDTO) throws Exception {

        log.info("{}.findUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userName = CmmUtil.nvl(pDTO.userName());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmailAndUserName(email, userName);

        boolean exists = rEntity.isPresent();
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


    @Override
    public UserInfoDTO getUserId(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userName = CmmUtil.nvl(pDTO.userName());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmailAndUserName(email, userName);

        UserInfoDTO rDTO;
        if (rEntity.isPresent()) {
            UserInfoEntity entity = rEntity.get();
            rDTO = UserInfoDTO.builder()
                    .userId(entity.getUserId())
                    .build();
        } else {
            rDTO = UserInfoDTO.builder().build();
        }

        log.info("{}.getUserId End!", this.getClass().getName());

        return rDTO;
    }


    @Override
    public ExistsDTO findUserPw(UserInfoDTO pDTO) throws Exception {

        log.info("{}.findUserPw Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String userId = CmmUtil.nvl(pDTO.userId());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmailAndUserId(email, userId);

        boolean exists = rEntity.isPresent();
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


    @Transactional
    @Override
    public int updatePassword(UserInfoDTO pDTO) throws Exception {

        log.info("{}.updatePassword Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId());
        String password = CmmUtil.nvl(pDTO.password());

        log.info("userId: {}", userId);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        int res;
        if (rEntity.isPresent()) {
            UserInfoEntity entity = rEntity.get();
            entity.updatePassword(password);
            res = 1;
        } else {
            res = 0;
        }

        log.info("{}.updatePassword End!", this.getClass().getName());

        return res;
    }


    @Override
    public UserInfoDTO getUserInfo(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserInfo Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId());

        log.info("userId: {}", userId);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        UserInfoDTO rDTO;
        if (rEntity.isPresent()) {
            UserInfoEntity entity = rEntity.get();
            log.info("회원 정보 조회 성공: {}", userId);
            rDTO = UserInfoDTO.builder()
                    .userNo(entity.getUserNo())
                    .userId(entity.getUserId())
                    .userName(entity.getUserName())
                    .profileImgUrl(entity.getProfileImgUrl())
                    .build();
        } else {
            log.info("회원 정보를 찾을 수 없음: {}", userId);
            rDTO = UserInfoDTO.builder().build();
        }

        log.info("{}.getUserInfo End!", this.getClass().getName());

        return rDTO;
    }
}