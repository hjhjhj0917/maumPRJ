import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginRequest } from '../../../api/authApi';

export const useLoginForm = () => {
    const navigate = useNavigate();
    const [userId, setUserId] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [messages, setMessages] = useState({ userId: null, password: null });
    const messageTimers = useRef({});

    const setMessage = (field, message, type) => {
        if (messageTimers.current[field]) {
            clearTimeout(messageTimers.current[field]);
        }
        setMessages(prev => ({ ...prev, [field]: { text: message, type } }));
        messageTimers.current[field] = setTimeout(() => {
            setMessages(prev => ({ ...prev, [field]: null }));
        }, 3000);
    };

    const clearMessage = (field) => {
        if (messageTimers.current[field]) {
            clearTimeout(messageTimers.current[field]);
            delete messageTimers.current[field];
        }
        setMessages(prev => ({ ...prev, [field]: null }));
    };

    const handleLogin = async (e) => {
        if (e) e.preventDefault();

        if (!userId.trim()) {
            setMessage('userId', '아이디를 입력해주세요.', 'error');
            return;
        }

        if (!password.trim()) {
            setMessage('password', '비밀번호를 입력해주세요.', 'error');
            return;
        }

        try {
            // apiClient 사용으로 인해 .json() 과정 생략
            const res = await loginRequest(userId, password);

            if (res.result === 1) {
                navigate('/diary/write');
            } else {
                setMessage('userId', res.msg || "로그인 정보를 확인해주세요.", 'error');
                setPassword('');
            }
        } catch (error) {
            const errorMsg = error.response?.data?.msg || "서버 통신 중 오류가 발생했습니다.";
            alert(errorMsg);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleLogin();
        }
    };

    return {
        userId, setUserId,
        password, setPassword,
        showPassword, setShowPassword,
        messages, clearMessage,
        handleLogin, handleKeyDown
    };
};