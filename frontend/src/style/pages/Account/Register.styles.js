import styled, { keyframes } from 'styled-components';
import { Link } from 'react-router-dom';

const fadeInBtn = keyframes`
    from { opacity: 0; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
`;

export const RegisterWrapper = styled.div`
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #ffffff;

    .input-group {
        margin-bottom: 30px;
        text-align: left;
    }

    .label-row {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 10px;

        @media (max-width: 560px) {
            flex-direction: column;
            align-items: flex-start;
        }
    }

    .input-group label {
        font-size: 14px;
        font-weight: 600;
        color: #666;
        min-width: 70px;
    }

    .field-message {
        flex: 1;
        text-align: left;
        font-size: 12px;
        font-weight: 500;
        white-space: nowrap;
        opacity: 0;
        transition: opacity 0.25s ease;

        &.show { opacity: 1; }
        &.error { color: #ef4444; }
        &.success { color: #22c55e; }

        @media (max-width: 560px) {
            width: 100%;
            text-align: left;
            white-space: normal;
        }
    }

    .flex-row {
        display: flex;
        gap: 10px;

        @media (max-width: 560px) {
            flex-direction: column;
        }
    }

    .input-group input {
        width: 100%;
        padding: 10px 0;
        border: none;
        border-bottom: 2px solid #eee;
        outline: none;
        font-size: 15px;
        color: #333;
        transition: border-color 0.3s;
        background-color: transparent;
        flex: 1;
        box-sizing: border-box;

        &:focus { border-bottom-color: #FFD166; }
        &::placeholder { color: #ccc; font-size: 14px; }
        &:disabled, &[readOnly] { background-color: transparent; }
    }

    .btn-check {
        padding: 8px 20px;
        background-color: #FFD166;
        border: none;
        border-radius: 20px;
        font-size: 12px;
        font-weight: bold;
        color: #333;
        cursor: pointer;
        white-space: nowrap;
        height: 35px;
        flex-shrink: 0;

        &:hover { background-color: #E0B34A; }
        &:disabled {
            background-color: #eee !important;
            color: #999 !important;
            cursor: not-allowed;
        }

        @media (max-width: 560px) {
            width: 100%;
        }
    }

    .password-wrapper {
        position: relative;
        width: 100%;

        input {
            padding-right: 40px !important;
            box-sizing: border-box;
        }
    }

    .toggle-password {
        color: #bbb;
        font-size: 18px;
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        right: 15px;
        cursor: pointer;
        transition: color 0.3s ease;
        z-index: 10;

        &:hover, &.active { color: #ffd166; }
    }

    .picker-overlay {
        position: fixed;
        top: 0; left: 0; right: 0; bottom: 0;
        z-index: 90;
    }

    .roller-picker-container {
        position: absolute;
        top: calc(100% + 5px);
        left: 0;
        width: 100%;
        background: #ffffff;
        border-radius: 12px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
        z-index: 100;
        overflow: hidden;
        display: flex;
        flex-direction: column;
    }

    .roller-picker-body {
        display: flex;
        position: relative;
        height: 150px;
        background: #fdfdfd;
    }

    .roller-highlight {
        position: absolute;
        top: 60px;
        left: 4%;
        width: 92%;
        height: 30px;
        background-color: rgba(255, 209, 102, 0.1);
        border-top: 1px solid #FFD166;
        border-bottom: 1px solid #FFD166;
        pointer-events: none;
        border-radius: 4px;
    }

    .roller-col {
        flex: 1;
        height: 100%;
        overflow-y: scroll;
        scroll-snap-type: y mandatory;
        scrollbar-width: none;
        -ms-overflow-style: none;

        &::-webkit-scrollbar { display: none; }
    }

    .roller-pad { height: 60px; }

    .roller-item {
        height: 30px;
        line-height: 30px;
        text-align: center;
        scroll-snap-align: center;
        font-size: 15px;
        color: #ccc;
        cursor: pointer;
        transition: color 0.1s, font-size 0.1s, font-weight 0.1s;

        &.active {
            color: #333;
            font-weight: 700;
        }
    }

    .roller-picker-footer {
        display: flex;
        padding: 15px;
        background: #fff;
        border-radius: 0 0 12px 12px;
    }

    #btnPickerConfirm {
        flex: 1;
        background-color: #FFD166;
        color: #444;
        border: 2px solid #FFD166;
        padding: 12px 0;
        font-size: 16px;
        font-weight: 700;
        cursor: pointer;
        border-radius: 16px;
        box-shadow: 0 6px 0 #DDA02A, 0 8px 15px rgba(0, 0, 0, 0.1);
        transition: transform 0.1s, box-shadow 0.1s, background-color 0.1s;

        &:hover {
            background-color: #FFC04D;
            border-color: #FFC04D;
        }

        &:active {
            transform: translateY(6px);
            box-shadow: 0 0px 0 #DDA02A, 0 2px 5px rgba(0, 0, 0, 0.1);
        }
    }
`;

