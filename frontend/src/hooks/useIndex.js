import { useState, useEffect, useRef, useCallback } from 'react';

export const useDraggable = (initialX, initialY) => {
    const [position, setPosition] = useState({ x: initialX, y: initialY });
    const [isDragging, setIsDragging] = useState(false);
    const dragInfo = useRef({ startX: 0, startY: 0, elStartX: 0, elStartY: 0 });

    const onPointerDown = useCallback((e) => {
        e.stopPropagation();
        e.target.setPointerCapture(e.pointerId);
        setIsDragging(true);
        dragInfo.current = {
            startX: e.clientX,
            startY: e.clientY,
            elStartX: position.x,
            elStartY: position.y,
        };
    }, [position]);

    const onPointerMove = useCallback((e) => {
        if (!isDragging) return;
        const dx = e.clientX - dragInfo.current.startX;
        const dy = e.clientY - dragInfo.current.startY;
        setPosition({
            x: dragInfo.current.elStartX + dx,
            y: dragInfo.current.elStartY + dy,
        });
    }, [isDragging]);

    const onPointerUp = useCallback((e) => {
        e.target.releasePointerCapture(e.pointerId);
        setIsDragging(false);
    }, []);

    return {
        dragProps: {
            style: {
                transform: `translate(${position.x}px, ${position.y}px)`,
                cursor: isDragging ? 'grabbing' : 'grab',
                zIndex: isDragging ? 1000 : 60,
                position: 'absolute'
            },
            onPointerDown,
            onPointerMove,
            onPointerUp
        },
        isDragging
    };
};

export const useIndex = () => {
    const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
    const [scrolled, setScrolled] = useState(false);

    const [stats, setStats] = useState({
        lessons: 0,
        workshops: 0,
        challenges: 0,
        friends: 0,
        members: 0,
        nationalities: 0
    });

    const statsRef1 = useRef(null);
    const statsRef2 = useRef(null);
    const hasAnimated1 = useRef(false);
    const hasAnimated2 = useRef(false);

    useEffect(() => {
        const handleResize = () => setIsMobile(window.innerWidth <= 768);
        const handleScroll = () => setScrolled(window.scrollY > 50);

        window.addEventListener('resize', handleResize);
        window.addEventListener('scroll', handleScroll);

        return () => {
            window.removeEventListener('resize', handleResize);
            window.removeEventListener('scroll', handleScroll);
        };
    }, []);

    useEffect(() => {
        const animateValue = (key, target, duration = 2000) => {
            let startTimestamp = null;
            const step = (timestamp) => {
                if (!startTimestamp) startTimestamp = timestamp;
                const progress = Math.min((timestamp - startTimestamp) / duration, 1);
                const easeOutQuart = 1 - Math.pow(1 - progress, 4);

                setStats(prev => ({ ...prev, [key]: Math.floor(easeOutQuart * target) }));

                if (progress < 1) {
                    window.requestAnimationFrame(step);
                } else {
                    setStats(prev => ({ ...prev, [key]: target }));
                }
            };
            window.requestAnimationFrame(step);
        };

        const observer1 = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !hasAnimated1.current) {
                    hasAnimated1.current = true;
                    animateValue('lessons', 42);
                    animateValue('workshops', 36);
                    animateValue('challenges', 12);
                }
            });
        }, { threshold: 0.5 });

        const observer2 = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !hasAnimated2.current) {
                    hasAnimated2.current = true;
                    animateValue('friends', 15000);
                    animateValue('members', 1250);
                    animateValue('nationalities', 20);
                }
            });
        }, { threshold: 0.5 });

        if (statsRef1.current) observer1.observe(statsRef1.current);
        if (statsRef2.current) observer2.observe(statsRef2.current);

        return () => {
            if (statsRef1.current) observer1.unobserve(statsRef1.current);
            if (statsRef2.current) observer2.unobserve(statsRef2.current);
        };
    }, []);

    return { isMobile, scrolled, stats, statsRef1, statsRef2 };
};