import React, { useState, useEffect } from 'react';
import * as S from '../../style/components/account/LoginSlider.styles';
import bg1 from '../../assets/images/account/login-background1.jpg';
import bg2 from '../../assets/images/account/login-background2.jpg';
import bg3 from '../../assets/images/account/login-background3.jpg';

const LoginSlider = () => {
    const [slideIndex, setSlideIndex] = useState(0);

    useEffect(() => {
        const timer = setInterval(() => {
            setSlideIndex((prevIndex) => (prevIndex + 1) % 3);
        }, 4000);
        return () => clearInterval(timer);
    }, []);

    return (
        <S.SliderSection>
            <S.BgSlider>
                <S.BgSlide $active={slideIndex === 0} $bgImg={bg1} />
                <S.BgSlide $active={slideIndex === 1} $bgImg={bg2} />
                <S.BgSlide $active={slideIndex === 2} $bgImg={bg3} />
            </S.BgSlider>

            <S.SliderOverlay />

            <S.SliderWrapper>
                <S.Slide $active={slideIndex === 0}>
                    <h2>Memory.</h2>
                    <p>흘러가면 잊혀질 오늘의 순간을 소중하게 기억합니다.<br />빛나는 청춘의 한 페이지를 이곳에 남겨두세요.</p>
                </S.Slide>
                <S.Slide $active={slideIndex === 1}>
                    <h2>Journal.</h2>
                    <p>복잡한 머릿속 생각들을 차분하게 기록합니다.<br />나만의 속도로 써 내려가는 글이 마음의 쉼표가 됩니다.</p>
                </S.Slide>
                <S.Slide $active={slideIndex === 2}>
                    <h2>Mood.</h2>
                    <p>글 속에 담긴 내면의 소리와 감정 상태를 분석합니다.<br />스스로도 몰랐던 나의 진짜 마음을 마주해보세요.</p>
                </S.Slide>
            </S.SliderWrapper>

            <S.DotsContainer>
                <S.Dot $active={slideIndex === 0} onClick={() => setSlideIndex(0)} />
                <S.Dot $active={slideIndex === 1} onClick={() => setSlideIndex(1)} />
                <S.Dot $active={slideIndex === 2} onClick={() => setSlideIndex(2)} />
            </S.DotsContainer>
        </S.SliderSection>
    );
};

export default LoginSlider;