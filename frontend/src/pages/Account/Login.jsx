import React, {useState, useEffect, useRef} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import './Login.css';

import bg1 from '../../assets/images/account/login-background1.jpg';
import bg2 from '../../assets/images/account/login-background2.jpg';
import bg3 from '../../assets/images/account/login-background3.jpg';
import logoImg from '../../assets/images/includes/logo.png';

const Login = () => {
    const [slideIndex, setSlideIndex] = useState(0);
    const [userId, setUserId] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [messages, setMessages] = useState({userId: null, password: null});

    const navigate = useNavigate();
    const messageTimers = useRef({});

    useEffect(() => {
        const timer = setInterval(() => {
            setSlideIndex((prevIndex) => (prevIndex + 1) % 3);
        }, 4000);
        return () => clearInterval(timer);
    }, []);

    const setMessage = (field, message, type) => {
        if (messageTimers.current[field]) {
            clearTimeout(messageTimers.current[field]);
        }

        setMessages(prev => ({...prev, [field]: {text: message, type}}));

        messageTimers.current[field] = setTimeout(() => {
            setMessages(prev => ({...prev, [field]: null}));
        }, 3000);
    };

    const clearMessage = (field) => {
        if (messageTimers.current[field]) {
            clearTimeout(messageTimers.current[field]);
            delete messageTimers.current[field];
        }
        setMessages(prev => ({...prev, [field]: null}));
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
            const params = new URLSearchParams();
            params.append('userId', userId);
            params.append('password', password);

            const response = await fetch('/api/account/loginProc', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params
            });

            const json = await response.json();

            if (json.result === 1) {
                navigate('/main');
            } else {
                setMessage('userId', json.msg, 'error');
                setPassword('');
            }
        } catch (error) {
            alert("서버 통신 중 오류가 발생했습니다.");
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleLogin();
        }
    };

    return (
        <div className="login-page-wrapper">
            <div className="container">
                <div className="slider-section">
                    <div className="bg-slider">
                        <div className={`bg-slide ${slideIndex === 0 ? 'active' : ''}`}
                             style={{backgroundImage: `url(${bg1})`}}></div>
                        <div className={`bg-slide ${slideIndex === 1 ? 'active' : ''}`}
                             style={{backgroundImage: `url(${bg2})`}}></div>
                        <div className={`bg-slide ${slideIndex === 2 ? 'active' : ''}`}
                             style={{backgroundImage: `url(${bg3})`}}></div>
                    </div>

                    <div className="slider-overlay"></div>

                    <div className="slider-wrapper">
                        <div className={`slide ${slideIndex === 0 ? 'active' : ''}`}>
                            <h2>Memory.</h2>
                            <p>흘러가면 잊혀질 오늘의 순간을 소중하게 기억합니다.<br/>빛나는 청춘의 한 페이지를 이곳에 남겨두세요.</p>
                        </div>
                        <div className={`slide ${slideIndex === 1 ? 'active' : ''}`}>
                            <h2>Journal.</h2>
                            <p>복잡한 머릿속 생각들을 차분하게 기록합니다.<br/>나만의 속도로 써 내려가는 글이 마음의 쉼표가 됩니다.</p>
                        </div>
                        <div className={`slide ${slideIndex === 2 ? 'active' : ''}`}>
                            <h2>Mood.</h2>
                            <p>글 속에 담긴 내면의 소리와 감정 상태를 분석합니다.<br/>스스로도 몰랐던 나의 진짜 마음을 마주해보세요.</p>
                        </div>
                    </div>

                    <div className="dots-container">
                        <span className={`dot ${slideIndex === 0 ? 'active' : ''}`}
                              onClick={() => setSlideIndex(0)}></span>
                        <span className={`dot ${slideIndex === 1 ? 'active' : ''}`}
                              onClick={() => setSlideIndex(1)}></span>
                        <span className={`dot ${slideIndex === 2 ? 'active' : ''}`}
                              onClick={() => setSlideIndex(2)}></span>
                    </div>
                </div>

                <div className="login-section">
                    <div className="login-card">
                        <Link to="/">
                            <img src={logoImg} alt="마음 로고" className="login-logo-title"/>
                        </Link>

                        <form id="loginForm" onSubmit={handleLogin}>
                            <div className="input-group">
                                <div className="label-row">
                                    <label htmlFor="userId">User ID</label>
                                    <span
                                        className={`field-message ${messages.userId ? `show ${messages.userId.type}` : ''}`}>
                                        {messages.userId?.text}
                                    </span>
                                </div>
                                <input
                                    type="text"
                                    id="userId"
                                    name="userId"
                                    placeholder="아이디를 입력하세요"
                                    required
                                    autoComplete="off"
                                    value={userId}
                                    onChange={(e) => {
                                        setUserId(e.target.value);
                                        clearMessage('userId');
                                    }}
                                />
                            </div>

                            <div className="input-group">
                                <div className="label-row">
                                    <label htmlFor="password">Password</label>
                                    <span
                                        className={`field-message ${messages.password ? `show ${messages.password.type}` : ''}`}>
                                        {messages.password?.text}
                                    </span>
                                </div>
                                <div className="password-wrapper">
                                    <input
                                        type={showPassword ? "text" : "password"}
                                        id="password"
                                        name="password"
                                        placeholder="비밀번호를 입력하세요"
                                        required
                                        value={password}
                                        onChange={(e) => {
                                            setPassword(e.target.value);
                                            clearMessage('password');
                                        }}
                                        onKeyDown={handleKeyDown}
                                    />
                                    <i
                                        className={`fa-regular ${showPassword ? 'fa-eye-slash active' : 'fa-eye'} toggle-password`}
                                        onClick={() => setShowPassword(!showPassword)}
                                    ></i>
                                </div>
                            </div>

                            <button type="button" className="btn-login" onClick={handleLogin}>로그인</button>
                        </form>

                        <div className="find-links">
                            <div className="find-row">
                                <Link to="/account/findId">아이디 찾기</Link>
                                <span className="separator">|</span>
                                <Link to="/account/findPw">비밀번호 찾기</Link>
                            </div>
                        </div>

                        <div className="signup-box">
                            아직 회원이 아니시라면, 지금 바로 마음(MAUM)을 <br/>
                            시작해 보세요. <Link to="/account/register" className="link-signup">가입하기</Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;