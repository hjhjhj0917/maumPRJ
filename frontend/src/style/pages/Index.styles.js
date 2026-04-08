import styled, { keyframes } from 'styled-components';

const zoomEffect = keyframes`
    from { transform: scale(1); }
    to { transform: scale(1.1); }
`;

export const ViewportWrapper = styled.div`
    width: 100%;
    height: 100vh;
    overflow: hidden;
    position: relative;
    background-color: #ffffff;

    @media (max-width: 768px) {
        height: auto;
        overflow: auto;
    }
`;

export const MainContent = styled.div`
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    transition: transform 0.8s cubic-bezier(0.645, 0.045, 0.355, 1.000);
    transform: ${props => props.$isMobile ? 'none' : `translateY(-${props.$index * 100}vh)`};

    @media (max-width: 768px) {
        display: block;
        transform: none;
    }
`;

export const Section = styled.section`
    width: 100%;
    height: 100vh;
    flex-shrink: 0;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    background-color: #ffffff;

    @media (max-width: 768px) {
        height: auto;
        min-height: 100vh;
        padding: 60px 0;
    }
`;

export const HeroBackground = styled.div`
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url(${props => props.$src});
    background-size: cover;
    background-position: center;
    z-index: 0;
    animation: ${zoomEffect} 20s infinite alternate ease-in-out;

    &::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        /* 상단은 진하게(어둡게), 하단은 급격히 하얗게 */
        background: linear-gradient(
                to bottom,
                rgba(0, 0, 0, 0.5) 0%,
                rgba(255, 255, 255, 0) 40%,
                rgba(255, 255, 255, 1) 85%
        );
    }
`;

export const HeroNav = styled.nav`
    position: absolute;
    top: 0;
    width: 100%;
    max-width: 1200px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30px 20px;
    z-index: 10;
`;

export const Logo = styled.div`
    font-size: 24px;
    font-weight: 900;
    color: #111;
`;

export const NavLinks = styled.div`
    display: flex;
    gap: 25px;
    a {
        text-decoration: none;
        color: #333;
        font-size: 14px;
        font-weight: 500;
    }
`;

export const HeroGrid = styled.div`
    width: 100%;
    max-width: 1200px;
    padding: 0 40px;
    z-index: 1;
    margin-top: -50px;

    @media (max-width: 768px) {
        text-align: center;
        margin-top: 20px;
    }
`;

export const HeroTextContent = styled.div`
    max-width: 800px;
    h1 {
        font-size: 3.5rem;
        font-weight: 600;
        line-height: 1.5;
        color: #FFD166;
        margin-bottom: 30px;
        letter-spacing: -2px;
    }
    p {
        font-size: 1.1rem;
        font-weight: 300;
        color: #fff;
        line-height: 1.6;
        margin-bottom: 20px;
    }
    .sign-in {
        display: inline-block;
        color: #333;
        text-decoration: none;
        font-weight: 500;
        font-size: 18px;
    }
    
    .sign-in:hover {
        color: #FFD166;
    }

    @media (max-width: 768px) {
        h1 { font-size: 40px; }
    }
`;

export const HeroFeatures = styled.div`
    position: absolute;
    bottom: 80px;
    width: 100%;
    max-width: 1200px;
    display: flex;
    justify-content: space-between;
    padding: 0 40px;
    z-index: 1;

    @media (max-width: 768px) {
        position: relative;
        bottom: 0;
        margin-top: 60px;
        flex-wrap: wrap;
        justify-content: center;
        gap: 40px;
    }
`;

export const FeatureItem = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    .icon-box {
        width: 85px;
        height: 85px;
        background: white;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        box-shadow: 0 10px 30px rgba(0,0,0,0.05);
        font-size: 32px;
        color: #333;
    }
    span { font-weight: 500; color: #555; font-size: 15px; }
`;

export const SplitContainer = styled.div`
    display: flex;
    width: 100%;
    height: 100%;
    @media (max-width: 768px) { flex-direction: column; }
`;

export const SplitPanel = styled.div`
    flex: 1;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    text-decoration: none;
    transition: all 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
    background-color: #333;
    overflow: hidden;
    cursor: pointer;

    &:hover {
        flex: 1.5;
        background-color: #444;
    }

    h4 {
        color: white;
        font-size: 1.5rem;
        z-index: 2;
        letter-spacing: 2px;
    }
`;

export const FooterWrapper = styled.div`
    width: 100%;
    height: 100vh;
    flex-shrink: 0;
    background-color: #ffffff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    @media (max-width: 768px) {
        height: auto;
    }
`;

export const StatsSection = styled.div`
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 80px;
    width: 100%;
    @media (max-width: 768px) { flex-direction: column; padding: 60px 0; }
`;

export const StatItem = styled.div`
    text-align: center;
    .num { font-size: 4.5rem; font-weight: 800; color: #FFD166; margin-bottom: 10px; }
    .label { font-size: 1.2rem; color: #555; font-weight: 600; }
`;