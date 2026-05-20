import styled from 'styled-components';

export const Container = styled.div`
    width: 100%;
    height: 100vh;
    box-sizing: border-box;
    background-color: #f8f9fa;
    display: flex;
    flex-direction: column;
    padding: 20px;
`;

export const MapWrapper = styled.div`
    width: 100%;
    flex: 1;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    position: relative;

    &::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 80px;
        background: linear-gradient(to bottom, rgba(248, 249, 250, 1) 0%, rgba(248, 249, 250, 0) 100%);
        pointer-events: none;
        z-index: 2;
    }
`;

export const LoadingErrorText = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    font-size: 1.2rem;
    color: ${(props) => (props.$isError ? '#d9534f' : '#666')};
`;

export const ControlsContainer = styled.div`
    position: absolute;
    bottom: 30px;
    right: 30px;
    z-index: 20;
`;

export const ButtonsWrapper = styled.div`
    display: flex;
    gap: 12px;
    justify-content: flex-end;
`;

export const MyLocationButton = styled.button`
    width: 50px;
    height: 50px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 22px;
    color: #333;
    background-color: white;
    border: 1px solid #ccc;
    border-radius: 50%;
    box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    cursor: pointer;
    flex-shrink: 0;

    &:hover {
        background-color: #f0f0f0;
    }
`;

export const FilterPanel = styled.div`
    position: absolute;
    bottom: 100%;
    right: 0;
    margin-bottom: 15px;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    align-content: flex-end;
    gap: 8px;
    background-color: white;
    padding: 15px;
    border-radius: 16px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.15);
    width: max-content;
    max-width: 320px;
    opacity: ${(props) => (props.$isOpen ? 1 : 0)};
    visibility: ${(props) => (props.$isOpen ? 'visible' : 'hidden')};
    transform: ${(props) => (props.$isOpen ? 'translateY(0)' : 'translateY(15px)')};
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    pointer-events: ${(props) => (props.$isOpen ? 'auto' : 'none')};
`;

export const FilterToggleButton = styled.button`
    width: 50px;
    height: 50px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 20px;
    color: #333;
    background-color: white;
    border: 1px solid #ccc;
    border-radius: 50%;
    box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    cursor: pointer;
    flex-shrink: 0;

    &:hover {
        background-color: #f0f0f0;
    }
`;

export const FilterChip = styled.button`
    padding: 8px 14px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: bold;
    border: 1px solid ${(props) => (props.$isActive ? '#FFC130' : '#e9ecef')};
    background-color: ${(props) => (props.$isActive ? '#FFC130' : 'white')};
    color: ${(props) => (props.$isActive ? '#212529' : '#495057')};
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background-color: ${(props) => (props.$isActive ? '#e6ae2b' : '#f8f9fa')};
    }
`;

export const SearchWrapper = styled.div`
    position: absolute;
    top: 50px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 10;
    width: 90%;
    max-width: 600px;
    display: flex;
    flex-direction: column;
`;

export const SearchContainer = styled.form`
    display: flex;
    width: 100%;
    background-color: white;
    border-radius: 25px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    overflow: hidden;
`;

export const SearchInput = styled.input`
    flex: 1;
    border: none;
    padding: 15px 20px;
    font-size: 16px;
    outline: none;
    background: transparent;
`;

export const SearchButton = styled.button`
    border: none;
    background-color: transparent;
    padding: 0 20px;
    cursor: pointer;
    color: #666;
    font-size: 18px;

    &:hover {
        color: #333;
    }
`;

export const DropdownContainer = styled.ul`
    background-color: white;
    border-radius: 20px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    margin-top: 10px;
    padding: 5px;
    list-style: none;
    max-height: 250px;
    overflow-y: auto;
    width: 100%;

    &::-webkit-scrollbar {
        width: 8px;
    }
    &::-webkit-scrollbar-track {
        background: transparent;
        border-radius: 12px;
        margin-top: 12px;
        margin-bottom: 12px;
    }
    &::-webkit-scrollbar-thumb {
        background: #ced4da;
        border-radius: 12px;
        border: 2px solid white;
    }
    &::-webkit-scrollbar-thumb:hover {
        background: #adb5bd;
    }
`;

export const DropdownItem = styled.li`
    padding: 12px 20px;
    border-bottom: 1px solid #f1f3f5;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    gap: 4px;

    &:last-child {
        border-bottom: none;
    }

    &:hover {
        background-color: #f8f9fa;
    }
`;

export const DropdownItemHeader = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
`;

export const DropdownItemName = styled.span`
    font-size: 15px;
    font-weight: bold;
    color: #333;
`;

export const DropdownItemDistance = styled.span`
    font-size: 13px;
    color: #4A90E2;
    font-weight: bold;
`;

export const DropdownItemAddress = styled.span`
    font-size: 12px;
    color: #888;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
`;

export const OverlayContainer = styled.div`
    position: absolute;
    bottom: 55px;
    left: 50%;
    transform: translateX(-50%);
    background-color: white;
    border-radius: 100px;
    border: 1px solid #ccc;
    box-shadow: 0px 4px 15px rgba(0, 0, 0, 0.15);
    padding: 24px 20px 24px 32px;
    min-width: 280px;
    width: max-content;
    height: auto;
    min-height: 110px;
    max-width: 600px;
    box-sizing: border-box;
    z-index: 5;
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 20px;
`;

export const OverlayLeftSection = styled.div`
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
    width: 100%;
`;

export const OverlayHeader = styled.div`
    display: flex;
    justify-content: flex-start;
    align-items: flex-start;
    margin-bottom: 8px;
    padding-bottom: 8px;
    width: 100%;
`;

export const OverlayTitle = styled.h3`
    margin: 0;
    font-size: 15px;
    font-weight: bold;
    color: #333;
    line-height: 1.4;
    word-break: break-word;
    white-space: normal;
`;

export const OverlayBody = styled.div`
    display: flex;
    flex-direction: column;
    gap: 6px;
    width: 100%;
`;

export const InfoText = styled.div`
    display: flex;
    align-items: flex-start;
    gap: 8px;
    margin: 0;
    font-size: 12px;
    color: #555;
    line-height: 1.4;
    word-break: break-word;
    white-space: normal;

    i {
        margin-top: 3px;
        color: #adb5bd;
        min-width: 14px;
        text-align: center;
    }

    span, a {
        flex: 1;
    }

    a {
        color: #4A90E2;
        text-decoration: none;
        word-break: break-all;
        &:hover { text-decoration: underline; }
    }
`;

export const HashtagText = styled.div`
    display: inline-block;
    font-size: 13px;
    font-weight: bold;
    color: #4A90E2;
    margin-top: 4px;
    word-break: keep-all;
`;

export const OverlayRightSection = styled.div`
    width: auto;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-shrink: 0;
`;

export const RouteButtonRound = styled.a`
    display: flex;
    justify-content: center;
    align-items: center;
    width: 76px;
    height: 76px;
    border-radius: 50%;
    background-color: #FFC130;
    color: #212529;
    text-decoration: none;
    box-shadow: 0 4px 10px rgba(0,0,0,0.15);
    font-size: 34px;
    transition: all 0.2s;

    &:hover {
        background-color: #e6ae2b;
        transform: translateY(-3px);
        box-shadow: 0 6px 15px rgba(0,0,0,0.2);
    }
`;