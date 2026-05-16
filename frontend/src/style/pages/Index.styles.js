import styled, { keyframes, css } from 'styled-components';

const floatAnimation = keyframes`
    0% { transform: translateY(0px) rotate(0deg); }
    50% { transform: translateY(-15px) rotate(2deg); }
    100% { transform: translateY(0px) rotate(0deg); }
`;

export const PageWrapper = styled.div`
    width: 100%;
    min-height: 100vh;
    background-color: #ffffff;
    color: #000000;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    overflow-x: hidden;
`;

export const Header = styled.header`
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    padding: 20px 40px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    z-index: 1000;
    transition: all 0.3s ease;
    background: ${props => props.$scrolled ? 'rgba(255, 255, 255, 0.7)' : 'transparent'};
    backdrop-filter: ${props => props.$scrolled ? 'blur(10px)' : 'none'};
    border-bottom: ${props => props.$scrolled ? '1px solid #eaeaea' : 'none'};

    @media (max-width: 768px) {
        padding: 20px;
    }
`;

export const Logo = styled.div`
    display: flex;
    align-items: center;
    font-size: 22px;
    font-weight: 500;

    img {
        height: 40px;
        width: auto;
        margin-right: 8px;
    }
`;

export const NavLinks = styled.nav`
    display: flex;
    gap: 30px;

    a {
        color: #666666;
        text-decoration: none;
        font-size: 15px;
        transition: color 0.2s;

        &:hover {
            color: #000000;
        }
    }

    @media (max-width: 768px) {
        display: none;
    }
`;

export const AuthButtons = styled.div`
    display: flex;
    gap: 15px;
`;

export const Button = styled.button`
    background: ${props => props.$primary ? '#000000' : 'transparent'};
    color: ${props => props.$primary ? '#ffffff' : '#000000'};
    border: ${props => props.$primary ? 'none' : '1px solid #eaeaea'};
    padding: 10px 20px;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: ${props => props.$primary ? '#333333' : 'rgba(0,0,0,0.05)'};
    }
`;

export const HeroSection = styled.section`
    padding: 180px 20px 100px;
    text-align: center;
    position: relative;
    max-width: 1400px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    min-height: 90vh;
`;

export const InteractiveCanvas = styled.div`
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 50;
`;

export const DraggableItem = styled.div`
    pointer-events: auto;
    touch-action: none;
    user-select: none;
    will-change: transform;
    left: 50%;
    top: 50%;
`;

export const FloatingElement = styled.div`
    animation: ${props => !props.$isDragging && css`${floatAnimation} ${props.$duration || '6s'} ease-in-out infinite`};
    animation-delay: ${props => props.$delay || '0s'};
`;

export const HeroPill = styled.div`
    padding: 10px 22px;
    border-radius: 30px;
    font-size: 15px;
    font-weight: 700;
    color: ${props => props.$isDark ? '#ffffff' : '#000000'};
    background: ${props => props.$color || '#fff'};
    white-space: nowrap;
    box-shadow: 0 10px 25px rgba(0,0,0,0.15);
    border: 2px solid rgba(255, 255, 255, 0.2);
    cursor: grab;

    &:active {
        cursor: grabbing;
    }
`;

export const HeroContent = styled.div`
    position: relative;
    z-index: 10;
    max-width: 900px;
`;

export const Title = styled.h1`
    font-size: clamp(40px, 8vw, 85px);
    font-weight: 600;
    line-height: 1.1;
    letter-spacing: -2px;
    margin-bottom: 24px;
`;

export const Subtitle = styled.p`
    font-size: clamp(16px, 2vw, 20px);
    color: #666666;
    line-height: 1.6;
    max-width: 650px;
    margin: 0 auto 40px;
`;

export const LogoTicker = styled.div`
    display: flex;
    gap: 40px;
    margin-top: 80px;
    opacity: 0.4;
    justify-content: center;

    i {
        font-size: 28px;
    }
`;

export const Section = styled.section`
    padding: 120px 20px;
    max-width: 1200px;
    margin: 0 auto;
    text-align: ${props => props.$center ? 'center' : 'left'};
`;

export const SectionHeader = styled.div`
    margin-bottom: 80px;
    text-align: center;
`;

export const SectionTitle = styled.h2`
    font-size: clamp(32px, 5vw, 48px);
    font-weight: 600;
    letter-spacing: -1px;
    margin-bottom: 20px;
`;

export const Grid = styled.div`
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
    gap: 30px;
`;

export const Card = styled.div`
    background: #ffffff;
    border: 1px solid #f0f0f0;
    border-radius: 24px;
    padding: 40px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 6px rgba(0,0,0,0.02);

    &:hover {
        transform: translateY(-5px);
        box-shadow: 0 20px 40px rgba(0,0,0,0.05);
        border-color: #e0e0e0;
    }

    h3 {
        font-size: 22px;
        margin-bottom: 16px;
    }

    p {
        color: #666666;
        font-size: 16px;
        line-height: 1.6;
    }
`;

export const StatsGrid = styled.div`
    display: flex;
    justify-content: space-around;
    flex-wrap: wrap;
    gap: 40px;
    padding: 80px 0;
`;

export const StatBox = styled.div`
    text-align: center;

    .number {
        font-size: clamp(48px, 7vw, 72px);
        font-weight: 700;
        margin-bottom: 12px;
        background: linear-gradient(135deg, #000, #444);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
    }

    .label {
        color: #888888;
        font-size: 18px;
        font-weight: 500;
    }
`;

export const EmailForm = styled.div`
    display: flex;
    gap: 12px;
    max-width: 450px;
    margin: 50px auto 0;

    input {
        flex: 1;
        padding: 18px 24px;
        border-radius: 14px;
        border: 1px solid #eeeeee;
        background: #fdfdfd;
        font-size: 16px;
        outline: none;

        &:focus {
            border-color: #000000;
        }
    }

    button {
        padding: 18px 30px;
        border-radius: 14px;
        border: none;
        background: #000000;
        color: #ffffff;
        font-weight: 600;
        cursor: pointer;

        &:hover {
            background: #222;
        }
    }
`;

export const CommunityGrid = styled.div`
    width: 100%;
    height: 450px;
    background: #fcfcfc;
    border-radius: 32px;
    margin: 50px 0;
    border: 1px solid #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #bbbbbb;
    font-size: 24px;
    font-weight: 500;
`;

export const BentoGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 30px;

    @media (max-width: 768px) {
        grid-template-columns: 1fr;
    }

    .large {
        grid-column: span 2;
        @media (max-width: 768px) {
            grid-column: span 1;
        }
    }
`;

export const Footer = styled.footer`
    border-top: 1px solid #f0f0f0;
    padding: 100px 20px 60px;
    margin-top: 120px;
    background: #fafafa;
`;

export const FooterGrid = styled.div`
    max-width: 1200px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 60px;

    @media (max-width: 768px) {
        grid-template-columns: repeat(2, 1fr);
    }

    h4 {
        margin-bottom: 25px;
        font-size: 17px;
        font-weight: 600;
    }

    ul {
        list-style: none;
        padding: 0;

        li {
            margin-bottom: 15px;

            a {
                color: #777777;
                text-decoration: none;
                font-size: 15px;

                &:hover {
                    color: #000000;
                }
            }
        }
    }
`;