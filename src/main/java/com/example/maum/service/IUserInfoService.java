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

    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;


    /* [Authentication & Verification] */

    MsgDTO verifyEmailCode(UserInfoDTO pDTO) throws Exception;

    List<ResponseCookie> logout(String accessToken, String userNo) throws Exception;


    /* [Account Recovery] */

    UserInfoDTO getUserId(UserInfoDTO pDTO) throws Exception;

    ExistsDTO findUserId(UserInfoDTO pDTO) throws Exception;

    ExistsDTO findUserPw(UserInfoDTO pDTO) throws Exception;

    ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception;


    /* [Profile & Security] */

    int updateProfileImg(UserInfoDTO pDTO) throws Exception;

    int updatePassword(UserInfoDTO pDTO) throws Exception;

}
