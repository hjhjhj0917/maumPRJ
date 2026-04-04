import React, { useEffect, useState, useRef } from 'react';
import { Link } from 'react-router-dom';
import './Home.css';
import mainBg from '../assets/images/main-background.png';

const StatItem = ({ targetValue, label }) => {
    const [displayValue, setDisplayValue] = useState(0);
    const elementRef = useRef(null);
    const hasAnimated = useRef(false);

    useEffect(() => {
        const observer = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && !hasAnimated.current) {
                animate();
                hasAnimated.current = true;
            }
        }, { threshold: 0.5 });

        if (elementRef.current) observer.observe(elementRef.current);
        return () => observer.disconnect();
    }, [targetValue]);

    const animate = () => {
        let startTimestamp = null;
        const duration = 2000;
        const step = (timestamp) => {
            if (!startTimestamp) startTimestamp = timestamp;
            const progress = Math.min((timestamp - startTimestamp) / duration, 1);
            const easeOutQuart = 1 - Math.pow(1 - progress, 4);
            setDisplayValue(Math.floor(easeOutQuart * targetValue));
            if (progress < 1) window.requestAnimationFrame(step);
        };
        window.requestAnimationFrame(step);
    };

    return (
        <div className="stat-item" ref={elementRef}>
            <span className="stat-number">{displayValue.toLocaleString()}</span>
            <span className="stat-label">{label}</span>
        </div>
    );
};

const Home = () => {
    const [currentIndex, setCurrentIndex] = useState(0);
    const isScrolling = useRef(false);
    const totalSections = 3;

    useEffect(() => {
        const handleWheel = (e) => {
            if (window.innerWidth <= 768 || isScrolling.current) return;

            if (e.deltaY > 0 && currentIndex < totalSections - 1) {
                moveSection(currentIndex + 1);
            } else if (e.deltaY < 0 && currentIndex > 0) {
                moveSection(currentIndex - 1);
            }
        };

        const handleKeyDown = (e) => {
            if (window.innerWidth <= 768 || isScrolling.current) return;
            if (e.key === "ArrowDown" && currentIndex < totalSections - 1) {
                moveSection(currentIndex + 1);
            } else if (e.key === "ArrowUp" && currentIndex > 0) {
                moveSection(currentIndex - 1);
            }
        };

        window.addEventListener("wheel", handleWheel, { passive: false });
        window.addEventListener("keydown", handleKeyDown);
        return () => {
            window.removeEventListener("wheel", handleWheel);
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [currentIndex]);

    const moveSection = (index) => {
        isScrolling.current = true;
        setCurrentIndex(index);
        setTimeout(() => {
            isScrolling.current = false;
        }, 800);
    };

    const getTransformStyle = () => {
        if (window.innerWidth <= 768) return {};
        return { transform: `translateY(-${currentIndex * 100}vh)` };
    };

    return (
        <div className="home-container" style={getTransformStyle()}>
            <section className={`hero-section full-page ${currentIndex === 0 ? 'active' : ''}`}>
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

            <section className={`hero-section full-page ${currentIndex === 1 ? 'active' : ''}`}>
                <div className="split-container">
                    <Link to="/diary/write" className="split-panel">
                        <div className="panel-overlay"></div>
                        <div className="panel-content"><h4>일기 작성하기</h4></div>
                    </Link>
                    <Link to="/diary/analysis" className="split-panel">
                        <div className="panel-overlay"></div>
                        <div className="panel-content"><h4>분석 결과</h4></div>
                    </Link>
                    <Link to="/diary/list" className="split-panel">
                        <div className="panel-overlay"></div>
                        <div className="panel-content"><h4>일기 보관소</h4></div>
                    </Link>
                </div>
            </section>

            <div className={`footer-wrapper full-page ${currentIndex === 2 ? 'active' : ''}`}>
                <div className="stats-section">
                    <StatItem targetValue={1204} label="기록된 마음" />
                    <div className="stat-divider"></div>
                    <StatItem targetValue={582} label="위로받은 사람" />
                    <div className="stat-divider"></div>
                    <StatItem targetValue={365} label="함께한 시간" />
                </div>
                {/* Footer 컴포넌트는 별도 생성 후 App.jsx 등에서 배치 권장 */}
            </div>
        </div>
    );
};

export default Home;