import React from 'react';
import CustomModal from '../common/CustomModal';
import * as S from '../../style/components/layout/Sidebar.styles';

const Sidebar = ({
                     isOpen,
                     toggleSidebar,
                     handleLogoutClick,
                     confirmLogout,
                     showLogoutModal,
                     setShowLogoutModal,
                     isActive,
                     navigate
                 }) => {
    return (
        <>
            <S.SidebarWrapper $isOpen={isOpen}>
                <S.TopSection>
                    <S.IconButton $isOpen={isOpen} onClick={toggleSidebar}>
                        <i className="fa-solid fa-bars"></i>
                    </S.IconButton>
                    <S.NewPostBtn $isOpen={isOpen} onClick={isOpen ? () => navigate('/diary/write') : undefined}>
                        <i className="fa-solid fa-user-gear"></i>
                        {isOpen && <span>프로필 설정</span>}
                    </S.NewPostBtn>
                </S.TopSection>

                <S.NavSection>
                    <S.NavItem $isOpen={isOpen} $active={isActive('/chatbot')} onClick={isOpen ? () => navigate('/chatbot') : undefined}>
                        <i className="fa-solid fa-robot"></i>
                        {isOpen && <span>챗봇</span>}
                    </S.NavItem>
                    <S.NavItem $isOpen={isOpen} $active={isActive('/diary/list')} onClick={isOpen ? () => navigate('/diary/list') : undefined}>
                        <i className="fa-solid fa-bars-staggered"></i>
                        {isOpen && <span>일기 목록</span>}
                    </S.NavItem>
                    <S.NavItem $isOpen={isOpen} $active={isActive('/counseling')} onClick={isOpen ? () => navigate('/counseling') : undefined}>
                        <i className="fa-solid fa-map-location-dot"></i>
                        {isOpen && <span>주변 상담소</span>}
                    </S.NavItem>
                </S.NavSection>

                <S.BottomSection>
                    <S.NavItem $isOpen={isOpen} onClick={isOpen ? handleLogoutClick : undefined}>
                        <i className="fa-solid fa-gear"></i>
                        {isOpen && <span>로그아웃</span>}
                    </S.NavItem>
                </S.BottomSection>
            </S.SidebarWrapper>

            <CustomModal
                isOpen={showLogoutModal}
                title="로그아웃"
                message="정말 로그아웃 하시겠습니까?"
                isConfirm={true}
                onConfirm={confirmLogout}
                onCancel={() => setShowLogoutModal(false)}
            />
        </>
    );
};

export default Sidebar;