import styled from 'styled-components';

export const PageWrapper = styled.div`
    width: 100%;
    min-height: 100vh;
    background-color: #f4f6f8;
    display: flex;
    flex-direction: column;
`;

export const HeaderBanner = styled.div`
    width: 100%;
    background-color: ${props => props.$themeColor || '#7b83c7'};
    padding: 60px 0;
    display: flex;
    justify-content: center;
    transition: background-color 0.4s ease;
`;

export const HeaderContent = styled.div`
    width: 100%;
    max-width: 1000px;
    display: flex;
    align-items: center;
    gap: 40px;
    padding: 0 20px;
`;

export const EditIcon = styled.i`
    position: absolute;
    bottom: 10px;
    right: 10px;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;
    font-size: 14px;
    z-index: 2;
`;

export const AvatarWrapper = styled.div`
    width: 160px;
    height: 160px;
    border-radius: 50%;
    border: 6px solid ${props => props.$themeColor ? `color-mix(in srgb, ${props.$themeColor}, black 15%)` : 'rgba(255, 255, 255, 0.3)'};
    flex-shrink: 0;
    position: relative;
    cursor: pointer;
    transition: border-color 0.4s ease;

    &:hover ${EditIcon} {
        opacity: 1;
    }
`;

export const AvatarImage = styled.img`
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
    z-index: 1;
`;

export const HeaderInfo = styled.div`
    display: flex;
    flex-direction: column;
    color: #333;
    transition: color 0.4s ease;
`;

export const Greeting = styled.span`
    font-size: 24px;
    font-weight: 300;
    opacity: 0.9;
`;

export const UserName = styled.h1`
    font-size: 42px;
    font-weight: 700;
    margin: 5px 0;
`;

export const UserId = styled.span`
    font-size: 18px;
    font-weight: 400;
    opacity: 0.8;
    margin-bottom: 20px;
`;

export const ContactInfo = styled.div`
    display: flex;
    gap: 25px;
    font-size: 14px;
`;

export const ContactItem = styled.div`
    display: flex;
    align-items: center;
    gap: 8px;
    opacity: 0.9;
`;

export const MainContent = styled.div`
    width: 100%;
    max-width: 1000px;
    margin: -40px auto 40px;
    padding: 0 20px;
    box-sizing: border-box;
`;

export const CardsGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 25px;
`;

export const Card = styled.div`
    background: white;
    border-radius: 12px;
    padding: 30px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
`;

export const CardTitle = styled.h3`
    font-size: 16px;
    color: #999;
    text-transform: uppercase;
    letter-spacing: 1.5px;
    margin-bottom: 25px;
    border-bottom: 1px solid #eee;
    padding-bottom: 15px;
`;

export const InputGroup = styled.div`
    margin-bottom: 20px;
`;

export const Label = styled.label`
    display: block;
    font-size: 13px;
    color: #666;
    margin-bottom: 8px;
    font-weight: 600;
`;

export const Input = styled.input`
    width: 100%;
    padding: 12px;
    border: 1px solid #e1e1e1;
    border-radius: 8px;
    font-size: 14px;
    color: #333;
    box-sizing: border-box;
    transition: all 0.3s ease;

    &:focus {
        outline: none;
        border-color: ${props => props.$themeColor || '#7b83c7'};
        box-shadow: 0 0 0 3px ${props => props.$themeColor ? props.$themeColor + '40' : 'rgba(123, 131, 199, 0.1)'};
    }

    &:disabled {
        background-color: #f8f9fa;
        color: #999;
        cursor: not-allowed;
    }
`;

export const ButtonGroup = styled.div`
    margin-top: auto;
    padding-top: 20px;
    display: flex;
    flex-direction: column;
    gap: 12px;
`;

export const SaveButton = styled.button`
    width: 100%;
    padding: 14px;
    background-color: ${props => props.$themeColor || '#7b83c7'};
    color: #333;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: bold;
    cursor: pointer;
    transition: filter 0.2s, background-color 0.4s ease;

    &:hover {
        filter: brightness(0.9);
    }
`;

export const DeleteButton = styled.button`
    width: 100%;
    padding: 14px;
    background-color: transparent;
    color: #ff4d4f;
    border: 1px solid #ff4d4f;
    border-radius: 8px;
    font-size: 15px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background-color: #fff1f0;
    }
`;

export const ModalOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
`;

export const ModalContent = styled.div`
    background: white;
    width: 100%;
    max-width: 500px;
    border-radius: 16px;
    padding: 30px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    gap: 25px;
    
    img {
        width: 30px;
        height: auto;
    }
`;

export const ModalHeader = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
        font-size: 20px;
        font-weight: bold;
        color: #333;
    }
`;

export const CloseIcon = styled.button`
    background: none;
    border: none;
    font-size: 24px;
    color: #999;
    cursor: pointer;
`;

export const CharacterGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 15px;
`;

export const CharacterItem = styled.div`
    width: 100%;
    aspect-ratio: 1;
    border-radius: 50%;
    border: 3px solid ${props => props.$isSelected ? (props.$themeColor || '#7b83c7') : '#eee'};
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;

    &:hover {
        border-color: ${props => props.$themeColor || '#7b83c7'};
        opacity: 0.8;
    }

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
`;

export const ModalFooter = styled.div`
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 10px;
`;

export const CancelButton = styled.button`
    padding: 10px 20px;
    background-color: transparent;
    color: #666;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;

    &:hover {
        background-color: #f8f9fa;
    }
`;

export const ConfirmButton = styled.button`
    padding: 10px 20px;
    background-color: #333;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: filter 0.2s, background-color 0.4s ease;

    &:hover {
        filter: brightness(0.9);
    }
`;