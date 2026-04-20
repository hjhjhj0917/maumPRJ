import styled from 'styled-components';

export const PickerOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.5);
    z-index: 999;
`;

export const PickerContainer = styled.div`
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 320px;
    max-width: 90vw;
    border-radius: 12px;
    z-index: 1000;
    overflow: hidden;
    display: flex;
    flex-direction: column;
`;

export const PickerBody = styled.div`
    display: flex;
    position: relative;
    height: 150px;
`;

export const Highlight = styled.div`
    position: absolute;
    top: 60px;
    left: 4%;
    width: 92%;
    height: 30px;
    border-top: 1px solid #333;
    border-bottom: 1px solid #333;
    pointer-events: none;
`;

export const Column = styled.div`
    flex: 1;
    height: 100%;
    overflow-y: scroll;
    scroll-snap-type: y mandatory;
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
        display: none;
    }
`;

export const Pad = styled.div`
    height: 60px;
`;

export const Item = styled.div`
    height: 30px;
    line-height: 30px;
    text-align: center;
    scroll-snap-align: center;
    font-size: 15px;
    color: ${props => (props.$active ? '#FFD166' : '#ccc')};
    font-weight: ${props => (props.$active ? '500' : 'normal')};
    cursor: pointer;
    transition: color 0.1s, font-size 0.1s, font-weight 0.1s;
`;

export const Footer = styled.div`
    display: flex;
    padding: 15px;
    border-radius: 0 0 12px 12px;
`;

export const ConfirmButton = styled.button`
    flex: 1;
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
`;