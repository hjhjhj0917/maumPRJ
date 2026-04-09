import React from 'react';
import { Link } from 'react-router-dom';
import CustomModal from '../common/CustomModal';
import { useHeader } from '../../hooks/layout/useHeader';
import logoImg from '../../assets/images/includes/logo.png';
import * as S from '../../style/components/layout/Header.styles';

const Header = () => {
    const {
        isMobileMenuOpen, isProfileExpanded, user, showLogoutModal, setShowLogoutModal,
        profileRef, toggleMobileMenu, toggleProfile, handleLogoutClick, confirmLogout
    } = useHeader();

    return (
        <>
            <S.HeaderContainer>
                <S.LogoContainer to={user ? '/main' : '/'}>
                    <S.LogoImage src={logoImg} alt="MAUM" />
                </S.LogoContainer>

                <S.MenuToggle onClick={toggleMobileMenu}>
                    <S.Bar />
                    <S.Bar />
                    <S.Bar />
                </S.MenuToggle>

                <S.NavMenu $isOpen={isMobileMenuOpen}>
                    {!user ? (
                        <>
                            <S.NavItem>
                                <S.NavLink to="/account/register">회원가입</S.NavLink>
                            </S.NavItem>
                            <S.NavItem>
                                <S.NavLink to="/account/login">로그인</S.NavLink>
                            </S.NavItem>
                        </>
                    ) : (
                        <S.NavItem>
                            <S.UserProfileContainer $isExpanded={isProfileExpanded} ref={profileRef}>
                                <S.ProfileImg src={user.profileImg} alt="프로필" />
                                <S.UserName $isExpanded={isProfileExpanded}>{user.name}님</S.UserName>

                                <S.ExpandedMenus $isExpanded={isProfileExpanded}>
                                    <Link to="/diary/list">일기 목록</Link>
                                    <Link to="/mypage/main">마이페이지</Link>
                                    <button onClick={handleLogoutClick}>로그아웃</button>
                                </S.ExpandedMenus>

                                <S.BtnMore onClick={toggleProfile}>
                                    &#8942;
                                </S.BtnMore>
                            </S.UserProfileContainer>
                        </S.NavItem>
                    )}
                </S.NavMenu>
            </S.HeaderContainer>

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

export default Header;