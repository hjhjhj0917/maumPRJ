import apiClient from './apiClient';

export const loginRequest = (userId, password) =>
    apiClient.post('/login/loginProc', {userId, password});

export const getUserStatus = () =>
    apiClient.post('/account/userInfo');

export const registerUser = (formData) => {
    const {code, passwordConfirm, ...userData} = formData;

    userData.profileImgUrl = "/images/account/profile1.png";

    if (userData.birthDate) {
        userData.birthDate = userData.birthDate
            .replace('년 ', '-')
            .replace('월 ', '-')
            .replace('일', '');

        const yearStr = userData.birthDate.split('-')[0];
        if (yearStr && yearStr.length === 4) {
            const birthYear = parseInt(yearStr, 10);
            let zodiacNum = ((birthYear - 4) % 12) + 1;
            if (zodiacNum < 1) zodiacNum += 12;

            userData.profileImgUrl = `/images/account/profile${zodiacNum}.png`;
        }
    }

    return apiClient.post('/reg/insertUserInfo', userData);
};

export const checkUserIdExists = (userId) =>
    apiClient.post('/reg/getUserIdExists', {userId});

export const checkEmailExists = (email) =>
    apiClient.post('/account/getEmailExists', new URLSearchParams({email}), {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    });

export const verifyEmailCode = (email, code) =>
    apiClient.post('/account/verifyEmailCode', new URLSearchParams({email, code}), {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    });

export const updateProfileImg = (profileImage) =>
    apiClient.post('/account/updateProfileImg', new URLSearchParams({profileImage}), {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    });

export const logoutUser = () => apiClient.post('/account/logout');

export const findUserId = (email, userName) =>
    apiClient.post('/account/findUserId', new URLSearchParams({email, userName}), {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    });

export const getUserId = (email, userName, code) =>
    apiClient.post('/account/getUserId', new URLSearchParams({email, userName, code}), {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    });

export const findUserPw = (email, userId) =>
    apiClient.post('/account/findUserPw', new URLSearchParams({email, userId}), {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    });

export const updateUserPw = (email, password, code) =>
    apiClient.post('/account/updateUserPw', new URLSearchParams({email, password, code}), {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    });