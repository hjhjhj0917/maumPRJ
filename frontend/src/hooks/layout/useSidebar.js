import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { logoutUser } from '../../api/authApi';

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
            if (res.result === 1) {
                setShowLogoutModal(false);
                navigate('/');
            }
        } catch (error) {
            navigate('/');
        }
    };

    const isActive = (path) => location.pathname === path;

    return {
        isSidebarOpen,
        showLogoutModal,
        setShowLogoutModal,
        toggleSidebar,
        handleLogoutClick,
        confirmLogout,
        isActive,
        navigate
    };
};