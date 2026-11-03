package com.example.maum.service;

import com.example.maum.dto.ExistsDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.UserInfoDTO;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface IUserInfoService extends UserDetailsService {

    /* [Account Management] */

    int insertUserInfo(UserInfoDTO pDTO);

    UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception;


    /* [Authentication & Verification] */

    MsgDTO verifyEmailCode(UserInfoDTO pDTO) throws Exception;

    int updateLastLogin(UserInfoDTO pDTO);

    List<ResponseCookie> logout(String accessToken, String publicId, long remainingMilliSeconds) throws Exception;


    /* [Account Recovery] */

    ExistsDTO findUserPw(UserInfoDTO pDTO) throws Exception;

    ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception;


    /* [Profile & Security] */

    int updateProfileImg(UserInfoDTO pDTO) throws Exception;

    int updatePassword(UserInfoDTO pDTO) throws Exception;

    MsgDTO verifyCurrentPassword(UserInfoDTO pDTO) throws Exception;

    int updateAccount(UserInfoDTO pDTO) throws Exception;

    MsgDTO sendWithdrawEmailCode(UserInfoDTO pDTO) throws Exception;

    int deleteUser(UserInfoDTO pDTO) throws Exception;

}
