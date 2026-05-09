import styled, { keyframes } from 'styled-components';

const spin = keyframes`
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
`;

export const LoadingOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(5px);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    z-index: 9999;
`;

export const Spinner = styled.div`
    width: 60px;
    height: 60px;
    border: 5px solid #f3f3f3;
    border-top: 5px solid #FFD166;
    border-radius: 50%;
    animation: ${spin} 1s linear infinite;
    margin-bottom: 24px;
`;

export const LoadingText = styled.div`
    font-size: 20px;
    font-weight: 600;
    color: #333;
    text-align: center;
    line-height: 1.5;

    span {
        display: block;
        font-size: 15px;
        font-weight: 400;
        color: #888;
        margin-top: 10px;
    }
`;

export const DetailPageContainer = styled.div`
    padding: 90px 50px 50px 50px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    gap: 30px;
    box-sizing: border-box;

    @media (max-width: 768px) {
        padding: 80px 20px 30px 20px;
    }
`;

export const HeaderSection = styled.div`
    display: flex;
    flex-direction: column;
    gap: 20px;
`;

export const TopActions = styled.div`
    display: flex;
    justify-content: space-between;
`;

export const BackButton = styled.button`
    background: none;
    border: none;
    color: #8e918f;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0;

    &:hover {
        color: #333;
    }

    &:disabled {
        color: #ccc;
        cursor: not-allowed;
    }
`;

export const TitleDateRow = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 20px;

    @media (max-width: 768px) {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }
`;

export const TitleText = styled.h1`
    margin: 0;
    color: #333;
    font-size: 40px;
    font-weight: 600;
    line-height: 1.3;
    word-break: keep-all;
`;

export const DateText = styled.div`
    color: #8e918f;
    font-size: 18px;
    font-weight: 400;
    padding-bottom: 5px;
`;

export const MainContentWrapper = styled.div`
    display: flex;
    gap: 30px;
    align-items: flex-start;

    @media (max-width: 900px) {
        flex-direction: column;
    }
`;

export const ContentArea = styled.div`
    flex: 1;
    background-color: #fff;
    border-radius: 28px;
    padding: 40px;
    min-height: 400px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.05);
    width: 100%;
    box-sizing: border-box;
`;

export const ContentText = styled.div`
    color: #333;
    font-size: 17px;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-all;
`;

export const SidebarArea = styled.div`
    width: 320px;
    flex-shrink: 0;
    background-color: #fff;
    border-radius: 28px;
    padding: 30px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.05);
    display: flex;
    flex-direction: column;
    gap: 25px;
    box-sizing: border-box;

    @media (max-width: 900px) {
        width: 100%;
    }
`;

export const SidebarTitle = styled.h3`
    margin: 0;
    font-size: 18px;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f0f0;

    i {
        color: #FFD166;
    }
`;

export const AnalysisCard = styled.div`
    display: flex;
    flex-direction: column;
    gap: 10px;
`;

export const AnalysisLabel = styled.span`
    font-size: 14px;
    color: #8e918f;
    font-weight: 500;
`;

export const EmotionRow = styled.div`
    display: flex;
    align-items: center;
    gap: 12px;
`;

export const ColorCircle = styled.div`
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background-color: ${props => props.$color};
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
`;

export const EmotionText = styled.span`
    font-size: 18px;
    font-weight: 600;
    color: #333;
`;

export const SummaryText = styled.p`
    margin: 0;
    font-size: 16px;
    line-height: 1.5;
    color: #555;
    font-style: italic;
    background-color: #fcfcfc;
    padding: 15px;
    border-radius: 12px;
    border-left: 4px solid #FFD166;
`;

export const LevelText = styled.div`
    font-size: 18px;
    font-weight: 600;
    color: #e74c3c;
`;

export const ButtonGroup = styled.div`
    display: flex;
    gap: 10px;
`;

export const ActionButton = styled.button`
    background-color: #fff;
    border: 1px solid #d1d4d2;
    color: #555;
    padding: 6px 16px;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover:not(:disabled) {
        background-color: #f8f9fa;
        color: #333;
    }

    &:disabled {
        background-color: #e0e0e0;
        color: #a0a0a0;
        border-color: #e0e0e0;
        cursor: not-allowed;
    }
`;

export const SaveButton = styled(ActionButton)`
    background-color: #FFD166;
    border-color: #FFD166;
    color: #333;
    font-weight: 600;

    &:hover:not(:disabled) {
        background-color: #ffc233;
    }
`;

export const TitleInput = styled.input`
    width: 100%;
    font-size: 40px;
    font-weight: 600;
    color: #333;
    border: none;
    border-bottom: 2px solid #FFD166;
    padding-bottom: 5px;
    outline: none;
    background: transparent;

    &::placeholder {
        color: #ccc;
    }
`;

export const ContentTextarea = styled.textarea`
    width: 100%;
    height: 100%;
    min-height: 400px;
    font-size: 17px;
    line-height: 1.8;
    color: #333;
    border: 1px solid #eee;
    border-radius: 12px;
    padding: 15px;
    outline: none;
    resize: vertical;
    font-family: inherit;
    box-sizing: border-box;

    &:focus {
        border-color: #FFD166;
    }
`;

export const DeleteButton = styled(ActionButton)`
    border-color: #ff6b6b;
    color: #ff6b6b;

    &:hover:not(:disabled) {
        background-color: #ff6b6b;
        color: #fff;
    }
`;