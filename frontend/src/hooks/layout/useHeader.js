import { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getUserStatus, logoutUser } from '../../api/authApi';

export const useHeader = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [isProfileExpanded, setIsProfileExpanded] = useState(false);
    const [user, setUser] = useState(null);
    const [showLogoutModal, setShowLogoutModal] = useState(false);

    const profileRef = useRef(null);
    const location = useLocation();
    const navigate = useNavigate();

    const isAccountPage = location.pathname.startsWith('/account');

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

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (profileRef.current && !profileRef.current.contains(event.target)) {
                setIsProfileExpanded(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const toggleMobileMenu = () => setIsMobileMenuOpen(!isMobileMenuOpen);

    const toggleProfile = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setIsProfileExpanded(!isProfileExpanded);
    };

    const handleLogoutClick = (e) => {
        e.preventDefault();
        setShowLogoutModal(true);
    };

    const confirmLogout = async () => {
        try {
            const res = await logoutUser();
            if (res.result === 1) {
                setUser(null);
                setShowLogoutModal(false);
                navigate('/');
            }
        } catch (error) {
            navigate('/');
        }
    };

    return {
        isMobileMenuOpen, isProfileExpanded, user, showLogoutModal, setShowLogoutModal,
        profileRef, isAccountPage,
        toggleMobileMenu, toggleProfile, handleLogoutClick, confirmLogout
    };
};