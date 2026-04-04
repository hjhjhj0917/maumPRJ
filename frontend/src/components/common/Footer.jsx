import React from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

const Footer = () => {
    return (
        <footer>
            <div className="footer-container">
                <div className="footer-content">
                    <div className="footer-logo">마음MAUM</div>
                    <nav className="footer-links">
                        <a href="https://kopo.ac.kr/kangseo/index.do" target="_blank" rel="noopener noreferrer">복지시설</a>
                        <Link to="/notice/noticeList" className="privacy">심리분석</Link>
                        <Link to="/mypage/withdraw">자세히 알아보기</Link>
                    </nav>
                    <address className="footer-info">
                        <span>(주)마음MAUM</span>
                        <span>메일: <a href="mailto:yjmo0309@gmail.com">yjmo0309@gmail.com</a></span>
                        <span>주소: 서울 강서구 우장산로10길 112 한국폴리텍대학서울강서캠퍼스</span>
                        <span>학과: 빅데이터학과</span>
                    </address>
                    <div className="footer-copyright">
                        &copy; 2025 URI_Ai. All Rights Reserved.
                    </div>
                </div>
                <div className="footer-social">
                    <a href="https://github.com/hjhjhj0917" target="_blank" rel="noopener noreferrer">
                        <i className="fa-brands fa-github"></i>
                    </a>
                    <a href="https://www.instagram.com/poly_kangseo/" target="_blank" rel="noopener noreferrer">
                        <i className="fa-brands fa-instagram"></i>
                    </a>
                    <a href="https://www.youtube.com/channel/UCmzyR9BA0gHRM58o8SjdYjw" target="_blank" rel="noopener noreferrer">
                        <i className="fa-brands fa-youtube"></i>
                    </a>
                </div>
            </div>
        </footer>
    );
};

export default Footer;