import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';
import { useSidebar } from '../../hooks/layout/useSidebar';
import * as S from '../../style/components/layout/AppLayout.styles';

const Layout = () => {
    const {
        isSidebarOpen,
        showLogoutModal,
        setShowLogoutModal,
        toggleSidebar,
        handleLogoutClick,
        confirmLogout,
        isActive,
        navigate
    } = useSidebar();

    return (
        <S.LayoutWrapper>
            <Sidebar
                isOpen={isSidebarOpen}
                toggleSidebar={toggleSidebar}
                handleLogoutClick={handleLogoutClick}
                confirmLogout={confirmLogout}
                showLogoutModal={showLogoutModal}
                setShowLogoutModal={setShowLogoutModal}
                isActive={isActive}
                navigate={navigate}
            />

            <S.MainWrapper>
                <Header />
                <S.LayoutContent>
                    <Outlet />
                </S.LayoutContent>
            </S.MainWrapper>
        </S.LayoutWrapper>
    );
};

export default Layout;