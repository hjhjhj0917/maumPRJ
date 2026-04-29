import apiClient from './apiClient';

/*
로그인
*/
export const loginRequest = (userId, password) =>
    apiClient.post('/account/loginProc', new URLSearchParams({userId, password}));

/*
이메일 중복 확인
*/
export const checkEmailExists = (email) =>
    apiClient.post('/account/getEmailExists', new URLSearchParams({email}));

/*
이메일 인증번호 확인
*/
export const verifyEmailCode = (email, code) =>
    apiClient.post('/account/verifyEmailCode', new URLSearchParams({email, code}));

/*
아이디 중복 확인
*/
export const checkUserIdExists = (userId) =>
    apiClient.post('/account/getUserIdExists', new URLSearchParams({userId}));

/*
회원가입
*/
export const registerUser = (formData) => {
    const params = new URLSearchParams();
    Object.keys(formData).forEach(key => {
        if (key !== 'code' && key !== 'passwordConfirm') {
            params.append(key, formData[key]);
        }
    });
    return apiClient.post('/account/insertUserInfo', params);
};

/*
프로필 이미지 수정
*/
export const updateProfileImg = (profileImage) =>
    apiClient.post('/account/updateProfileImg', new URLSearchParams({ profileImage }));

/*
로그인 상태 확인
*/
export const getUserStatus = () => apiClient.get('/account/status');

/*
로그아웃
*/
export const logoutUser = () => apiClient.post('/account/logout');

/*
아이디 찾기
*/
export const findUserId = (email, userName) =>
    apiClient.post('/account/findUserId', new URLSearchParams({email, userName}));

/*
메일과 이름으로 아이디 조회
*/
export const getUserId = (email, userName, code) =>
    apiClient.post('/account/getUserId', new URLSearchParams({email, userName, code}));

/*
비밀번호 찾기
*/
export const findUserPw = (email, userId) =>
    apiClient.post('/account/findUserPw', new URLSearchParams({email, userId}));

/*
비밀번호 수정
*/
export const updateUserPw = (email, password, code) =>
    apiClient.post('/account/updateUserPw', new URLSearchParams({email, password, code}));