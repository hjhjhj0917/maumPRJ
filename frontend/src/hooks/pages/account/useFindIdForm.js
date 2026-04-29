import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { findUserId, getUserId } from '../../../api/authApi';

export const useFindIdForm = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [formData, setFormData] = useState({ userName: '', userEmail: '', code: '' });
    const [messages, setMessages] = useState({});
    const [foundId, setFoundId] = useState('');
    const [modal, setModal] = useState({ show: false, title: '', message: '', isConfirm: false, onConfirm: null, onCancel: null });

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
        setModal({ show: true, title, message, isConfirm: false, onConfirm: onConfirmCallback, onCancel: null });
    };

    const handleModalConfirm = () => {
        if (modal.onConfirm) modal.onConfirm();
        setModal(prev => ({ ...prev, show: false }));
    };

    const handleModalCancel = () => {
        if (modal.onCancel) modal.onCancel();
        setModal(prev => ({ ...prev, show: false }));
    };

    const validateEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

    // Step 1: 아이디 존재 여부 확인 및 인증번호 발송
    const handleStep1Submit = async (e) => {
        e.preventDefault();
        if (!formData.userEmail.trim()) return setMessage('userEmailMsg', '이메일을 입력하세요.', 'error');
        if (!validateEmail(formData.userEmail)) return setMessage('userEmailMsg', '유효한 이메일 형식이 아닙니다.', 'error');
        if (!formData.userName.trim()) return setMessage('userNameMsg', '이름을 입력하세요.', 'error');

        try {
            // apiClient 사용으로 인해 .json() 파싱이 필요 없음
            const res = await findUserId(formData.userEmail, formData.userName);

            if (res.exists) {
                showAlert("인증번호 발송", "이메일로 인증번호가 발송되었습니다.", () => {
                    setStep(2);
                });
            } else {
                setMessage('userEmailMsg', res.msg || '일치하는 회원이 없습니다.', 'error');
            }
        } catch (error) {
            const errorMsg = error.response?.data?.msg || "서버 통신 중 오류가 발생했습니다.";
            showAlert("시스템 오류", errorMsg);
        }
    };

    // Step 2: 인증번호 검증 및 아이디 획득
    const handleStep2Submit = async (e) => {
        e.preventDefault();
        const code = formData.code?.replace(/\s/g, '');
        if (!code || code.length < 6) return setMessage('codeMsg', '인증번호 6자리를 모두 입력하세요.', 'error');

        try {
            const res = await getUserId(formData.userEmail, formData.userName, code);

            if (res.userId) {
                setFoundId(res.userId);
                setStep(3);
            } else {
                setMessage('codeMsg', res.msg || '인증번호가 일치하지 않거나 만료되었습니다.', 'error');
            }
        } catch (error) {
            const errorMsg = error.response?.data?.msg || "서버 통신 중 오류가 발생했습니다.";
            showAlert("시스템 오류", errorMsg);
        }
    };

    // 인증번호 재전송
    const handleResend = async () => {
        try {
            const res = await findUserId(formData.userEmail, formData.userName);
            if (res.exists) {
                setMessage('codeMsg', '인증번호가 재전송되었습니다.', 'success');
            }
        } catch (error) {
            setMessage('codeMsg', '재전송 중 오류가 발생했습니다.', 'error');
        }
    };

    return {
        step,
        formData,
        handleChange,
        handleOtpChange,
        handleKeyDown,
        handlePaste,
        messages,
        foundId,
        modal,
        inputRefs,
        handleModalConfirm,
        handleModalCancel,
        handleStep1Submit,
        handleStep2Submit,
        handleResend,
        navigate
    };
};