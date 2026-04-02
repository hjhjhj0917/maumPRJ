import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/common/Header';
import Footer from '../components/common/Footer';
import './Index.css';

import mainBg from '../assets/images/main-background.png';

const Main = () => {
    const [currentSectionIndex, setCurrentSectionIndex] = useState(0);
    const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
    const [stats, setStats] = useState({ records: 0, comforted: 0, hours: 0 });

    const isScrolling = useRef(false);
    const indexRef = useRef(0);
    const statsRef = useRef(null);
    const hasAnimated = useRef(false);

    const totalSections = 3;

    useEffect(() => {
        const handleResize = () => {
            setIsMobile(window.innerWidth <= 768);
        };
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    useEffect(() => {
        const changeSection = (newIndex) => {
            isScrolling.current = true;
            setCurrentSectionIndex(newIndex);
            indexRef.current = newIndex;
            setTimeout(() => {
                isScrolling.current = false;
            }, 800);
        };

        const handleWheel = (e) => {
            if (window.innerWidth <= 768) return;
            e.preventDefault();
            if (isScrolling.current) return;

            if (e.deltaY > 0 && indexRef.current < totalSections - 1) {
                changeSection(indexRef.current + 1);
            } else if (e.deltaY < 0 && indexRef.current > 0) {
                changeSection(indexRef.current - 1);
            }
        };

        const handleKeyDown = (e) => {
            if (window.innerWidth <= 768) return;
            if (isScrolling.current) return;

            if (e.key === "ArrowDown" && indexRef.current < totalSections - 1) {
                changeSection(indexRef.current + 1);
            } else if (e.key === "ArrowUp" && indexRef.current > 0) {
                changeSection(indexRef.current - 1);
            }
        };

        window.addEventListener("wheel", handleWheel, { passive: false });
        window.addEventListener("keydown", handleKeyDown);

        return () => {
            window.removeEventListener("wheel", handleWheel);
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, []);

    useEffect(() => {
        const animateValue = (key, target) => {
            let startTimestamp = null;
            const duration = 2000;

            const step = (timestamp) => {
                if (!startTimestamp) startTimestamp = timestamp;
                const progress = Math.min((timestamp - startTimestamp) / duration, 1);
                const easeOutQuart = 1 - Math.pow(1 - progress, 4);
                const currentVal = Math.floor(easeOutQuart * target);

                setStats(prev => ({ ...prev, [key]: currentVal }));

                if (progress < 1) {
                    window.requestAnimationFrame(step);
                } else {
                    setStats(prev => ({ ...prev, [key]: target }));
                }
            };
            window.requestAnimationFrame(step);
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !hasAnimated.current) {
                    hasAnimated.current = true;
                    animateValue('records', 1204);
                    animateValue('comforted', 582);
                    animateValue('hours', 365);
                }
            });
        }, { threshold: 0.5 });

        if (statsRef.current) {
            observer.observe(statsRef.current);
        }

        return () => {
            if (statsRef.current) observer.unobserve(statsRef.current);
        };
    }, []);

    const getSectionClass = (index) => {
        return `full-page ${isMobile ? 'active' : (currentSectionIndex === index ? 'active' : '')}`;
    };

    const mainTransform = isMobile ? 'none' : `translateY(-${currentSectionIndex * 100}vh)`;

    return (
        <>
            <Header />
            <div className="main-content" style={{ transform: mainTransform }}>

                <section className={`hero-section ${getSectionClass(0)}`}>
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

                <section className={`hero-section ${getSectionClass(1)}`}>
                    <div className="split-container">
                        <Link to="/diary/write" className="split-panel">
                            <div className="panel-overlay"></div>
                            <div className="panel-content">
                                <h4>일기 작성하기</h4>
                            </div>
                        </Link>

                        <Link to="/diary/analysis" className="split-panel">
                            <div className="panel-overlay"></div>
                            <div className="panel-content">
                                <h4>분석 결과</h4>
                            </div>
                        </Link>

                        <Link to="/diary/list" className="split-panel">
                            <div className="panel-overlay"></div>
                            <div className="panel-content">
                                <h4>일기 보관소</h4>
                            </div>
                        </Link>
                    </div>
                </section>

                <div className={`footer-wrapper ${getSectionClass(2)}`}>
                    <div className="stats-section" ref={statsRef}>
                        <div className="stat-item">
                            <span className="stat-number">{stats.records.toLocaleString()}</span>
                            <span className="stat-label">기록된 마음</span>
                        </div>
                        <div className="stat-divider"></div>
                        <div className="stat-item">
                            <span className="stat-number">{stats.comforted.toLocaleString()}</span>
                            <span className="stat-label">위로받은 사람</span>
                        </div>
                        <div className="stat-divider"></div>
                        <div className="stat-item">
                            <span className="stat-number">{stats.hours.toLocaleString()}</span>
                            <span className="stat-label">함께한 시간</span>
                        </div>
                    </div>
                    <div style={{ width: '100%' }}>
                        <Footer />
                    </div>
                </div>

            </div>
        </>
    );
};

export default Main;