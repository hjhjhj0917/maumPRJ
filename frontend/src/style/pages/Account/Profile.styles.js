import styled, { keyframes } from 'styled-components';

const fadeInUp = keyframes`
    from { opacity: 0; transform: translateY(30px); }
    to { opacity: 1; transform: translateY(0); }
`;

export const ProfileWrapper = styled.div`
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #ffffff;
`;

export const Container = styled.div`
    flex: 1;
    display: flex;
    align-items: center;
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 40px;
    box-sizing: border-box;
    animation: ${fadeInUp} 0.8s ease-out;
`;

export const ProfileForm = styled.form`
    display: flex;
    flex-direction: column;
    width: 100%;
`;

export const ProfileLayout = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    margin-bottom: 50px;
    gap: 40px;

    @media (max-width: 1024px) {
        flex-direction: column;
        gap: 60px;
    }
`;

export const PreviewArea = styled.div`
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
`;

export const PreviewCircle = styled.div`
    width: 480px;
    height: 480px;
    border-radius: 50%;
    border: 10px solid #FFD166;
    overflow: hidden;
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.08);

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    @media (max-width: 1024px) {
        width: 350px;
        height: 350px;
    }

    @media (max-width: 600px) {
        width: 250px;
        height: 250px;
    }
`;

export const SelectionArea = styled.div`
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
`;

export const ZodiacGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 30px 45px;

    @media (max-width: 600px) {
        grid-template-columns: repeat(3, 1fr);
        gap: 20px 25px;
    }
`;

export const ButtonArea = styled.div`
    display: flex;
    justify-content: flex-end;
    width: 100%;
    padding-right: 20px;

    @media (max-width: 1024px) {
        justify-content: center;
        padding-right: 0;
    }
`;

export const BtnSave = styled.button`
    background-color: #333;
    color: #fff;
    border: 2px solid #333;
    padding: 9px 41px;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    border-radius: 9px;
    transition: transform 0.1s, box-shadow 0.1s;
    text-transform: uppercase;

    &:hover {
        background-color: #000;
        border-color: #000;
    }

    &:active {
        transform: translateY(6px);
        box-shadow: 0 0px 0 #DDA02A, 0 2px 5px rgba(0, 0, 0, 0.1);
    }
`;