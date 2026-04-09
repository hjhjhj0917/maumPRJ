import styled from 'styled-components';

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
    background-color: ${props => props.$bg || '#ffffff'};

    @media (max-width: 768px) {
        height: auto;
        min-height: 100vh;
        padding: 60px 0;
    }
`;

export const HeroGrid = styled.div`
    display: grid;
    grid-template-columns: 1.2fr 0.8fr;
    width: 100%;
    max-width: 1200px;
    padding: 0 40px;
    align-items: center;
    margin-top: -50px;

    @media (max-width: 768px) {
        grid-template-columns: 1fr;
        text-align: center;
        gap: 40px;
        margin-top: 20px;
    }
`;

export const HeroTextContent = styled.div`
    h1 {
        font-size: 80px;
        font-weight: 500;
        line-height: 1.1;
        color: #333;
        margin-bottom: 30px;
        letter-spacing: -2px;

        &:first-of-type {
            color: #FFD166;
        }
    }
    p {
        font-size: 18px;
        color: #666;
        line-height: 1.6;
        margin-bottom: 20px;
    }
    .how-it-works {
        display: inline-block;
        color: #111;
        text-decoration: underline;
        font-weight: 600;
        font-size: 16px;
    }

    @media (max-width: 768px) {
        h1 { font-size: 40px; }
    }
`;

export const HeroCtaContent = styled.div`
    display: flex;
    justify-content: flex-end;
    @media (max-width: 768px) { justify-content: center; }
`;

export const MetaButton = styled.div`
    background-color: #333;
    color: #fff;
    padding: 14px 28px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 12px;
    font-weight: 400;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
        background-color: #444;
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
    gap: 15px;
    .icon-box {
        width: 70px;
        height: 70px;
        background: #555;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        box-shadow: 0 4px 15px rgba(0,0,0,0.05);
        font-size: 24px;
        color: #FFD166;
    }
    span { font-weight: 500; color: #666; font-size: 14px; }
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
    background-color: #666;
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
    background-color: #fff;
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
    .num { font-size: 4.5rem; font-weight: 700; color: #FFD166; margin-bottom: 10px; }
    .label { font-size: 1.2rem; color: #555; font-weight: 600; }
`;