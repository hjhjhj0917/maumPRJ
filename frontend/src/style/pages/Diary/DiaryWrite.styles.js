import styled from 'styled-components';

export const WritePageContainer = styled.div`
    padding: 90px 50px 0 50px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    gap: 40px;
    box-sizing: border-box;

    @media (max-width: 768px) {
        padding: 80px 20px 0 20px;
    }
`;

export const HeaderSection = styled.div`
    display: flex;
    flex-direction: column;
    gap: 10px;

    p {
        font-size: 18px;
        color: #8e918f;
        margin-top: 5px;
    }
`;

export const TitleDateRow = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 20px;

    @media (max-width: 560px) {
        flex-direction: column;
        align-items: flex-start;
    }
`;

export const TitleInput = styled.input`
    flex: 1;
    background: transparent;
    border: none;
    outline: none;
    color: #333;
    font-size: 40px;
    font-weight: 400;
    width: 100%;

    &::placeholder {
        color: #e3e3e3;
    }
`;

export const DateWrapper = styled.div`
    position: relative;
`;

export const DateSelector = styled.div`
    display: flex;
    align-items: center;
    gap: 10px;
    color: #888;
    font-size: 16px;
    font-weight: 400;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 8px;
    transition: background-color 0.2s;

    &:hover {
        background-color: rgba(255, 255, 255, 0.05);
    }

    i {
        font-size: 18px;
    }
`;

export const PickerOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 90;
`;

export const PickerContainer = styled.div`
    position: absolute;
    top: calc(100% + 10px);
    right: 0;
    width: 300px;
    z-index: 100;

    @media (max-width: 560px) {
        left: 0;
        right: auto;
    }
`;

export const EditorWrapper = styled.div`
    background-color: #333;
    border-radius: 28px 28px 0 0;
    padding: 40px;
    flex: 1;
    display: flex;
    flex-direction: column;
    box-shadow: 0 -4px 20px rgba(0,0,0,0.2);

    textarea {
        flex: 1;
        background: transparent;
        border: none;
        outline: none;
        color: #e3e3e3;
        font-size: 17px;
        line-height: 1.6;
        resize: none;

        &::placeholder {
            color: #8e918f;
        }
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

    &:hover {
        background-color: #f7c244;
    }
`;