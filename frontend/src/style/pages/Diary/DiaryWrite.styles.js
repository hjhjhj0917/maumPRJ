import styled from 'styled-components';

export const WritePageContainer = styled.div`
    padding: 50px;
    max-width: 900px;
    margin: 0 auto;
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 40px;
`;

export const HeaderSection = styled.div`
    h1 { font-size: 40px; color: #e3e3e3; font-weight: 400; }
    p { font-size: 18px; color: #8e918f; margin-top: 10px; }
`;

export const EditorWrapper = styled.div`
    background-color: #1e1f20;
    border-radius: 28px;
    padding: 30px;
    min-height: 500px;
    display: flex;
    flex-direction: column;
    box-shadow: 0 4px 20px rgba(0,0,0,0.2);

    textarea {
        flex: 1;
        background: transparent;
        border: none;
        outline: none;
        color: #e3e3e3;
        font-size: 20px;
        line-height: 1.6;
        resize: none;
        &::placeholder { color: #8e918f; }
    }
`;

export const FooterActions = styled.div`
    display: flex;
    justify-content: flex-end;
    padding-top: 20px;
`;

export const SubmitButton = styled.button`
    background-color: #FFD166;
    color: #131314;
    border: none;
    padding: 14px 28px;
    border-radius: 24px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    &:hover { background-color: #f7c244; }
`;