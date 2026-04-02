import React, { useState, useEffect, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './Header.css';

import logoImg from '../../assets/images/includes/logo.png';
import baseProfileImg from '../../assets/images/account/profile7.png';

const Header = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [isProfileExpanded, setIsProfileExpanded] = useState(false);
    const [user, setUser] = useState(null);
    const profileRef = useRef(null);

    const location = useLocation();
    const navigate = useNavigate();

    const pathName = location.pathname.split('/').pop() || 'index';

    const isAccountPage = location.pathname.startsWith('/account');

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (profileRef.current && !profileRef.current.contains(event.target)) {
                setIsProfileExpanded(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const toggleMobileMenu = () => setIsMobileMenuOpen(!isMobileMenuOpen);

    const toggleProfile = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setIsProfileExpanded(!isProfileExpanded);
    };

    const handleLogout = async (e) => {
        e.preventDefault();
        if (window.confirm("로그아웃 하시겠습니까?")) {
            try {
                const response = await fetch('/api/account/logout', { method: 'POST' });
                const res = await response.json();
                if (res.result === 1) {
                    setUser(null);
                    navigate('/');
                } else {
                    alert("실패: " + res.msg);
                }
            } catch (error) {
                navigate('/');
            }
        }
    };

    const handleSignClick = () => {
        if (pathName === 'login') {
            navigate('/account/register');
        } else {
            navigate('/account/login');
        }
    };

    const hideMenus = ['login', 'register'].includes(pathName);
    const hideSignBtn = ['profile', 'chat', 'list'].includes(pathName);

    return (
        <header className={isAccountPage ? 'header-transparent' : 'header-solid'}>
            <Link to={user ? '/main' : '/'} className="logo-container">
                <img src={logoImg} alt="MAUM" className="logo-image" />
            </Link>

            <div className="menu-toggle" onClick={toggleMobileMenu}>
                <span className="bar"></span>
                <span className="bar"></span>
                <span className="bar"></span>
            </div>

            <ul className={`nav-menu ${isMobileMenuOpen ? 'active' : ''}`}>
                {!hideMenus && (
                    <>
                        <li className="nav-item">
                            <Link to="/map/centerMap" className="nav-link">주변 상담소</Link>
                        </li>
                        <li className="nav-item">
                            <Link to="/chat/chat" className="nav-link">오늘의 대화</Link>
                        </li>
                        <li className="nav-item">
                            <Link to="/diary/write" className="nav-link">일기 작성</Link>
                        </li>
                    </>
                )}

                <li className="nav-item">
                    {!user ? (
                        !hideSignBtn && (
                            <button className="btn-signin" onClick={handleSignClick}>
                                {pathName === 'login' ? '회원가입' : '로그인'}
                            </button>
                        )
                    ) : (
                        <div
                            className={`user-profile-container ${isProfileExpanded ? 'expanded' : ''}`}
                            ref={profileRef}
                        >
                            <img src={user.profileImg || baseProfileImg} alt="프로필" className="profile-img" />
                            <span className="user-name">{user.name}님</span>

                            <div className="expanded-menus">
                                <Link to="/diary/list">일기 목록</Link>
                                <Link to="/mypage/main">마이페이지</Link>
                                <button onClick={handleLogout} className="btn-logout-text">로그아웃</button>
                            </div>

                            <button className="btn-more" onClick={toggleProfile}>
                                &#8942;
                            </button>
                        </div>
                    )}
                </li>
            </ul>
        </header>
    );
};

export default Header;