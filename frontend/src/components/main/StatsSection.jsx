import React, { useState, useEffect, useRef } from 'react';
import Footer from '../common/Footer';

const StatsSection = ({ isActive }) => {
    const [stats, setStats] = useState({ records: 0, comforted: 0, hours: 0 });
    const statsRef = useRef(null);
    const hasAnimated = useRef(false);

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

        if (statsRef.current) observer.observe(statsRef.current);
        return () => {
            if (statsRef.current) observer.unobserve(statsRef.current);
        };
    }, []);

    return (
        <div className={`footer-wrapper ${isActive ? 'active' : ''}`}>
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
    );
};

export default StatsSection;