import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export const useSidebar = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);

    const toggleSidebar = () => setIsSidebarOpen(!isSidebarOpen);

    const handleLogout = () => {
        localStorage.removeItem('userToken');
        navigate('/account/login');
    };

    const isActive = (path) => location.pathname === path;

    return {
        isSidebarOpen,
        toggleSidebar,
        handleLogout,
        isActive,
        navigate
    };
};