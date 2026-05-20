import styled, { keyframes } from 'styled-components';

const gradientGlow = keyframes`
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
`;

export const ChatContainer = styled.div`
    display: flex;
    flex-direction: column;
    height: 100vh;
    max-width: 800px;
    margin: 0 auto;
    background-color: transparent;
    position: relative;
`;

export const EmptyStateContainer = styled.div`
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px;
    width: 100%;
`;

export const CenterInputArea = styled.div`
    width: 100%;
    max-width: 760px;
    display: flex;
    flex-direction: column;
    align-items: center;
`;

export const BottomInputArea = styled.div`
    width: 100%;
    padding: 20px;
    background-color: transparent;
    max-width: 800px;
    margin-bottom: 60px;
`;

export const MessageList = styled.div`
    flex-grow: 1;
    overflow-y: auto;
    padding: 105px 20px 40px;
    display: flex;
    flex-direction: column;
    gap: 32px;
`;

export const MessageWrapper = styled.div`
    display: flex;
    flex-direction: column;
    align-items: ${({ $isUser }) => ($isUser ? 'flex-end' : 'flex-start')};
    width: 100%;
`;

export const Bubble = styled.div`
    max-width: 85%;
    padding: ${({ $isUser }) => ($isUser ? '12px 20px' : '0')};
    border-radius: 24px;
    font-size: 16px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
    background-color: ${({ $isUser }) => ($isUser ? '#ffffff' : 'transparent')};
    color: #0d0d0d;
    box-shadow: ${({ $isUser }) => ($isUser ? '0 2px 8px rgba(0, 0, 0, 0.04)' : 'none')};

    p { margin: 0 0 12px 0; }
    p:last-child { margin: 0; }
    strong { font-weight: 600; }
    ul, ol { margin-top: 4px; padding-left: 20px; }
    li { margin-bottom: 4px; }
`;

export const SuggestionContainer = styled.div`
    display: flex;
    gap: 12px;
    margin-top: 20px;
    justify-content: center;
    flex-wrap: wrap;
    width: 100%;
`;

export const SuggestionButton = styled.button`
    display: flex;
    align-items: center;
    gap: 8px;
    background-color: #ffffff;
    border: 1px solid #e5e5e5;
    border-radius: 20px;
    padding: 10px 16px;
    font-size: 13px;
    color: #444;
    cursor: pointer;
    transition: background-color 0.2s ease;

    &:hover {
        background-color: #f9f9f9;
    }

    &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    i {
        font-size: 14px;
        color: #666;
    }
`;

export const InputWrapper = styled.div`
    display: flex;
    align-items: flex-end;
    width: 100%;
    min-height: 64px;
    padding: 12px 16px 12px 24px;
    border-radius: 36px;
    position: relative;
    box-sizing: border-box;
    z-index: 1;

    background-color: transparent;

    &::before {
        content: '';
        position: absolute;
        inset: -2px;
        border-radius: 38px;
        background: linear-gradient(
                115deg,
                #FFD700, #66CDAA, #4B0082, #00BFFF, #1E3A8A, #556B2F, #FF3B30, #FFA500, #9E9E9E, #FFD700
        );
        background-size: 300% 300%;
        animation: ${gradientGlow} 6s ease infinite;
        filter: blur(10px);
        opacity: 0.5;
        z-index: -2;
        pointer-events: none;
        transition: opacity 0.3s, filter 0.3s;
    }

    &::after {
        content: '';
        position: absolute;
        inset: -2px;
        border-radius: 38px;
        background:
            /* 안쪽은 완벽한 하얀색으로 채움 */
                linear-gradient(#ffffff, #ffffff) padding-box,
                    /* 테두리만 무지개색 애니메이션 적용 */
                linear-gradient(
                        115deg,
                        #FFD700, #66CDAA, #4B0082, #00BFFF, #1E3A8A, #556B2F, #FF3B30, #FFA500, #9E9E9E, #FFD700
                ) border-box;
        border: 2px solid transparent;
        background-size: 100% 100%, 300% 300%;
        animation: ${gradientGlow} 6s ease infinite;
        z-index: -1;
        pointer-events: none;
    }

    &:focus-within::before {
        opacity: 0.8;
        filter: blur(14px);
    }
`;

export const StyledTextarea = styled.textarea`
    flex-grow: 1;
    border: none;
    background: transparent;
    resize: none;
    max-height: 200px;
    min-height: 28px;
    padding: 6px 12px 6px 0;
    margin-bottom: 2px;
    font-size: 16px;
    line-height: 1.5;
    outline: none;
    color: #0d0d0d;
    width: 100%;

    &::placeholder {
        color: #8e8e8e;
    }
`;

export const RightActions = styled.div`
    display: flex;
    align-items: center;
    gap: 8px;
`;

export const SendButton = styled.button`
    background-color: ${({ disabled }) => (disabled ? '#e5e5e5' : '#000000')};
    color: ${({ disabled }) => (disabled ? '#a3a3a3' : '#ffffff')};
    border: none;
    width: 38px;
    height: 38px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: ${({ disabled }) => (disabled ? 'not-allowed' : 'pointer')};
    transition: background-color 0.2s, color 0.2s;
    margin-bottom: 1px;
    flex-shrink: 0;

    &:hover {
        background-color: ${({ disabled }) => (disabled ? '#e5e5e5' : '#333333')};
    }

    i {
        font-size: 16px;
    }
`;

export const ThinkingIndicator = styled.div`
    font-size: 14px;
    color: #666;
    display: flex;
    align-items: center;
    gap: 8px;
    animation: pulse 1.5s infinite;

    @keyframes pulse {
        0% { opacity: 0.5; }
        50% { opacity: 1; }
        100% { opacity: 0.5; }
    }
`;