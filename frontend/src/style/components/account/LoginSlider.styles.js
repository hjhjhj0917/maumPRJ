import styled, { keyframes } from 'styled-components';

const fadeText = keyframes`
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
`;

export const SliderSection = styled.div`
    flex: 1.2;
    background-color: #FFD166;
    color: white;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 50px;
    overflow: hidden;
    transition: flex 0.5s ease-in-out;

    @media (max-width: 768px) {
        flex: auto;
        transition: none;
        padding: 40px 30px;
        min-height: 300px;
    }
`;

export const BgSlider = styled.div`
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
`;

export const BgSlide = styled.div`
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    opacity: ${props => (props.$active ? 1 : 0)};
    transition: opacity 1.5s ease-in-out;
    background-image: url(${props => props.$bgImg});
`;

export const SliderOverlay = styled.div`
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.4);
    z-index: 1;
`;

export const SliderWrapper = styled.div`
    position: relative;
    z-index: 2;
`;

export const Slide = styled.div`
    display: ${props => (props.$active ? 'block' : 'none')};
    animation: ${fadeText} 1s ease-out;

    h2 {
        font-size: 3.5rem;
        margin-bottom: 20px;
        font-weight: 700;
        white-space: nowrap;
        text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);

        @media (max-width: 768px) {
            font-size: 2.5rem;
            white-space: normal;
        }
    }

    p {
        font-size: 1.2rem;
        line-height: 1.6;
        white-space: nowrap;
        text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);

        @media (max-width: 768px) {
            white-space: normal;
        }
    }
`;

export const DotsContainer = styled.div`
    position: absolute;
    bottom: 40px;
    left: 50px;
    display: flex;
    gap: 12px;
    z-index: 2;
`;

export const Dot = styled.span`
    cursor: pointer;
    height: 12px;
    width: 12px;
    background-color: ${props => (props.$active ? 'white' : 'rgba(255, 255, 255, 0.5)')};
    border-radius: 50%;
    transition: all 0.3s;
    transform: ${props => (props.$active ? 'scale(1.2)' : 'none')};

    &:hover {
        background-color: white;
        transform: scale(1.2);
    }
`;