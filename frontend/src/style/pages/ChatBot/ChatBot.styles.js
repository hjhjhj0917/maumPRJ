import styled from 'styled-components';

export const ChatContainer = styled.div`
    display: flex;
    flex-direction: column;
    height: 100vh;
    max-width: 800px;
    margin: 0 auto;
    background-color: #ffffff;
`;

export const MessageList = styled.div`
    flex-grow: 1;
    overflow-y: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 24px;
`;

export const MessageWrapper = styled.div`
    display: flex;
    flex-direction: column;
    align-items: ${({ $isUser }) => ($isUser ? 'flex-end' : 'flex-start')};
`;

export const ProfileName = styled.span`
    font-size: 13px;
    color: #666;
    margin-bottom: 6px;
    margin-left: ${({ $isUser }) => ($isUser ? '0' : '4px')};
    margin-right: ${({ $isUser }) => ($isUser ? '4px' : '0')};
`;

export const Bubble = styled.div`
    max-width: 80%;
    padding: 14px 18px;
    border-radius: 20px;
    font-size: 15px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
    background-color: ${({ $isUser }) => ($isUser ? '#f0f0f0' : '#ffffff')};
    color: #333333;

    ${({ $isUser }) => !$isUser && `
        border: 1px solid #eaeaea;
        box-shadow: 0 2px 5px rgba(0,0,0,0.02);
    `}

    p { margin: 0 0 8px 0; }
    p:last-child { margin: 0; }
    strong { font-weight: 600; color: #1a73e8; }
    ul, ol { margin-top: 4px; padding-left: 20px; }
    li { margin-bottom: 4px; }
`;

export const InputContainer = styled.div`
    padding: 20px;
    background-color: #ffffff;
    border-top: 1px solid #eaeaea;
`;

export const InputWrapper = styled.div`
    display: flex;
    align-items: flex-end;
    background-color: #f4f4f4;
    border-radius: 24px;
    padding: 8px 16px;
    border: 1px solid #e0e0e0;

    &:focus-within {
        border-color: #999;
        background-color: #ffffff;
    }
`;

export const StyledTextarea = styled.textarea`
    flex-grow: 1;
    border: none;
    background: transparent;
    resize: none;
    max-height: 150px;
    min-height: 24px;
    padding: 8px 0;
    font-size: 15px;
    line-height: 1.5;
    outline: none;

    &::placeholder {
        color: #999;
    }
`;

export const SendButton = styled.button`
    background-color: ${({ disabled }) => (disabled ? '#cccccc' : '#333333')};
    color: white;
    border: none;
    border-radius: 50%;
    width: 36px;
    height: 36px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: ${({ disabled }) => (disabled ? 'not-allowed' : 'pointer')};
    margin-left: 12px;
    margin-bottom: 4px;
    transition: background-color 0.2s;

    &:hover {
        background-color: ${({ disabled }) => (disabled ? '#cccccc' : '#000000')};
    }
`;

export const ThinkingIndicator = styled.div`
    font-size: 13px;
    color: #888888;
    margin-bottom: 8px;
    padding-bottom: 8px;
    border-bottom: 1px dashed #e0e0e0;
    display: flex;
    align-items: center;
    gap: 6px;
    animation: pulse 1.5s infinite;
    
    @keyframes pulse {
        0% { opacity: 0.5; }
        50% { opacity: 1; }
        100% { opacity: 0.5; }
    }
`;