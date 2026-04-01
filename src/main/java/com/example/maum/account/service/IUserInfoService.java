package com.example.maum.account.service;

import com.example.maum.account.dto.ExistsDTO;
import com.example.maum.account.dto.UserInfoDTO;

public interface IUserInfoService {

    /*
    아이디 중복 확인
    */
    ExistsDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    /*
    이메일 중복 확인
    */
    ExistsDTO getEmailExists(UserInfoDTO pDTO) throws Exception;

    /*
    회원가입
    */
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;

    /*
    로그인
    */
    int getUserLogin(UserInfoDTO pDTO) throws Exception;

    /*
    아이디 찾기
    */
    ExistsDTO findUserId(UserInfoDTO pDTO) throws Exception;

    /*
    모든 회원 정보를 조회
    */
    UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception;

    /*
    회원 프로필 이미지 수정
    */
    int updateProfileImg(UserInfoDTO pDTO) throws Exception;
}
