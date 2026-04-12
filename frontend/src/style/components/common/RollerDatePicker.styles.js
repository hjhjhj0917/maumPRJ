import styled from 'styled-components';

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
`;

export const PickerBody = styled.div`
    display: flex;
    position: relative;
    height: 150px;
    background: #fdfdfd;
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
    color: ${props => (props.$active ? '#333' : '#ccc')};
    font-weight: ${props => (props.$active ? '500' : 'normal')};
    cursor: pointer;
    transition: color 0.1s, font-size 0.1s, font-weight 0.1s;
`;

export const Footer = styled.div`
    display: flex;
    padding: 15px;
    background: #fff;
    border-radius: 0 0 12px 12px;
`;

export const ConfirmButton = styled.button`
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
`;