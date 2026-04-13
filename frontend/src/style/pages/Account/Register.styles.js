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
            cursor: default;
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

        border-top: 1px solid #333;
        border-bottom: 1px solid #333;
        pointer-events: none;
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
            font-weight: 500;
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
        background-color: #fff;
        color: #333;
        border: none;
        padding: 12px 0;
        font-size: 16px;
        font-weight: 500;
        cursor: pointer;
        border-radius: 16px;
        transition: transform 0.1s, box-shadow 0.1s, background-color 0.1s;

        &:hover {
            color: #000;
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
    cursor: pointer;

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

        i {
            color: ${props => (props.$active || props.$completed ? '#333' : '#ddd')};
        }
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

export const StepTitle = styled.h2`
    font-size: 32px;
    font-weight: 800;
    color: #000;
    margin-bottom: 12px;
    text-align: left;
`;

export const StepSubTitle = styled.p`
    font-size: 16px;
    color: #666;
    line-height: 1.5;
    margin-bottom: -40px;
    text-align: left;
    min-height: 48px;

    span {
        color: #333;
        font-weight: 600;
    }
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

export const VerificationContainer = styled.div`
    margin-top: 20px;
    display: flex;
    flex-direction: column;
    
    label {
        font-size: 14px;
        font-weight: 600;
        color: #666;
        min-width: 70px;
    }
`;

export const VerificationWrapper = styled.div`
    display: flex;
    justify-content: space-between;
    gap: 10px;
    margin-bottom: 10px;

    input {
        width: 60px;
        height: 70px;
        background-color: #f8f9fa;
        border: 2px solid #eee;
        border-radius: 12px;
        font-size: 24px;
        font-weight: 700;
        text-align: center;
        transition: all 0.3s;

        &:focus {
            border-color: #FFD166;
            background-color: #fff;
            box-shadow: 0 0 0 4px rgba(255, 209, 102, 0.1);
            outline: none;
        }

        &::placeholder {
            color: #eee;
        }

        &:disabled {
            background-color: #eee;
            color: #999;
            cursor: default;
        }
    }

    @media (max-width: 480px) {
        gap: 5px;
        input {
            width: 45px;
            height: 55px;
            font-size: 20px;
        }
    }
`;

export const ResendText = styled.div`
    text-align: left;
    font-size: 14px;
    color: #888;
    margin-top: 17px;
    margin-bottom: 15px;

    button {
        background: none;
        border: none;
        color: #FFD166;
        text-decoration: underline;
        cursor: pointer;
        font-weight: 500;
        margin-left: 6px;
        padding: 0;

        &:hover {
            color: #E0B34A;
        }
    }
`;

export const BtnVerify = styled.button`
    width: 100%;
    padding: 16px;
    background-color: #333;
    color: #fff;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    margin-top: 10px;
    margin-bottom: 20px;
    transition: background-color 0.3s;

    &:hover {
        background-color: #000;
    }

    &:disabled {
        background-color: #eee;
        color: #999;
        cursor: default;
    }
`;

export const ActionButtons = styled.div`
    width: 100%;
    margin-top: 30px;
    animation: ${fadeInBtn} 0.3s ease-out;
`;

export const BtnSubmit = styled.button`
    width: 100%;
    padding: 16px;
    background-color: #333;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    color: #fff;
    margin-bottom: 40px;
    transition: background-color 0.3s;

    &:hover { background-color: #000; }
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