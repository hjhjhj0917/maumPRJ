import styled from 'styled-components';

export const ItemWrapper = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    transition: transform 0.2s ease;
    width: 80px;

    &:hover {
        transform: translateY(-5px);
    }
`;

export const ItemImage = styled.img`
    width: 80px;  
    height: 80px; 
    border-radius: 50%;
    object-fit: cover;
    margin-bottom: 10px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
    transition: all 0.2s ease;
    border: ${props => props.$isSelected ? '4px solid #FFD166' : '4px solid transparent'};
`;

export const ItemName = styled.span`
    font-size: 14px;
    color: #333;
    font-weight: 700;
    white-space: nowrap;
`;