import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import * as S from '../../style/components/layout/HeaderLayout.styles';

const HeaderLayout = () => {
    return (
        <S.HeaderLayoutWrapper>
            <Header />
            <S.MainContent>
                <Outlet />
            </S.MainContent>
        </S.HeaderLayoutWrapper>
    );
};

export default HeaderLayout;