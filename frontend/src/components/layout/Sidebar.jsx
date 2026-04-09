import React from 'react';
import * as S from '../../style/components/layout/Sidebar.styles';

const Sidebar = ({ isOpen, toggleSidebar, onLogout, isActive, navigate }) => {
    return (
        <S.SidebarWrapper $isOpen={isOpen}>
            <S.TopSection>
                <S.IconButton onClick={toggleSidebar}>
                    <i className="fa-solid fa-bars"></i>
                </S.IconButton>
                <S.NewPostBtn onClick={() => navigate('/diary/write')} $isOpen={isOpen}>
                    <i className="fa-solid fa-plus"></i>
                    {isOpen && <span>새 일기 작성</span>}
                </S.NewPostBtn>
            </S.TopSection>

            <S.NavSection>
                <S.NavItem $active={isActive('/counseling')} onClick={() => navigate('/counseling')}>
                    <i className="fa-solid fa-house-medical"></i>
                    {isOpen && <span>주변 상담소</span>}
                </S.NavItem>
                <S.NavItem $active={isActive('/diary/list')} onClick={() => navigate('/diary/list')}>
                    <i className="fa-solid fa-book"></i>
                    {isOpen && <span>일기 목록</span>}
                </S.NavItem>
                <S.NavItem $active={isActive('/chatbot')} onClick={() => navigate('/chatbot')}>
                    <i className="fa-solid fa-comment-dots"></i>
                    {isOpen && <span>대화형 챗봇</span>}
                </S.NavItem>
            </S.NavSection>

            <S.BottomSection>
                <S.NavItem onClick={onLogout}>
                    <i className="fa-solid fa-right-from-bracket"></i>
                    {isOpen && <span>로그아웃</span>}
                </S.NavItem>
            </S.BottomSection>
        </S.SidebarWrapper>
    );
};

export default Sidebar;