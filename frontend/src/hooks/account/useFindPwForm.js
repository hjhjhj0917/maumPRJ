import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { findUserPw, verifyEmailCode, updateUserPw } from '../../api/authApi';

export const useFindPwForm = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [formData, setFormData] = useState({
        userId: '',
        userEmail: '',
        code: '',
        password: '',
        passwordConfirm: ''
    });
    const [messages, setMessages] = useState({});
    const [modal, setModal] = useState({ show: false, title: '', message: '', isConfirm: false, onConfirm: null });

    const messageTimers = useRef({});
    const inputRefs = useRef([]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        clearMessage(name + 'Msg');
    };

    const handleOtpChange = (e, index) => {
        const value = e.target.value;
        if (!/^[0-9]*$/.test(value)) return;

        const currentCode = formData.code || "";
        const codeArray = currentCode.split("");
        while (codeArray.length < 6) codeArray.push("");

        codeArray[index] = value.slice(-1);
        const finalCode = codeArray.join("");

        setFormData(prev => ({ ...prev, code: finalCode }));
        clearMessage('codeMsg');

        if (value !== "" && index < 5) {
            inputRefs.current[index + 1].focus();
        }
    };

    const handleKeyDown = (e, index) => {
        if (e.key === "Backspace") {
            if (!formData.code?.[index] && index > 0) {
                inputRefs.current[index - 1].focus();
            }
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();
        const pasteData = e.clipboardData.getData('text').replace(/[^0-9]/g, '').slice(0, 6);
        if (!pasteData) return;

        setFormData(prev => ({ ...prev, code: pasteData }));
        clearMessage('codeMsg');
        const nextIndex = pasteData.length === 6 ? 5 : pasteData.length;
        inputRefs.current[nextIndex]?.focus();
    };

    const setMessage = (id, message, type) => {
        if (messageTimers.current[id]) clearTimeout(messageTimers.current[id]);
        setMessages(prev => ({ ...prev, [id]: { text: message, type } }));
        messageTimers.current[id] = setTimeout(() => clearMessage(id), 3000);
    };

    const clearMessage = (id) => {
        if (messageTimers.current[id]) clearTimeout(messageTimers.current[id]);
        setMessages(prev => {
            const newMsgs = { ...prev };
            delete newMsgs[id];
            return newMsgs;
        });
    };

    const showAlert = (title, message, onConfirmCallback = null) => {
        setModal({ show: true, title, message, isConfirm: false, onConfirm: onConfirmCallback });
    };

    const showConfirm = (title, message, onConfirmCallback = null) => {
        setModal({ show: true, title, message, isConfirm: true, onConfirm: onConfirmCallback });
    };

    const handleModalConfirm = () => {
        if (modal.onConfirm) modal.onConfirm();
        setModal({ show: false, title: '', message: '', isConfirm: false, onConfirm: null });
    };

    const handleModalCancel = () => {
        setModal({ show: false, title: '', message: '', isConfirm: false, onConfirm: null });
    };

    const validateEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

    const handleStep1Submit = async (e) => {
        e.preventDefault();
        if (!formData.userId.trim()) return setMessage('userIdMsg', '아이디를 입력하세요.', 'error');
        if (!formData.userEmail.trim()) return setMessage('userEmailMsg', '이메일을 입력하세요.', 'error');
        if (!validateEmail(formData.userEmail)) return setMessage('userEmailMsg', '유효한 이메일 형식이 아닙니다.', 'error');

        try {
            const json = await findUserPw(formData.userEmail, formData.userId);
            if (json.exists) {
                showAlert("인증번호 발송", "이메일로 인증번호가 발송되었습니다.", () => setStep(2));
            } else {
                setMessage('userEmailMsg', '정보와 일치하는 회원이 없습니다.', 'error');
            }
        } catch (error) {
            showAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    const handleStep2Submit = async (e) => {
        e.preventDefault();
        const code = formData.code?.replace(/\s/g, '');
        if (!code || code.length < 6) return setMessage('codeMsg', '인증번호 6자리를 입력하세요.', 'error');

        try {
            const json = await verifyEmailCode(formData.userEmail, code);
            if (json.result === 1) {
                setMessage('codeMsg', '인증에 성공하였습니다.', 'success');
                setTimeout(() => setStep(3), 800);
            } else {
                setMessage('codeMsg', '인증번호가 일치하지 않습니다.', 'error');
            }
        } catch (error) {
            showAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    const handleStep3Submit = async (e) => {
        e.preventDefault();
        if (!formData.password.trim()) return setMessage('passwordMsg', '새 비밀번호를 입력하세요.', 'error');
        if (formData.password !== formData.passwordConfirm) return setMessage('passwordConfirmMsg', '비밀번호가 일치하지 않습니다.', 'error');

        try {
            const json = await updateUserPw(formData.userEmail, formData.password, formData.code);
            if (json.result === 1) {
                showAlert("변경 완료", "비밀번호가 성공적으로 변경되었습니다.", () => navigate('/account/login'));
            } else {
                showAlert("변경 실패", json.msg || "다시 시도해 주세요.");
            }
        } catch (error) {
            showAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    const handleResend = async () => {
        try {
            const json = await findUserPw(formData.userEmail, formData.userId);
            if (json.exists) setMessage('codeMsg', '인증번호가 재전송되었습니다.', 'success');
        } catch (error) {
            showAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    return {
        step, formData, handleChange, handleOtpChange, handleKeyDown, handlePaste,
        messages, modal, inputRefs, handleModalConfirm, handleModalCancel,
        handleStep1Submit, handleStep2Submit, handleStep3Submit, handleResend, navigate
    };
};