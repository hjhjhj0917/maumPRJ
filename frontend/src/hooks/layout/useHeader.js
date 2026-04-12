import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { getUserStatus } from '../../api/authApi';

export const useHeader = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [user, setUser] = useState(null);
    const location = useLocation();

    const isLoginPage = location.pathname === '/account/login';
    const isRegisterPage = location.pathname === '/account/register';

    const fetchUserStatus = async () => {
        try {
            const data = await getUserStatus();
            if (data && data.userNo) {
                setUser({
                    no: data.userNo,
                    id: data.userId,
                    name: data.userName,
                    profileImg: data.profileImgUrl
                });
            } else {
                setUser(null);
            }
        } catch (error) {
            setUser(null);
        }
    };

    useEffect(() => {
        fetchUserStatus();
    }, [location.pathname]);

    const toggleMobileMenu = () => setIsMobileMenuOpen(!isMobileMenuOpen);

    return {
        isMobileMenuOpen,
        user,
        toggleMobileMenu,
        isLoginPage,
        isRegisterPage
    };
};