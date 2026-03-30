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

    @Override
    public ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        boolean exists = userInfoRepository.findByEmail(pDTO.email()).isPresent();
        int authNumber = 0;

        log.info("exists : {}", exists);

        if (!exists) {
            authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
            log.info("authNumber : {}", authNumber);

            MailDTO mailDTO = new MailDTO();
            mailDTO.setTitle("이메일 중복 확인 인증번호 발송 메일");
            mailDTO.setContent("인증번호는 " + authNumber + " 입니다. ");
            mailDTO.setReceiver(EncryptUtil.decAES128BCBC(CmmUtil.nvl(pDTO.email())));

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

    @Override
    public int insertUserInfo(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        log.info("pDTO: {}", pDTO);

        int res;

        String userId = CmmUtil.nvl(pDTO.userId());
        String password = CmmUtil.nvl(pDTO.password());
        String userName = CmmUtil.nvl(pDTO.userName());
        String email = CmmUtil.nvl(pDTO.email());
        String birthDate = CmmUtil.nvl(pDTO.birthDate());
        String addr = CmmUtil.nvl(pDTO.addr());
        String detailAddr = CmmUtil.nvl(pDTO.detailAddr());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            res = 2;
        } else {
            String profileImgUrl = "/images/account/base-profile.png";

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
                    .password(password)
                    .userName(userName)
                    .email(email)
                    .birthDate(LocalDate.parse(birthDate))
                    .addr(addr)
                    .detailAddr(detailAddr)
                    .profileImgUrl(profileImgUrl)
                    .build();

            userInfoRepository.save(pEntity);

            res = userInfoRepository.findByUserId(userId).isPresent() ? 1 : 0;
        }

        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return res;
    }

    @Transactional
    @Override
    public int updateProfileImg(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.updateProfileImg Start!", this.getClass().getName());

        int res = 0;

        String userId = CmmUtil.nvl(pDTO.userId());
        String profileImage = CmmUtil.nvl(pDTO.profileImgUrl());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            UserInfoEntity pEntity = rEntity.get();

            String profileImgUrl = "/images/account/base-profile.png";

            if (profileImage != null && profileImage.startsWith("profile") && profileImage.endsWith(".png")) {
                profileImgUrl = "/images/account/" + profileImage;
            }

            pEntity.updateProfileImg(profileImgUrl);

            res = 1;
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

        boolean res = userInfoRepository.findByUserIdAndPassword(userId, password).isPresent();

        log.info("{}.getUserLogin End!", this.getClass().getName());

        return res ? 1 : 0;
    }
}
