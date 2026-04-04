import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Header.css';
import logoImg from '../../assets/images/logo.png';

const Header = () => {
    const [isNavActive, setIsNavActive] = useState(false);
    const [isProfileExpanded, setIsProfileExpanded] = useState(false);
    const [session, setSession] = useState(null);

    const location = useLocation();
    const navigate = useNavigate();
    const path = location.pathname;
    const pageName = path.split('/').pop() || "index";

    useEffect(() => {
        const checkSession = async () => {
            try {
                const res = await axios.get('/api/account/session');
                if (res.data) setSession(res.data);
            } catch (err) {
                setSession(null);
            }
        };
        checkSession();
    }, []);

    useEffect(() => {
        const handleOutsideClick = (e) => {
            if (isProfileExpanded && !e.target.closest('#userProfileContainer')) {
                setIsProfileExpanded(false);
            }
        };
        document.addEventListener('click', handleOutsideClick);
        return () => document.removeEventListener('click', handleOutsideClick);
    }, [isProfileExpanded]);

    const handleLogout = async (e) => {
        e.preventDefault();
        const confirmMsg = "로그아웃 하시겠습니까?";
        if (window.confirm(confirmMsg)) {
            try {
                const res = await axios.post('/account/logout');
                if (res.data.result === 1) window.location.href = "/";
            } catch (err) {
                window.location.href = "/";
            }
        }
    };

    const handleSigninClick = () => {
        if (pageName === 'login') {
            navigate('/account/register');
        } else {
            navigate('/account/login');
        }
    };

    const isHidden = (index) => {
        const hideMap = {
            login: [0, 1, 2],
            register: [0, 1, 2, 3],
            profile: [3],
            chat: [3],
            list: [3]
        };
        return hideMap[pageName]?.includes(index) || false;
    };

    return (
        <header>
            <Link to="/" className="logo-container">
                <img src={logoImg} alt="MAUM" className="logo-image" />
            </Link>

            <div className="menu-toggle" id="mobileMenu" onClick={() => setIsNavActive(!isNavActive)}>
                <span className="bar"></span>
                <span className="bar"></span>
                <span className="bar"></span>
            </div>

            <ul className={`nav-menu ${isNavActive ? 'active' : ''}`}>
                {!isHidden(0) && (
                    <li className="nav-item">
                        <Link to="/map/centerMap" className="nav-link">주변 상담소</Link>
                    </li>
                )}
                {!isHidden(1) && (
                    <li className="nav-item">
                        <Link to="/chat/chat" className="nav-link">오늘의 대화</Link>
                    </li>
                )}
                {!isHidden(2) && (
                    <li className="nav-item">
                        <Link to="/diary/write" className="nav-link">일기 작성</Link>
                    </li>
                )}
                {!isHidden(3) && (
                    <li className="nav-item">
                        {!session ? (
                            <button className="btn-signin" onClick={handleSigninClick}>
                                {pageName === 'login' ? '회원가입' : '로그인'}
                            </button>
                        ) : (
                            <div
                                className={`user-profile-container ${isProfileExpanded ? 'expanded' : ''}`}
                                id="userProfileContainer"
                            >
                                <img
                                    src={session.profileImgUrl || '/images/account/profile7.png'}
                                    alt="프로필"
                                    className="profile-img"
                                />
                                <span className="user-name">{session.userName}님</span>
                                <div className="expanded-menus">
                                    <Link to="/diary/list">일기 목록</Link>
                                    <Link to="/mypage/main">마이페이지</Link>
                                    <a href="#" onClick={handleLogout}>로그아웃</a>
                                </div>
                                <button className="btn-more" onClick={(e) => {
                                    e.stopPropagation();
                                    setIsProfileExpanded(!isProfileExpanded);
                                }}>
                                    &#8942;
                                </button>
                            </div>
                        )}
                    </li>
                )}
            </ul>
        </header>
    );
};

export default Header;