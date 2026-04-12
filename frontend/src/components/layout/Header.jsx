import React from 'react';
import { Link } from 'react-router-dom';
import { useHeader } from '../../hooks/layout/useHeader';
import logoImg from '../../assets/images/includes/logo.png';
import * as S from '../../style/components/layout/Header.styles';

const Header = () => {
    const {
        isMobileMenuOpen,
        user,
        toggleMobileMenu,
        isLoginPage,
        isRegisterPage
    } = useHeader();

    return (
        <S.HeaderContainer>
            {user ? (
                <S.LogoContainer as="div">
                    <S.LogoImage src={logoImg} alt="MAUM" />
                </S.LogoContainer>
            ) : (
                <S.LogoContainer to="/">
                    <S.LogoImage src={logoImg} alt="MAUM" />
                </S.LogoContainer>
            )}

            <S.MenuToggle onClick={toggleMobileMenu}>
                <S.Bar />
                <S.Bar />
                <S.Bar />
            </S.MenuToggle>

            <S.NavMenu $isOpen={isMobileMenuOpen}>
                {!user ? (
                    <>
                        {!isRegisterPage && (
                            <S.NavItem>
                                <S.NavLink to="/account/register">회원가입</S.NavLink>
                            </S.NavItem>
                        )}

                        {!isLoginPage && (
                            <S.NavItem>
                                <S.NavLink to="/account/login">로그인</S.NavLink>
                            </S.NavItem>
                        )}
                    </>
                ) : (
                    <S.NavItem>
                        <S.UserProfileContainer>
                            <S.ProfileImg src={user.profileImg} alt="프로필" />
                            <S.UserName>{user.name}님</S.UserName>
                        </S.UserProfileContainer>
                    </S.NavItem>
                )}
            </S.NavMenu>
        </S.HeaderContainer>
    );
};

export default Header;