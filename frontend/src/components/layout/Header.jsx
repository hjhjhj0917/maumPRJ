import React from 'react';
import { Link } from 'react-router-dom';
import CustomModal from '../common/CustomModal';
import { useHeader } from '../../hooks/layout/useHeader';
import logoImg from '../../assets/images/includes/logo.png';
import * as S from '../../style/components/layout/Header.styles';

const Header = () => {
    const {
        isMobileMenuOpen, isProfileExpanded, user, showLogoutModal, setShowLogoutModal,
        profileRef, isAccountPage, pathName, hideMenus, hideSignBtn,
        toggleMobileMenu, toggleProfile, handleLogoutClick, confirmLogout, handleSignClick
    } = useHeader();

    return (
        <>
            <S.HeaderContainer $isAccountPage={isAccountPage}>
                <S.LogoContainer to={user ? '/main' : '/'}>
                    <S.LogoImage src={logoImg} alt="MAUM" />
                </S.LogoContainer>

                <S.MenuToggle onClick={toggleMobileMenu}>
                    <S.Bar />
                    <S.Bar />
                    <S.Bar />
                </S.MenuToggle>

                <S.NavMenu $isOpen={isMobileMenuOpen}>
                    {!hideMenus && (
                        <>
                            <S.NavItem>
                                <S.NavLink to="/map/centerMap">주변 상담소</S.NavLink>
                            </S.NavItem>
                            <S.NavItem>
                                <S.NavLink to="/chat/chat">오늘의 대화</S.NavLink>
                            </S.NavItem>
                            <S.NavItem>
                                <S.NavLink to="/diary/write">일기 작성</S.NavLink>
                            </S.NavItem>
                        </>
                    )}

                    <S.NavItem>
                        {!user ? (
                            !hideSignBtn && (
                                <S.BtnSignin onClick={handleSignClick}>
                                    {pathName === 'login' ? '회원가입' : '로그인'}
                                </S.BtnSignin>
                            )
                        ) : (
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
                        )}
                    </S.NavItem>
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