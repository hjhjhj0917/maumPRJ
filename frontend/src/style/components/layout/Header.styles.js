import styled from 'styled-components';
import { Link } from 'react-router-dom';

export const HeaderContainer = styled.header`
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 11px 50px;
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
    z-index: 100;
    background-color: transparent;
    box-shadow: none;
    border-bottom: none;
    transition: all 0.3s ease;

    @media (max-width: 768px) {
        padding: 15px 20px;
    }
`;

export const LogoContainer = styled(Link)`
    display: flex;
    align-items: center;
    text-decoration: none;
    z-index: 1001;
`;

export const LogoImage = styled.img`
    height: 46px;
    width: auto;
    object-fit: contain;
    display: block;
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
    background-color: #fff;
    border-radius: 3px;
`;

export const NavMenu = styled.ul`
    display: flex;
    list-style: none;
    align-items: center;
    gap: 30px;

    @media (max-width: 768px) {
        display: ${props => props.$isOpen ? 'flex' : 'none'};
        position: absolute;
        top: 100%;
        left: 0;
        width: 100%;
        flex-direction: column;
        background-color: rgba(30, 31, 32, 0.9);
        padding: 20px;
        gap: 0;
    }
`;

export const NavItem = styled.li`
    display: flex;
    align-items: center;
    @media (max-width: 768px) { width: 100%; }
`;

export const NavLink = styled(Link)`
    text-decoration: none;
    color: #333;
    font-weight: 500;
    font-size: 13px;
    transition: color 0.3s;
    &:hover { color: #FFD166; }
`;

export const UserProfileContainer = styled.div`
    display: flex;
    align-items: center;
    background-color: #fff;
    border: 2px solid #FFD166;
    border-radius: 40px;
    height: 44px;
    padding: 4px 17px 4px 4px;
    width: fit-content;
`;

export const ProfileImg = styled.img`
    width: 34px;
    height: 34px;
    border-radius: 50%;
    object-fit: cover;
`;

export const UserName = styled.span`
    font-size: 14px;
    font-weight: 500;
    color: #000;
    margin-left: 10px;
`;