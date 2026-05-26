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
    apiClient.post('/account/getEmailExists', {email});

export const verifyEmailCode = (email, code) =>
    apiClient.post('/account/verifyEmailCode', {email, code});

export const updateProfileImg = (profileImgUrl) =>
    apiClient.post('/account/updateProfileImg', {profileImgUrl});

export const logoutUser = () => apiClient.post('/account/logout');

export const findUserId = (email, userName) =>
    apiClient.post('/account/findUserId', {email, userName});

export const getUserId = (email, userName, code) =>
    apiClient.post('/account/getUserId', {email, userName, code});

export const findUserPw = (email, userId) =>
    apiClient.post('/account/findUserPw', {email, userId});

export const updateUserPw = (email, password, code) =>
    apiClient.post('/account/updateUserPw', {email, password, code});

export const verifyCurrentPassword = (password) =>
    apiClient.post('/account/verifyCurrentPassword', { password });

export const updateAccount = (payload) =>
    apiClient.post('/account/updateAccount', payload);

export const sendWithdrawEmailCode = (email) =>
    apiClient.post('/account/sendWithdrawEmailCode', { email });

export const deleteUser = () =>
    apiClient.post('/account/deleteUser');