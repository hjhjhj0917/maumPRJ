import React from 'react';
import { Link } from 'react-router-dom';
import mainBg from '../../assets/images/main-background.png';

const HeroSection = ({ isActive }) => {
    return (
        <section className={`hero-section ${isActive ? 'active' : ''}`}>
            <div className="hero-bg" style={{ backgroundImage: `url(${mainBg})` }}></div>
            <div className="hero-overlay"></div>
            <div className="hero-container">
                <div className="hero-text-box scroll-target">
                    <h1 className="main-title">
                        수많은 소음 속,<br />
                        <span className="highlight">온전히 나에게 집중하는 시간</span>
                    </h1>
                    <p className="sub-title">
                        오늘 하루, 당신의 마음은 안녕한가요?<br />
                        누구에게도 말 못한 고민이 있다면 MAUM에 털어놓으세요.<br />
                        AI가 당신의 하루를 듣고 따뜻한 위로와 분석을 건넵니다.
                    </p>
                    <div className="hero-buttons">
                        <Link to="/account/login" className="btn-primary">시작하기</Link>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default HeroSection;