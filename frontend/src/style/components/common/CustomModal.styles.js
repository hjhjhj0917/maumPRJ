import styled from 'styled-components';

export const ModalOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.4);
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
`;

export const ModalBox = styled.div`
    background-color: #ffffff;
    width: 90%;
    max-width: 410px;
    padding: 10px 30px 25px 30px;
    border-radius: 20px;
    border: 2px solid #ffffff;
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    transition: transform 0.2s, box-shadow 0.2s;
`;

export const CloseButton = styled.span`
    position: absolute;
    top: 15px;
    right: 20px;
    font-size: 20px;
    color: #ccc;
    cursor: pointer;
    z-index: 2;
    transition: color 0.2s;

    &:hover {
        color: #888;
    }
`;

export const ModalContent = styled.div`
    position: relative;
    z-index: 1;

    h2 {
        font-size: 17px;
        font-weight: 700;
        color: #444;
        margin-top: 20px;
        margin-bottom: 80px;
    }

    p {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
        margin-top: -51px;
        margin-bottom: 12px;
    }
`;

export const ModalActions = styled.div`
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: flex-end;
    gap: 15px;
`;

export const CancelButton = styled.button`
    background-color: #f1f1f1;
    color: #666;
    border: none;
    padding: 9px 26px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    border-radius: 18px;
    transition: transform 0.1s, box-shadow 0.1s;
    text-transform: uppercase;

    &:hover {
        background-color: #e9e9e9;
    }

    &:active {
        transform: translateY(2px);
    }
`;

export const OkButton = styled.button`
    background-color: #333;
    color: #fff;
    border: 1px solid #333;
    padding: 9px 26px;
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
    border-radius: 18px;
    transition: transform 0.1s, box-shadow 0.1s;
    text-transform: uppercase;

    &:hover {
        background-color: #000;
    }

    &:active {
        transform: translateY(2px);
    }
`;