import styled from 'styled-components';
import { Link } from 'react-router-dom';

export const LoginPageWrapper = styled.div`
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f0f2f5;
    overflow: hidden;

    @media (max-width: 768px) {
        height: auto;
        overflow-y: auto;
    }
`;

export const Container = styled.div`
    display: flex;
    flex: 1;
    width: 100%;

    @media (max-width: 768px) {
        flex-direction: column;
    }
`;

export const FindLinks = styled.div`
    text-align: center;
    font-size: 0.9rem;
    color: #888;
    max-height: 0;
    opacity: 0;
    overflow: hidden;
    margin-top: 0;
    transition: all 0.5s ease-in-out;
`;

export const SignupBox = styled.div`
    font-size: 0.9rem;
    color: #888;
    line-height: 1.5;
    text-align: left !important;
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 0 40px;
    box-sizing: border-box;
    white-space: normal;
    opacity: 0;
    transition: opacity 0.5s ease-in-out;
`;

export const LoginSection = styled.div`
    flex: 0.8;
    display: flex;
    justify-content: center;    
    align-items: center;
    background-color: #ffffff;
    transition: flex 0.5s ease-in-out;
    position: relative;
    z-index: 3;

    &:hover {
        flex: 1.5;
    }

    &:hover ${FindLinks} {
        max-height: 50px;
        opacity: 1;
        margin-top: 25px;
    }

    &:hover ${SignupBox} {
        opacity: 1;
    }

    @media (max-width: 768px) {
        flex: auto;
        padding: 50px 20px;
        background-color: #fff;
        border-top-left-radius: 30px;
        border-top-right-radius: 30px;
        margin-top: -30px;
        box-shadow: 0 -10px 20px rgba(0, 0, 0, 0.05);
        transition: none;

        &:hover {
            flex: auto;
        }

        ${FindLinks} {
            max-height: none;
            opacity: 1;
            margin-top: 25px;
        }

        ${SignupBox} {
            position: static;
            opacity: 1;
            text-align: center;
            margin-top: 20px;
        }
    }
`;

export const LoginCard = styled.div`
    width: 100%;
    max-width: 400px;
    padding: 40px;
    position: relative;
    padding-bottom: 115px;

    @media (max-width: 768px) {
        padding-bottom: 40px;
    }
`;

export const LoginLogoTitle = styled.img`
    display: block;
    margin: 0 auto 50px;
    max-width: 170px;
    height: auto;
`;

export const InputGroup = styled.div`
    margin-bottom: 30px;
    text-align: left;
`;

export const LabelRow = styled.div`
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 5px;

    @media (max-width: 768px) {
        flex-direction: row;
        align-items: center;
    }

    label {
        font-size: 14px;
        font-weight: 600;
        color: #666;
        min-width: 70px;
    }
`;

export const FieldMessage = styled.span`
    font-size: 12px;
    font-weight: 500;
    white-space: nowrap;
    opacity: ${props => (props.$show ? '1 !important' : '0')};
    transition: opacity 0.25s ease;
    pointer-events: none;
    color: ${props => (props.$type === 'error' ? '#ef4444' : props.$type === 'success' ? '#22c55e' : 'inherit')};

    @media (max-width: 768px) {
        width: auto;
        text-align: left;
        white-space: nowrap;
    }
`;

export const PasswordWrapper = styled.div`
    position: relative;
    width: 100%;

    input {
        padding-right: 40px !important;
        box-sizing: border-box;
    }
`;

export const ToggleIcon = styled.i`
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;
    color: ${props => (props.$active ? '#FFD166' : '#ccc')};
    transition: color 0.3s;
    font-size: 1.1rem;
    z-index: 10;

    &:hover {
        color: #FFD166;
    }
`;

export const Input = styled.input`
    width: 100%;
    padding: 10px 0;
    border: none;
    border-bottom: 2px solid #eee;
    outline: none;
    font-size: 15px;
    background-color: transparent;
    transition: border-color 0.3s;

    &::placeholder {
        color: #d0d0d0;
        opacity: 1;
    }

    &:focus {
        border-bottom-color: #FFD166;
    }
`;

export const BtnLogin = styled.button`
    width: 100%;
    padding: 15px;
    color: #333;
    border: none;
    border-radius: 30px;
    font-size: 1rem;
    font-weight: bold;
    cursor: pointer;
    margin-top: 10px;
    transition: background-color 0.3s, transform 0.1s;
    box-shadow: 0 4px 15px rgba(51, 51, 51, 0.4);

    &:hover {
        transform: translateY(-2px);
    }
`;

export const FindRow = styled.div`
    a {
        color: #888;
        text-decoration: none;
        transition: color 0.2s;
        font-size: 0.85rem;

        &:hover {
            color: #FFD166;
        }
    }
`;

export const Separator = styled.span`
    margin: 0 8px;
    color: #ddd;
`;

export const LinkSignup = styled(Link)`
    color: #FFD166;
    text-decoration: none;
    font-weight: bold;
    margin-left: 5px;
    transition: color 0.2s;
    display: inline-block;

    &:hover {
        color: #E0B34A;
        text-decoration: underline;
    }
`;