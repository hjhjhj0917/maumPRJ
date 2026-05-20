import styled from 'styled-components';

export const Container = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px;
    background-color: #f9f9f9;
    min-height: 100vh;
`;

export const Title = styled.h2`
    margin-bottom: 20px;
    color: #333;
`;

export const ProfileCard = styled.div`
    background: white;
    padding: 30px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    width: 100%;
    max-width: 500px;
`;

export const Section = styled.div`
    margin-bottom: 15px;
    width: 100%;
`;

export const Row = styled.div`
    display: flex;
    gap: 10px;
`;

export const Label = styled.label`
    display: block;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 5px;
    color: #555;
`;

export const Input = styled.input`
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    box-sizing: border-box;
    margin-bottom: 5px;
    &:disabled {
        background-color: #eee;
        cursor: not-allowed;
    }
`;

export const ButtonContainer = styled.div`
    margin-top: 25px;
    display: flex;
    flex-direction: column;
    gap: 10px;
`;

export const SaveButton = styled.button`
    width: 100%;
    padding: 12px;
    background-color: #333;
    color: white;
    border: none;
    border-radius: 6px;
    font-weight: bold;
    cursor: pointer;
    &:hover { background-color: #555; }
`;

export const DeleteButton = styled.button`
    width: 100%;
    padding: 12px;
    background-color: #ff4d4f;
    color: white;
    border: none;
    border-radius: 6px;
    font-weight: bold;
    cursor: pointer;
    &:hover { background-color: #d9363e; }
`;