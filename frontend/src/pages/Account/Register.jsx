import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../../components/common/Header';
import CustomModal from '../../components/common/CustomModal';
import InputField from '../../components/common/InputField';
import RollerDatePicker from '../../components/common/RollerDatePicker';
import './Register.css';

const Register = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [formData, setFormData] = useState({
        email: '', code: '', userId: '', password: '', passwordConfirm: '',
        userName: '', birthDate: '', addr: '', detailAddr: ''
    });
    const [messages, setMessages] = useState({});
    const [flags, setFlags] = useState({ emailVerified: false, emailAuthNumber: null, userIdChecked: false });
    const [showDatePicker, setShowDatePicker] = useState(false);
    const [modal, setModal] = useState({ show: false, title: '', message: '', onConfirm: null });

    const messageTimers = useRef({});

    useEffect(() => {
        const script = document.createElement('script');
        script.src = "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
        script.async = true;
        document.body.appendChild(script);
        return () => document.body.removeChild(script);
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        clearMessage(name + 'Msg');
    };

    useEffect(() => {
        // Step 2에서 비밀번호와 확인란에 모두 값이 있을 때만 작동
        if (step === 2 && formData.password && formData.passwordConfirm) {
            if (formData.password === formData.passwordConfirm) {
                // 아이디 중복확인 안 했으면 에러 띄우고 정지
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
        setMessages(prev => ({ ...prev, [id]: { text: message, type } }));

        messageTimers.current[id] = setTimeout(() => {
            clearMessage(id);
        }, type === 'success' ? 2000 : 3000);
    };

    const clearMessage = (id) => {
        if (messageTimers.current[id]) clearTimeout(messageTimers.current[id]);
        setMessages(prev => {
            const newMsgs = { ...prev };
            delete newMsgs[id];
            return newMsgs;
        });
    };

    const showAlert = (title, message, onConfirm = null) => {
        setModal({ show: true, title, message, onConfirm });
    };

    const validateEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

    const handleEmailSend = async () => {
        if (!formData.email.trim()) return setMessage('emailMsg', '이메일을 입력하세요.', 'error');
        if (!validateEmail(formData.email)) return setMessage('emailMsg', '유효한 이메일 형식이 아닙니다.', 'error');

        try {
            const params = new URLSearchParams({ email: formData.email });
            const response = await fetch('/api/account/getEmailExists', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });
            const json = await response.json();

            if (json.exists) {
                setMessage('emailMsg', '이미 가입된 이메일 주소가 존재합니다.', 'error');
            } else {
                showAlert("인증번호 발송", "이메일로 인증번호가 발송되었습니다.");
                setFlags(prev => ({ ...prev, emailAuthNumber: json.authNumber }));
            }
        } catch (e) {
            showAlert("서버 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    const handleCodeVerify = () => {
        if (!formData.code.trim()) return setMessage('codeMsg', '인증번호를 입력하세요.', 'error');
        if (parseInt(formData.code) !== flags.emailAuthNumber) return setMessage('codeMsg', '잘못된 인증번호 입니다.', 'error');

        setMessage('codeMsg', '인증번호가 확인되었습니다.', 'success');
        setFlags(prev => ({ ...prev, emailVerified: true }));
        setTimeout(() => nextStep(2), 800);
    };

    const handleUserIdCheck = async () => {
        if (!formData.userId.trim()) return setMessage('userIdMsg', '아이디를 입력하세요.', 'error');

        try {
            const params = new URLSearchParams({ userId: formData.userId });
            const response = await fetch('/api/account/getUserIdExists', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });
            const json = await response.json();

            if (json.exists) {
                setMessage('userIdMsg', '이미 가입된 아이디가 존재합니다.', 'error');
            } else {
                setMessage('userIdMsg', '사용 가능한 아이디입니다.', 'success');
                setFlags(prev => ({ ...prev, userIdChecked: true }));
            }
        } catch (e) {
            setMessage('userIdMsg', '서버 통신 중 오류가 발생했습니다.', 'error');
        }
    };

    const handleKakaoPost = () => {
        new window.daum.Postcode({
            oncomplete: (data) => {
                setFormData(prev => ({ ...prev, addr: `(${data.zonecode}) ${data.address}` }));
                clearMessage('addrMsg');
            }
        }).open();
    };

    const validateStep1 = () => {
        let isValid = true;
        if (!formData.email.trim()) { setMessage('emailMsg', '이메일을 입력하세요.', 'error'); isValid = false; }
        else if (!validateEmail(formData.email)) { setMessage('emailMsg', '유효한 이메일 형식이 아닙니다.', 'error'); isValid = false; }
        if (!formData.code.trim()) { setMessage('codeMsg', '인증번호를 입력하세요.', 'error'); isValid = false; }
        else if (parseInt(formData.code) !== flags.emailAuthNumber) { setMessage('codeMsg', '이메일 인증번호가 일치하지 않습니다.', 'error'); isValid = false; }
        return isValid;
    };

    const validateStep2 = () => {
        let isValid = true;
        if (!formData.userId.trim()) { setMessage('userIdMsg', '아이디를 입력하세요.', 'error'); isValid = false; }
        else if (!flags.userIdChecked) { setMessage('userIdMsg', '아이디 중복 체크를 완료해 주세요.', 'error'); isValid = false; }
        if (!formData.password.trim()) { setMessage('passwordMsg', '비밀번호를 입력하세요.', 'error'); isValid = false; }
        if (!formData.passwordConfirm.trim()) { setMessage('passwordConfirmMsg', '비밀번호 확인을 입력하세요.', 'error'); isValid = false; }
        else if (formData.password !== formData.passwordConfirm) { setMessage('passwordConfirmMsg', '비밀번호가 일치하지 않습니다.', 'error'); isValid = false; }
        return isValid;
    };

    const validateStep3 = () => {
        let isValid = true;
        if (!formData.userName.trim()) { setMessage('userNameMsg', '이름을 입력하세요.', 'error'); isValid = false; }
        if (!formData.birthDate.trim()) { setMessage('birthDateMsg', '생년월일을 입력하세요.', 'error'); isValid = false; }
        if (!formData.addr.trim()) { setMessage('addrMsg', '주소를 입력하세요.', 'error'); isValid = false; }
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
            const params = new URLSearchParams();
            Object.keys(formData).forEach(key => {
                if (key !== 'code' && key !== 'passwordConfirm') params.append(key, formData[key]);
            });

            const response = await fetch('/api/account/insertUserInfo', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });
            const json = await response.json();

            if (json.result === 1) {
                showAlert("회원가입 성공", json.msg, () => navigate('/account/profile'));
            } else {
                showAlert("회원가입 실패", json.msg);
            }
        } catch (err) {
            showAlert("서버 오류", "서버 통신 중 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.");
        }
    };

    return (
        <div className="register-wrapper">
            <Header />
            <CustomModal
                isOpen={modal.show} title={modal.title} message={modal.message}
                isConfirm={false} onCancel={() => setModal({ show: false, title: '', message: '', onConfirm: null })}
                onConfirm={() => {
                    setModal({ show: false, title: '', message: '', onConfirm: null });
                    if (modal.onConfirm) modal.onConfirm();
                }}
            />

            <div className="container">
                <div className="register-card">
                    <div className="stepper-wrapper">
                        {[1, 2, 3].map((num, idx) => (
                            <React.Fragment key={num}>
                                <div className={`stepper-item ${step >= num ? (step === num ? 'active' : 'completed') : ''}`}>
                                    <div className="step-label">{num === 1 ? '인증' : num === 2 ? '계정정보' : '개인정보'}</div>
                                    <div className="step-circle"><i className="fa-solid fa-check"></i></div>
                                </div>
                                {idx < 2 && <div className="step-line"></div>}
                            </React.Fragment>
                        ))}
                    </div>

                    <form onSubmit={handleSubmit}>
                        <div className="slide-viewport">
                            <div className="slide-track" style={{ transform: `translateX(${(step - 1) * -100}%)` }}>

                                <div className={`form-step ${step === 1 ? 'active-slide' : ''}`}>
                                    <div className="auth-inputs">
                                        <InputField label="E-mail" name="email" value={formData.email} onChange={handleChange} errorMsg={messages.emailMsg} readOnly={flags.emailVerified} actionBtn={{ text: '인증번호 발송', onClick: handleEmailSend, disabled: flags.emailVerified }} placeholder="이메일을 입력하세요." />
                                        <InputField label="Code" name="code" value={formData.code} onChange={handleChange} errorMsg={messages.codeMsg} readOnly={flags.emailVerified} actionBtn={{ text: '인증 확인', onClick: handleCodeVerify, disabled: flags.emailVerified }} placeholder="인증번호를 입력하세요." />
                                    </div>
                                </div>

                                <div className={`form-step ${step === 2 ? 'active-slide' : ''}`}>
                                    <div className="auth-inputs">
                                        <InputField label="User ID" name="userId" value={formData.userId} onChange={handleChange} errorMsg={messages.userIdMsg} readOnly={flags.userIdChecked} actionBtn={{ text: '중복확인', onClick: handleUserIdCheck, disabled: flags.userIdChecked }} placeholder="아이디를 입력하세요." />
                                        <InputField label="Password" name="password" isPassword={true} value={formData.password} onChange={handleChange} errorMsg={messages.passwordMsg} placeholder="비밀번호를 입력하세요." />
                                        <InputField label="Confirm Password" name="passwordConfirm" isPassword={true} value={formData.passwordConfirm} onChange={handleChange} errorMsg={messages.passwordConfirmMsg} placeholder="비밀번호를 다시 입력하세요." />
                                    </div>
                                </div>

                                <div className={`form-step ${step === 3 ? 'active-slide' : ''}`}>
                                    <div className="auth-inputs">
                                        <InputField label="Name" name="userName" value={formData.userName} onChange={handleChange} errorMsg={messages.userNameMsg} placeholder="이름을 입력하세요." />

                                        <InputField label="Birth Date" name="birthDate" value={formData.birthDate} readOnly={true} onClick={() => setShowDatePicker(true)} errorMsg={messages.birthDateMsg} placeholder="YYYY-MM-DD">
                                            {showDatePicker && (
                                                <RollerDatePicker
                                                    initialDate={{ year: 2000, month: 1, day: 1 }}
                                                    onClose={() => setShowDatePicker(false)}
                                                    onConfirm={(date) => {
                                                        setFormData(prev => ({ ...prev, birthDate: `${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')}` }));
                                                        setShowDatePicker(false);
                                                    }}
                                                />
                                            )}
                                        </InputField>

                                        <InputField label="Address" name="addr" value={formData.addr} readOnly={true} errorMsg={messages.addrMsg} actionBtn={{ text: '우편번호', onClick: handleKakaoPost }} placeholder="주소를 입력하세요." />
                                        <InputField label="Apartment, etc" name="detailAddr" value={formData.detailAddr} onChange={handleChange} errorMsg={messages.detailAddrMsg} placeholder="상세주소를 입력하세요." />
                                    </div>
                                </div>

                            </div>
                        </div>

                        <div className="action-buttons">
                            {step === 1 && (
                                <div className="btn-step-group active">
                                    <div className="btn-row right">
                                        <button type="button" className="btn-next" onClick={() => nextStep(2)}>다음</button>
                                    </div>
                                </div>
                            )}
                            {step === 2 && (
                                <div className="btn-step-group active">
                                    <div className="btn-row split">
                                        <button type="button" className="btn-prev" onClick={() => setStep(1)}>이전</button>
                                        <button type="button" className="btn-next" onClick={() => nextStep(3)}>다음</button>
                                    </div>
                                </div>
                            )}
                            {step === 3 && (
                                <div className="btn-step-group active">
                                    <div className="btn-row split">
                                        <button type="button" className="btn-prev" onClick={() => setStep(2)}>이전</button>
                                        <button type="submit" className="btn-submit">가입하기</button>
                                    </div>
                                </div>
                            )}
                        </div>
                    </form>

                    <div className="login-box">
                        이미 마음(MAUM) 회원이신가요?
                        <Link to="/account/login" className="link-login">로그인</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Register;