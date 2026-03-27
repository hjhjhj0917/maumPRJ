package com.example.maum.account.service;

import com.example.maum.account.dto.ExistsDTO;
import com.example.maum.account.dto.UserInfoDTO;

public interface IUserInfoService {

    ExistsDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception;

    int insertUserInfo(UserInfoDTO pDTO) throws Exception;

    int getUserLogin(UserInfoDTO pDTO) throws Exception;
}
