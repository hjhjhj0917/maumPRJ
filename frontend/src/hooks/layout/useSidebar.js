import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { logoutUser } from '../../api/authApi';
import {getRecentDiaries } from "../../api/diaryApi.js";

export const useSidebar = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [showLogoutModal, setShowLogoutModal] = useState(false);

    const toggleSidebar = () => setIsSidebarOpen(!isSidebarOpen);

    const handleLogoutClick = (e) => {
        if (e) e.preventDefault();
        setShowLogoutModal(true);
    };

    const confirmLogout = async () => {
        try {
            const res = await logoutUser();

            if (res && res.data && res.data.result === 1) {
                setShowLogoutModal(false);
                navigate('/');
            } else {
                console.error("로그아웃 실패:", res.message);
                navigate('/');
            }
        } catch (error) {
            console.error("로그아웃 통신 에러:", error);
            navigate('/');
        }
    };

    const isActive = (path) => location.pathname === path;

    const [recentDiaries, setRecentDiaries] = useState([]);

    useEffect(() => {
        const fetchRecent = async () => {
            try {
                const res = await getRecentDiaries();

                if (res) {
                    const data = Array.isArray(res) ? res : [res];
                    setRecentDiaries(data);
                }
            } catch (err) {
                console.error("최신 일기 로드 실패", err);
            }
        };
        fetchRecent();

        window.addEventListener('diary-updated', fetchRecent);

        return () => window.removeEventListener('diary-updated', fetchRecent);
    }, []);

    return {
        isSidebarOpen,
        showLogoutModal,
        setShowLogoutModal,
        toggleSidebar,
        handleLogoutClick,
        confirmLogout,
        isActive,
        recentDiaries,
        navigate
    };
};