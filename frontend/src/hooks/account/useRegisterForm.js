import {useState, useEffect, useRef} from 'react';
import {useNavigate} from 'react-router-dom';
import {checkEmailExists, checkUserIdExists, registerUser, verifyEmailCode} from '../../api/authApi';

export const useRegisterForm = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [formData, setFormData] = useState({
        email: '', code: '', userId: '', password: '', passwordConfirm: '',
        userName: '', birthDate: '', addr: '', detailAddr: ''
    });
    const [messages, setMessages] = useState({});
    const [flags, setFlags] = useState({emailVerified: false, userIdChecked: false});
    const [showDatePicker, setShowDatePicker] = useState(false);
    const [modal, setModal] = useState({show: false, title: '', message: '', onConfirm: null});

    const messageTimers = useRef({});

    useEffect(() => {
        const script = document.createElement('script');
        script.src = "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
        script.async = true;
        document.body.appendChild(script);
        return () => document.body.removeChild(script);
    }, []);

    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData(prev => ({...prev, [name]: value}));
        clearMessage(name + 'Msg');
    };

    useEffect(() => {
        if (step === 2 && formData.password && formData.passwordConfirm) {
            if (formData.password === formData.passwordConfirm) {
                if (!flags.userIdChecked) {
                    setMessage('userIdMsg', '아이디 중복 체크를 완료해 주세요.', 'error');
                    return;
                }
                setMessage('passwordConfirmMsg', '비밀번호가 일치합니다.', 'success');
                const timer = setTimeout(() => {
                    setStep(3);
                }, 1000);
                return () => clearTimeout(timer);
            }
        }
    }, [formData.password, formData.passwordConfirm, flags.userIdChecked, step]);

    const setMessage = (id, message, type) => {
        if (messageTimers.current[id]) clearTimeout(messageTimers.current[id]);
        setMessages(prev => ({...prev, [id]: {text: message, type}}));
        messageTimers.current[id] = setTimeout(() => {
            clearMessage(id);
        }, type === 'success' ? 2000 : 3000);
    };

    const clearMessage = (id) => {
        if (messageTimers.current[id]) clearTimeout(messageTimers.current[id]);
        setMessages(prev => {
            const newMsgs = {...prev};
            delete newMsgs[id];
            return newMsgs;
        });
    };

    const showAlert = (title, message, onConfirm = null) => {
        setModal({show: true, title, message, onConfirm});
    };

    const validateEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

    const handleEmailSend = async () => {
        if (!formData.email.trim()) return setMessage('emailMsg', '이메일을 입력하세요.', 'error');
        if (!validateEmail(formData.email)) return setMessage('emailMsg', '유효한 이메일 형식이 아닙니다.', 'error');

        try {
            const json = await checkEmailExists(formData.email);
            if (json.exists) {
                setMessage('emailMsg', '이미 가입된 이메일 주소가 존재합니다.', 'error');
            } else {
                showAlert("인증번호 발송", "이메일로 인증번호가 발송되었습니다.");
            }
        } catch (e) {
            showAlert("서버 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    const handleCodeVerify = async () => {
        if (!formData.code.trim()) return setMessage('codeMsg', '인증번호를 입력하세요.', 'error');

        try {
            const json = await verifyEmailCode(formData.email, formData.code);

            if (json.result === 1) {
                setMessage('codeMsg', '인증번호가 확인되었습니다.', 'success');

                setFlags(prev => ({...prev, emailVerified: true}));

                setTimeout(() => {
                    setStep(2);
                }, 800);
            } else {
                setMessage('codeMsg', json.msg || '잘못된 인증번호 입니다.', 'error');
            }
        } catch (e) {
            setMessage('codeMsg', '서버 통신 중 오류가 발생했습니다.', 'error');
        }
    };

    const handleUserIdCheck = async () => {
        if (!formData.userId.trim()) return setMessage('userIdMsg', '아이디를 입력하세요.', 'error');

        try {
            const json = await checkUserIdExists(formData.userId);
            if (json.exists) {
                setMessage('userIdMsg', '이미 가입된 아이디가 존재합니다.', 'error');
            } else {
                setMessage('userIdMsg', '사용 가능한 아이디입니다.', 'success');
                setFlags(prev => ({...prev, userIdChecked: true}));
            }
        } catch (e) {
            setMessage('userIdMsg', '서버 통신 중 오류가 발생했습니다.', 'error');
        }
    };

    const handleKakaoPost = () => {
        new window.daum.Postcode({
            oncomplete: (data) => {
                setFormData(prev => ({...prev, addr: `(${data.zonecode}) ${data.address}`}));
                clearMessage('addrMsg');
            }
        }).open();
    };

    const validateStep1 = () => {
        if (!flags.emailVerified) {
            setMessage('codeMsg', '이메일 인증을 완료해 주세요.', 'error');
            return false;
        }
        return true;
    };

    const validateStep2 = () => {
        let isValid = true;
        if (!formData.userId.trim()) {
            setMessage('userIdMsg', '아이디를 입력하세요.', 'error');
            isValid = false;
        } else if (!flags.userIdChecked) {
            setMessage('userIdMsg', '아이디 중복 체크를 완료해 주세요.', 'error');
            isValid = false;
        }
        if (!formData.password.trim()) {
            setMessage('passwordMsg', '비밀번호를 입력하세요.', 'error');
            isValid = false;
        }
        if (!formData.passwordConfirm.trim()) {
            setMessage('passwordConfirmMsg', '비밀번호 확인을 입력하세요.', 'error');
            isValid = false;
        } else if (formData.password !== formData.passwordConfirm) {
            setMessage('passwordConfirmMsg', '비밀번호가 일치하지 않습니다.', 'error');
            isValid = false;
        }
        return isValid;
    };

    const validateStep3 = () => {
        let isValid = true;
        if (!formData.userName.trim()) {
            setMessage('userNameMsg', '이름을 입력하세요.', 'error');
            isValid = false;
        }
        if (!formData.birthDate.trim()) {
            setMessage('birthDateMsg', '생년월일을 입력하세요.', 'error');
            isValid = false;
        }
        if (!formData.addr.trim()) {
            setMessage('addrMsg', '주소를 입력하세요.', 'error');
            isValid = false;
        }
        return isValid;
    };

    const nextStep = (target) => {
        if (target === 2 && !validateStep1()) return;
        if (target === 3 && !validateStep2()) return;
        setStep(target);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateStep1() || !validateStep2() || !validateStep3()) return;

        try {
            const json = await registerUser(formData);
            if (json.result === 1) {
                showAlert("회원가입 성공", json.msg, () => navigate('/account/profile'));
            } else {
                showAlert("회원가입 실패", json.msg);
            }
        } catch (err) {
            showAlert("서버 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    return {
        step, setStep, nextStep,
        formData, setFormData, handleChange,
        messages, flags,
        showDatePicker, setShowDatePicker,
        modal, setModal,
        handleEmailSend, handleCodeVerify, handleUserIdCheck, handleKakaoPost, handleSubmit
    };
};