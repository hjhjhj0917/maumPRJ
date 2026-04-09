import { useState, useEffect, useRef } from 'react';

export const useIndex = () => {
    const [currentSectionIndex, setCurrentSectionIndex] = useState(0);
    const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
    const [stats, setStats] = useState({ records: 0, comforted: 0, hours: 0 });

    const isScrolling = useRef(false);
    const indexRef = useRef(0);
    const statsRef = useRef(null);
    const hasAnimated = useRef(false);

    const totalSections = 3;

    useEffect(() => {
        const handleResize = () => setIsMobile(window.innerWidth <= 768);
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    useEffect(() => {
        const changeSection = (newIndex) => {
            isScrolling.current = true;
            setCurrentSectionIndex(newIndex);
            indexRef.current = newIndex;
            setTimeout(() => { isScrolling.current = false; }, 800);
        };

        const handleWheel = (e) => {
            if (isMobile || isScrolling.current) return;
            if (e.deltaY > 0 && indexRef.current < totalSections - 1) changeSection(indexRef.current + 1);
            else if (e.deltaY < 0 && indexRef.current > 0) changeSection(indexRef.current - 1);
        };

        const handleKeyDown = (e) => {
            if (isMobile || isScrolling.current) return;
            if (e.key === "ArrowDown" && indexRef.current < totalSections - 1) changeSection(indexRef.current + 1);
            else if (e.key === "ArrowUp" && indexRef.current > 0) changeSection(indexRef.current - 1);
        };

        window.addEventListener("wheel", handleWheel, { passive: false });
        window.addEventListener("keydown", handleKeyDown);
        return () => {
            window.removeEventListener("wheel", handleWheel);
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [isMobile]);

    useEffect(() => {
        const animateValue = (key, target) => {
            let startTimestamp = null;
            const duration = 2000;
            const step = (timestamp) => {
                if (!startTimestamp) startTimestamp = timestamp;
                const progress = Math.min((timestamp - startTimestamp) / duration, 1);
                const easeOutQuart = 1 - Math.pow(1 - progress, 4);
                setStats(prev => ({ ...prev, [key]: Math.floor(easeOutQuart * target) }));
                if (progress < 1) window.requestAnimationFrame(step);
                else setStats(prev => ({ ...prev, [key]: target }));
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
        return () => { if (statsRef.current) observer.unobserve(statsRef.current); };
    }, []);

    return { currentSectionIndex, isMobile, stats, statsRef };
};