export const Container = styled.div`
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    padding: 40px 20px;
    box-sizing: border-box;
`;

export const RegisterCard = styled.div`
    width: 100%;
    max-width: 475px;
    padding-top: 40px;

    @media (max-width: 560px) {
        max-width: 100%;
    }
`;

export const StepperWrapper = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 50px;
    padding: 0 20px;
    position: relative;
`;

export const StepperItem = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    z-index: 2;
    position: relative;

    .step-circle {
        background-color: ${props => (props.$active || props.$completed ? '#FFD166' : '#fff')};
        border-color: ${props => (props.$active || props.$completed ? '#FFD166' : '#ddd')};
        color: ${props => (props.$active || props.$completed ? '#fff' : '#ddd')};
        width: 32px;
        height: 32px;
        border-radius: 50%;
        border: 2px solid;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 14px;
        transition: all 0.3s ease;
    }

    .step-label {
        color: ${props => (props.$active || props.$completed ? '#333' : '#999')};
        font-weight: ${props => (props.$active || props.$completed ? 'bold' : 'normal')};
        font-size: 11px;
        position: absolute;
        top: -25px;
        width: 60px;
        text-align: center;
    }
`;

export const StepLine = styled.div`
    flex: 1;
    height: 1px;
    background-color: #eee;
    margin-bottom: 16px;
    margin-left: -5px;
    margin-right: -5px;
`;

export const SlideViewport = styled.div`
    width: 100%;
    overflow: hidden;
    margin-bottom: 20px;
`;

export const SlideTrack = styled.div`
    display: flex;
    width: 100%;
    transition: transform 0.6s cubic-bezier(0.25, 1, 0.5, 1);
    transform: translateX(${props => (props.$step - 1) * -100}%);
`;

export const FormStep = styled.div`
    min-width: 100%;
    box-sizing: border-box;
    transform: ${props => (props.$active ? 'scale(1)' : 'scale(0.9)')};
    opacity: ${props => (props.$active ? '1' : '0.3')};
    transition: all 0.6s ease;
`;

export const AuthInputs = styled.div`
    margin-top: 50px;
    margin-bottom: 50px;
`;

export const ActionButtons = styled.div`
    width: 100%;
    margin-top: 30px;
`;

export const BtnStepGroup = styled.div`
    display: ${props => (props.$active ? 'block' : 'none')};
    animation: ${props => (props.$active ? fadeInBtn : 'none')} 0.3s ease-out;
`;

export const BtnRow = styled.div`
    display: flex;
    justify-content: ${props => (props.$split ? 'space-between' : 'flex-end')};

    @media (max-width: 560px) {
        gap: ${props => (props.$split ? '10px' : '0')};
    }
`;

export const BtnPrev = styled.button`
    padding: 12px 40px;
    background-color: #fff;
    border: 1px solid #ddd;
    border-radius: 30px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    font-weight: 600;

    &:hover { background-color: #f9f9f9; }

    @media (max-width: 560px) {
        flex: 1;
        padding: 12px 20px;
    }
`;

export const BtnNext = styled.button`
    padding: 12px 40px;
    background-color: #fff;
    border: 1px solid #FFD166;
    border-radius: 30px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    font-weight: 600;

    &:hover { background-color: #FFD166; }

    @media (max-width: 560px) {
        flex: 1;
        padding: 12px 20px;
    }
`;

export const BtnSubmit = styled.button`
    padding: 12px 40px;
    background-color: #FFD166;
    border: none;
    border-radius: 30px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    font-weight: 600;
    box-shadow: 0 4px 10px rgba(255, 209, 102, 0.3);

    &:hover { background-color: #E0B34A; }

    @media (max-width: 560px) {
        flex: 1;
        padding: 12px 20px;
    }
`;

export const LoginBox = styled.div`
    margin-top: 40px;
    font-size: 13px;
    color: #999;
    text-align: left;
`;

export const LinkLogin = styled(Link)`
    color: #FFD166;
    text-decoration: none;
    font-weight: 700;
    margin-left: 5px;
`;