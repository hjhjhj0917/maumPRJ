import styled from 'styled-components';
import { Link } from 'react-router-dom';

export const HeaderContainer = styled.header`
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 40px;
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
    z-index: 1000;
    background-color: transparent;
    box-shadow: none;
    border-bottom: none;
    transition: all 0.3s ease;
    box-sizing: border-box;

    @media (max-width: 768px) {
        padding: 20px;
    }
`;

export const LogoContainer = styled(Link)`
    display: flex;
    align-items: center;
    text-decoration: none;
    z-index: 1001;
    font-size: 22px;
    font-weight: 500;
    color: #000000;
`;

export const LogoImage = styled.img`
    height: 40px;
    width: auto;
    object-fit: contain;
    display: block;
    margin-right: 8px;
`;

export const MenuToggle = styled.div`
    display: none;
    flex-direction: column;
    cursor: pointer;
    gap: 5px;
    z-index: 1001;
    @media (max-width: 768px) { display: flex; }
`;

export const Bar = styled.span`
    width: 25px;
    height: 3px;
    background-color: #333333;
    border-radius: 3px;
`;

export const NavMenu = styled.ul`
    display: flex;
    list-style: none;
    align-items: center;
    gap: 30px;
    margin: 0;
    padding: 0;

    @media (max-width: 768px) {
        display: ${props => props.$isOpen ? 'flex' : 'none'};
        position: absolute;
        top: 100%;
        left: 0;
        width: 100%;
        flex-direction: column;
        background-color: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(10px);
        padding: 20px;
        gap: 15px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    }
`;

export const NavItem = styled.li`
    display: flex;
    align-items: center;
    @media (max-width: 768px) { width: 100%; }
`;

export const NavLink = styled(Link)`
    text-decoration: none;
    color: #333333;
    font-weight: 500;
    font-size: 14px;
    transition: color 0.3s;
    &:hover { color: #c37975; }
`;

export const UserProfileContainer = styled.div`
    display: flex;
    align-items: center;
    border-radius: 40px;
    height: 44px;
    padding: 4px 17px 4px 4px;
    width: fit-content;
`;

export const ProfileImg = styled.img`
    width: 37px;
    height: 37px;
    border-radius: 50%;
    border: 2px solid #FFE3A2;
    object-fit: cover;
`;

export const UserName = styled.span`
    font-size: 14px;
    font-weight: 500;
    color: #000000;
    margin-left: 10px;
`;