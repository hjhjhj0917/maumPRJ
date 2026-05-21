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
                     recentDiaries,
                     navigate
                 }) => {
    return (
        <>
            <S.SidebarWrapper $isOpen={isOpen}>
                <S.TopSection>
                    <S.IconButton $isOpen={isOpen} onClick={toggleSidebar}>
                        <i className="fa-solid fa-bars"></i>
                    </S.IconButton>
                    <S.NewPostBtn $isOpen={isOpen} onClick={isOpen ? () => navigate('/account/profile') : undefined}>
                        <i className="fa-solid fa-user-gear"></i>
                        {isOpen && <span>마이페이지</span>}
                    </S.NewPostBtn>
                </S.TopSection>

                <S.NavSection>
                    <S.NavItem $isOpen={isOpen} $active={isActive('/chatbot')}
                               onClick={isOpen ? () => navigate('/chatbot') : undefined}>
                        <i className="fa-solid fa-robot"></i>
                        {isOpen && <span>챗봇</span>}
                    </S.NavItem>
                    <S.NavItem $isOpen={isOpen} $active={isActive('/map')}
                               onClick={isOpen ? () => navigate('/map') : undefined}>
                        <i className="fa-solid fa-map-location-dot"></i>
                        {isOpen && <span>주변 상담소</span>}
                    </S.NavItem>
                    <S.NavItem $isOpen={isOpen} $active={isActive('/diary/list')}
                               onClick={isOpen ? () => navigate('/diary/list') : undefined}>
                        <i className="fa-solid fa-bars-staggered"></i>
                        {isOpen && <span>일기 목록</span>}
                    </S.NavItem>

                    {isOpen && recentDiaries.length > 0 && (
                        <S.RecentDiarySection>
                            <S.RecentDiaryTitle>최근 일기</S.RecentDiaryTitle>
                            {recentDiaries.map((diary) => (
                                <S.RecentDiaryItem
                                    key={diary.diaryNo}
                                    onClick={() => navigate(`/diary/${diary.diaryNo}`)}
                                    title={diary.title}
                                >
                                    {diary.title}
                                </S.RecentDiaryItem>
                            ))}
                        </S.RecentDiarySection>
                    )}
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