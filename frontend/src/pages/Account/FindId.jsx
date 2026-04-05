import React, {useState, useRef} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import InputField from '../../components/common/InputField';
import CustomModal from '../../components/common/CustomModal';
import './FindId.css';

const FindId = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [formData, setFormData] = useState({userName: '', userEmail: '', code: ''});
    const [messages, setMessages] = useState({});
    const [foundId, setFoundId] = useState('');

    const [modal, setModal] = useState({show: false, title: '', message: '', onConfirm: null});

    const messageTimers = useRef({});

    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData(prev => ({...prev, [name]: value}));
        clearMessage(name + 'Msg');
    };

    const setMessage = (id, message, type) => {
        if (messageTimers.current[id]) clearTimeout(messageTimers.current[id]);
        setMessages(prev => ({...prev, [id]: {text: message, type}}));
        messageTimers.current[id] = setTimeout(() => clearMessage(id), 3000);
    };

    const clearMessage = (id) => {
        if (messageTimers.current[id]) clearTimeout(messageTimers.current[id]);
        setMessages(prev => {
            const newMsgs = {...prev};
            delete newMsgs[id];
            return newMsgs;
        });
    };

    const showAlert = (title, message, onConfirmCallback = null) => {
        setModal({show: true, title, message, onConfirm: onConfirmCallback});
    };

    const handleModalConfirm = () => {
        if (modal.onConfirm) {
            modal.onConfirm(); // 여기서 setStep(2)가 실행됩니다.
        }
        setModal({show: false, title: '', message: '', onConfirm: null});
    };

    const handleModalCancel = () => {
        if (modal.onConfirm) {
            modal.onConfirm();
        }
        setModal({show: false, title: '', message: '', onConfirm: null});
    };

    const validateEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

    const handleStep1Submit = async (e) => {
        e.preventDefault();
        if (!formData.userEmail.trim()) return setMessage('userEmailMsg', '이메일을 입력하세요.', 'error');
        if (!validateEmail(formData.userEmail)) return setMessage('userEmailMsg', '유효한 이메일 형식이 아닙니다.', 'error');
        if (!formData.userName.trim()) return setMessage('userNameMsg', '이름을 입력하세요.', 'error');

        try {
            const params = new URLSearchParams({email: formData.userEmail, userName: formData.userName});
            const response = await fetch('/api/account/findUserId', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: params
            });
            const json = await response.json();

            if (json.exists) {
                showAlert("인증번호 발송", "이메일로 인증번호가 발송되었습니다.", () => {
                    setStep(2);
                });
            } else {
                showAlert("확인 불가", "입력하신 정보와 일치하는 회원이 없습니다.");
            }
        } catch (error) {
            showAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    const handleStep2Submit = async (e) => {
        e.preventDefault();
        if (!formData.code.trim()) return setMessage('codeMsg', '인증번호를 입력하세요.', 'error');

        try {
            const params = new URLSearchParams({
                email: formData.userEmail,
                userName: formData.userName,
                code: formData.code
            });
            const response = await fetch('/api/account/getUserId', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: params
            });
            const json = await response.json();

            if (json.userId) {
                setFoundId(json.userId);
                setStep(3);
            } else {
                setMessage('codeMsg', '인증번호가 일치하지 않거나 만료되었습니다.', 'error');
            }
        } catch (error) {
            showAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    const handleResend = async () => {
        try {
            const params = new URLSearchParams({email: formData.userEmail, userName: formData.userName});
            const response = await fetch('/api/account/findUserId', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: params
            });
            const json = await response.json();

            if (json.exists) {
                setMessage('codeMsg', '인증번호가 재전송되었습니다.', 'success');
            }
        } catch (error) {
            showAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="find-id-wrapper">
            <CustomModal
                isOpen={modal.show}
                title={modal.title}
                message={modal.message}
                isConfirm={false}
                onCancel={handleModalCancel}
                onConfirm={handleModalConfirm}
            />

            <div className="container">
                <div className="find-id-card">
                    <h3>아이디 찾기</h3>

                    <div className="step-container">
                        {step === 1 && (
                            <form onSubmit={handleStep1Submit} className="fade-in">
                                <InputField label="E-mail" name="userEmail" value={formData.userEmail}
                                            onChange={handleChange} errorMsg={messages.userEmailMsg}
                                            placeholder="이메일을 입력하세요."/>
                                <InputField label="Name" name="userName" value={formData.userName}
                                            onChange={handleChange} errorMsg={messages.userNameMsg}
                                            placeholder="이름을 입력하세요."/>
                                <button type="submit" className="btn-confirm">확인</button>
                            </form>
                        )}

                        {step === 2 && (
                            <form onSubmit={handleStep2Submit} className="fade-in">
                                <InputField label="Code" name="code" value={formData.code} onChange={handleChange}
                                            errorMsg={messages.codeMsg} placeholder="인증번호를 입력하세요."
                                            actionBtn={{text: '재전송', onClick: handleResend}}/>
                                <button type="submit" className="btn-confirm">확인</button>
                            </form>
                        )}

                        {step === 3 && (
                            <div className="result-section fade-in">
                                <div className="check-circle">
                                    <i className="fa-solid fa-check"></i>
                                </div>
                                <p className="result-text">
                                    {formData.userName}님의 아이디는<br/>
                                    <span className="highlight-id">{foundId}</span> 입니다.
                                </p>
                                <button type="button" className="btn-confirm"
                                        onClick={() => navigate('/account/login')}>확인
                                </button>
                            </div>
                        )}
                    </div>

                    <div className="auth-links">
                        <Link to="/account/login">로그인</Link>
                        <span className="separator">|</span>
                        <Link to="/account/find-pw">비밀번호 찾기</Link>
                    </div>

                    <div className="signup-box">
                        아직 회원이 아니시라면, 지금 바로 마음을 시작해 보세요.
                        <Link to="/account/register" className="link-signup">회원가입</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FindId;