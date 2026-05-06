import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { getUserStatus } from '../../api/authApi';

export const useHeader = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [user, setUser] = useState(null);
    const location = useLocation();

    const isLoginPage = location.pathname === '/account/login';
    const isRegisterPage = location.pathname === '/account/register';

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const fetchUserStatus = async () => {
        try {
            const res = await getUserStatus();

            const userData = res.data;

            if (userData && userData.userId) {
                setUser({
                    no: userData.userNo,
                    id: userData.userId,
                    name: userData.userName,
                    profileImg: userData.profileImgUrl
                });
            } else {
                setUser(null);
            }
        } catch (error) {
            setUser(null);
        }
    };

    useEffect(() => {
        const loginFlag = getCookie('isLoggedIn');

        if (!isLoginPage && !isRegisterPage && loginFlag === 'true') {
            fetchUserStatus();
        } else {
            setUser(null);
        }

        setIsMobileMenuOpen(false);

    }, [location.pathname, isLoginPage, isRegisterPage]);

    const toggleMobileMenu = () => setIsMobileMenuOpen(!isMobileMenuOpen);

    return {
        isMobileMenuOpen,
        user,
        toggleMobileMenu,
        isLoginPage,
        isRegisterPage
    };
};