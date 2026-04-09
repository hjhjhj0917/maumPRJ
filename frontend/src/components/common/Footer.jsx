import React from 'react';
import {useFooter} from '../../hooks/pages/common/useFooter';
import * as S from '../../style/components/common/Footer.styles';

const Footer = () => {
    const {currentYear, socialLinks} = useFooter();

    return (
        <S.FooterWrapper>
            <S.FooterContainer>
                <S.LeftSection>
                    <S.InfoText>
                        <span>(주)마음MAÜM</span>
                        <span>메일: yjmo0309@gmail.com</span>
                        <span>주소: 서울 강서구 우장산로10길 112 한국폴리텍대학서울강서캠퍼스</span>
                        <span>빅데이터학과</span>
                    </S.InfoText>
                    <S.Copyright>
                        &copy; {currentYear} MAÜM. All Rights Reserved.
                    </S.Copyright>
                </S.LeftSection>

                <S.RightSection>
                    {socialLinks.map((social) => (
                        <S.SocialLink
                            key={social.id}
                            href={social.url}
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            <i className={social.icon}></i>
                        </S.SocialLink>
                    ))}
                </S.RightSection>
            </S.FooterContainer>
        </S.FooterWrapper>
    );
};

export default Footer;