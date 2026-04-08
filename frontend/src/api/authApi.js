/*
로그인
*/
export const loginRequest = async (userId, password) => {
    const params = new URLSearchParams();
    params.append('userId', userId);
    params.append('password', password);

    const response = await fetch('/api/account/loginProc', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    });

    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
이메일 중복 확인
*/
export const checkEmailExists = async (email) => {
    const params = new URLSearchParams({ email });
    const response = await fetch('/api/account/getEmailExists', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    });
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
이메일 중복 확인 인증번호 확인
*/
export const verifyEmailCode = async (email, code) => {
    try {
        const params = new URLSearchParams({ email, code });
        const response = await fetch('/api/account/verifyEmailCode', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        });
        return await response.json();
    } catch (error) {
        console.error("verifyEmailCode Error:", error);
        throw error;
    }
};

/*
아이디 중복 확인
*/
export const checkUserIdExists = async (userId) => {
    const params = new URLSearchParams({ userId });
    const response = await fetch('/api/account/getUserIdExists', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    });
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
회원가입
*/
export const registerUser = async (formData) => {
    const params = new URLSearchParams();
    Object.keys(formData).forEach(key => {
        if (key !== 'code' && key !== 'passwordConfirm') params.append(key, formData[key]);
    });

    const response = await fetch('/api/account/insertUserInfo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    });
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
로그인 상태 확인
*/
export const getUserStatus = async () => {
    const response = await fetch('/api/account/status');
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
로그아웃
*/
export const logoutUser = async () => {
    const response = await fetch('/api/account/logout', { method: 'POST' });
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
아이디 찾기
*/
export const findUserId = async (email, userName) => {
    const params = new URLSearchParams({ email, userName });
    const response = await fetch('/api/account/findUserId', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    });
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
메일과 이름으로 아이디 조회
*/
export const getUserId = async (email, userName, code) => {
    const params = new URLSearchParams({ email, userName, code });
    const response = await fetch('/api/account/getUserId', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    });
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
비밀번호 찾기
*/
export const findUserPw = async (email, userId) => {
    const params = new URLSearchParams({ email, userId });
    const response = await fetch('/api/account/findUserPw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    });
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};

/*
비밀번호 수정
*/
export const updateUserPw = async (password) => {
    const params = new URLSearchParams({ password });
    const response = await fetch('/api/account/updateUserPw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    });
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
};