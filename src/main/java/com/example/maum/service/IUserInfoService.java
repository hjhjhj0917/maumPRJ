package com.example.maum.service;

import com.example.maum.dto.ExistsDTO;
import com.example.maum.dto.UserInfoDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserInfoService extends UserDetailsService {

    /* CREATE */

    int insertUserInfo(UserInfoDTO pDTO);


    /* READ */

    int getUserLogin(UserInfoDTO pDTO) throws Exception;

    UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception;

    UserInfoDTO getUserId(UserInfoDTO pDTO) throws Exception;

    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    ExistsDTO findUserId(UserInfoDTO pDTO) throws Exception;

    ExistsDTO findUserPw(UserInfoDTO pDTO) throws Exception;

    ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception;


    /* UPDATE */

    int updateProfileImg(UserInfoDTO pDTO) throws Exception;

    int updatePassword(UserInfoDTO pDTO) throws Exception;

}
