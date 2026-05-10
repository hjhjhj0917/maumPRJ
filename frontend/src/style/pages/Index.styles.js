import styled, { keyframes, css } from 'styled-components';

const floatAnimation = keyframes`
    0% { transform: translateY(0px) rotate(0deg); }
    50% { transform: translateY(-20px) rotate(5deg); }
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
    z-index: 100;
    transition: all 0.3s ease;
    background: ${props => props.$scrolled ? 'rgba(255, 255, 255, 0.9)' : 'transparent'};
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
        object-fit: contain;
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
    position: relative;
    z-index: 10;

    &:hover {
        background: ${props => props.$primary ? '#333333' : 'rgba(0,0,0,0.05)'};
    }
`;

export const HeroSection = styled.section`
    padding: 180px 20px 100px;
    text-align: center;
    position: relative;
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    min-height: 80vh;
`;

export const InteractiveCanvas = styled.div`
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
`;

export const DraggableItem = styled.div`
    pointer-events: auto;
    touch-action: none;
    user-select: none;
    will-change: transform;
`;

export const FloatingElement = styled.div`
    animation: ${props => !props.$isDragging && css`${floatAnimation} ${props.$duration || '6s'} ease-in-out infinite`};
    animation-delay: ${props => props.$delay || '0s'};
    display: flex;
    align-items: center;
    justify-content: center;
`;

export const HeroPill = styled.div`
    padding: 8px 16px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 600;
    color: #000;
    background: ${props => props.$color || '#fff'};
    white-space: nowrap;
    box-shadow: 0 10px 20px rgba(0,0,0,0.1);
`;

export const HeroContent = styled.div`
    position: relative;
    z-index: 10;
    pointer-events: none;
    display: flex;
    flex-direction: column;
    align-items: center;

    > * {
        pointer-events: auto;
    }
`;

export const Title = styled.h1`
    font-size: clamp(40px, 8vw, 85px);
    font-weight: 600;
    line-height: 1.1;
    letter-spacing: -2px;
    margin-bottom: 24px;
    max-width: 800px;
`;

export const Subtitle = styled.p`
    font-size: clamp(16px, 2vw, 20px);
    color: #666666;
    line-height: 1.5;
    max-width: 600px;
    margin-bottom: 40px;
`;

export const LogoTicker = styled.div`
    display: flex;
    gap: 40px;
    margin-top: 80px;
    opacity: 0.5;
    flex-wrap: wrap;
    justify-content: center;

    i {
        font-size: 24px;
    }
`;

export const Section = styled.section`
    padding: 100px 20px;
    max-width: 1200px;
    margin: 0 auto;
    text-align: ${props => props.$center ? 'center' : 'left'};
`;

export const SectionHeader = styled.div`
    margin-bottom: 60px;
    text-align: center;
`;

export const SectionTitle = styled.h2`
    font-size: clamp(32px, 5vw, 48px);
    font-weight: 600;
    letter-spacing: -1px;
    margin-bottom: 16px;
`;

export const Grid = styled.div`
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 24px;
`;

export const Card = styled.div`
    background: #fafafa;
    border: 1px solid #eaeaea;
    border-radius: 20px;
    padding: 30px;
    position: relative;
    overflow: hidden;
    transition: transform 0.2s, border-color 0.2s;

    &:hover {
        border-color: #cccccc;
    }

    h3 {
        font-size: 20px;
        margin-bottom: 12px;
    }

    p {
        color: #666666;
        font-size: 15px;
        line-height: 1.5;
    }
`;

export const StatsGrid = styled.div`
    display: flex;
    justify-content: space-around;
    flex-wrap: wrap;
    gap: 40px;
    padding: 60px 0;
`;

export const StatBox = styled.div`
    text-align: center;

    .number {
        font-size: clamp(40px, 6vw, 64px);
        font-weight: 600;
        margin-bottom: 8px;
    }

    .label {
        color: #666666;
        font-size: 18px;
    }
`;

export const EmailForm = styled.div`
    display: flex;
    gap: 10px;
    max-width: 400px;
    margin: 40px auto 0;

    input {
        flex: 1;
        padding: 16px 20px;
        border-radius: 12px;
        border: 1px solid #dddddd;
        background: #fafafa;
        color: #000000;
        font-size: 16px;
        outline: none;
        transition: border-color 0.2s;

        &:focus {
            border-color: #999999;
        }
    }

    button {
        padding: 16px 24px;
        border-radius: 12px;
        border: none;
        background: #000000;
        color: #ffffff;
        font-weight: 600;
        cursor: pointer;
        transition: background 0.2s;

        &:hover {
            background: #333333;
        }
    }
`;

export const CommunityGrid = styled.div`
    width: 100%;
    height: 400px;
    background: #fafafa;
    border-radius: 24px;
    margin: 40px 0;
    border: 1px solid #eaeaea;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999999;
    font-size: 24px;
`;

export const BentoGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-auto-rows: minmax(250px, auto);
    gap: 24px;

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

export const PricingGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    margin-top: 40px;

    @media (max-width: 900px) {
        grid-template-columns: 1fr;
    }
`;

export const PriceCard = styled(Card)`
    display: flex;
    flex-direction: column;

    .tier {
        font-size: 24px;
        margin-bottom: 8px;
    }

    .price {
        font-size: 56px;
        font-weight: 600;
        margin-bottom: 24px;

        span {
            font-size: 16px;
            color: #666666;
        }
    }

    ul {
        list-style: none;
        padding: 0;
        margin: 0 0 40px 0;
        flex: 1;

        li {
            color: #444444;
            margin-bottom: 16px;
            font-size: 15px;
            display: flex;
            align-items: center;
            gap: 10px;

            &:before {
                content: '✓';
                color: #000000;
            }
        }
    }
`;

export const FullWidthButton = styled(Button)`
    width: 100%;
    padding: 16px;
`;

export const Footer = styled.footer`
    border-top: 1px solid #eaeaea;
    padding: 80px 20px 40px;
    margin-top: 100px;
`;

export const FooterGrid = styled.div`
    max-width: 1200px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 40px;

    @media (max-width: 768px) {
        grid-template-columns: repeat(2, 1fr);
    }

    h4 {
        margin-bottom: 20px;
        font-size: 16px;
    }

    ul {
        list-style: none;
        padding: 0;

        li {
            margin-bottom: 12px;

            a {
                color: #666666;
                text-decoration: none;
                font-size: 14px;
                transition: color 0.2s;

                &:hover {
                    color: #000000;
                }
            }
        }
    }
`;