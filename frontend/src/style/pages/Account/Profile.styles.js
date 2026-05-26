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
    padding-top: 40px;
    position: relative;
`;

export const OptionsWrapper = styled.div`
    position: absolute;
    top: -20px;
    right: 20px;
`;

export const EllipsisIcon = styled.i`
    font-size: 24px;
    color: #333;
    cursor: pointer;
    padding: 10px;
    padding-top: 40px;
    opacity: 0.7;
    transition: opacity 0.2s;

    &:hover {
        opacity: 1;
    }
`;

export const DropdownMenu = styled.div`
    position: absolute;
    top: 70px;
    right: 0;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    width: 120px;
    z-index: 10;
    overflow: hidden;
    display: flex;
    flex-direction: column;
`;

export const DropdownItem = styled.button`
    padding: 12px 16px;
    border: none;
    background: none;
    text-align: left;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
        background: #f8f9fa;
    }

    &:not(:last-child) {
        border-bottom: 1px solid #eee;
    }
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
`;

export const ModalHeader = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
        font-size: 20px;
        font-weight: bold;
        color: #333;
        display: flex;
        align-items: center;
        gap: 10px;

        img {
            width: 30px;
            height: auto;
        }
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
    border: 4px solid ${props => props.$isSelected ? (props.$themeColor || '#7b83c7') : 'transparent'};
    padding: 4px;
    box-sizing: border-box;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
        border-color: ${props => props.$themeColor || '#7b83c7'};
        transform: scale(1.05);
    }

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 50%;
        box-shadow: ${props => props.$isSelected ? '0 4px 10px rgba(0,0,0,0.15)' : '0 2px 6px rgba(0,0,0,0.05)'};
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
    background-color: ${props => props.$themeColor || '#7b83c7'};
    color: #333;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: filter 0.2s, background-color 0.4s ease, opacity 0.2s;

    &:hover:not(:disabled) {
        filter: brightness(0.9);
    }

    &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }
`;

export const ModalScrollContent = styled.div`
    max-height: 60vh;
    overflow-y: auto;
    padding-right: 10px;

    &::-webkit-scrollbar {
        width: 6px;
    }
    &::-webkit-scrollbar-thumb {
        background-color: #ccc;
        border-radius: 4px;
    }
`;

export const FormGroup = styled.div`
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 24px;
    text-align: left;
`;

export const FormLabel = styled.label`
    font-size: 14px;
    font-weight: 600;
    color: #333;
`;

export const InputRow = styled.div`
    display: flex;
    gap: 10px;
`;

export const FormInput = styled.input`
    flex: 1;
    padding: 12px;
    border: 1px solid #e1e1e1;
    border-radius: 8px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;

    &:focus {
        border-color: #7b83c7;
    }
    &:disabled, &[readonly] {
        background-color: #f4f6f8;
        color: #888;
        cursor: not-allowed;
    }
`;

export const VerifyButton = styled.button`
    padding: 0 16px;
    background-color: #333;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    white-space: nowrap;

    &:disabled {
        background-color: #ccc;
        cursor: not-allowed;
    }
`;

export const StepContainer = styled.div`
    display: flex;
    justify-content: space-between;
    margin-bottom: 30px;
    position: relative;
    padding: 0 10px;
`;

export const StepLine = styled.div`
    position: absolute;
    top: 15px;
    left: 30px;
    right: 30px;
    height: 2px;
    background-color: #eee;
    z-index: 1;
`;

export const StepItem = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    z-index: 2;
`;

export const StepCircle = styled.div`
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background-color: ${props => props.$active ? (props.$themeColor || '#7b83c7') : (props.$completed ? '#333' : '#fff')};
    border: 2px solid ${props => props.$active || props.$completed ? (props.$active ? (props.$themeColor || '#7b83c7') : '#333') : '#ddd'};
    color: ${props => props.$active || props.$completed ? '#fff' : '#999'};
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 14px;
    transition: all 0.3s ease;
`;

export const StepLabel = styled.span`
    font-size: 12px;
    color: ${props => props.$active ? '#333' : '#999'};
    font-weight: ${props => props.$active ? 'bold' : 'normal'};
`;

export const StepContentWrapper = styled.div`
    min-height: 120px;
    display: flex;
    flex-direction: column;
    justify-content: center;
`;