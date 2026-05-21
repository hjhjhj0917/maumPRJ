import styled from 'styled-components';

export const Container = styled.div`
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
`;

export const StickyHeader = styled.div`
    position: sticky;
    top: 0;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: #f4f7f9;
    z-index: 10;
    padding-top: 40px;
    padding-bottom: 10px;
`;

export const MonthNav = styled.div`
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 40px;
    margin-top: 20px;
    margin-bottom: 30px;
`;

export const ArrowButton = styled.button`
    background: none;
    border: none;
    font-size: 24px;
    color: #FFD166;
    cursor: pointer;
    padding: 10px;
    transition: transform 0.2s;

    &:hover {
        transform: scale(1.1);
    }
`;

export const MonthText = styled.h2`
    font-size: 28px;
    font-weight: 700;
    color: #FFD166;
    margin: 0;
    min-width: 140px;
    text-align: center;
`;

export const FilterSection = styled.div`
    width: 100%;
    max-width: 820px;
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
    margin-bottom: 25px;
    padding: 0 20px;
`;

export const FilterChip = styled.button`
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    background-color: ${props => props.$isSelected ? props.$color : '#fff'};
    color: ${props => props.$isSelected ? '#fff' : '#666'};
    border: 1px solid ${props => props.$isSelected ? props.$color : '#e0e0e0'};
    box-shadow: ${props => props.$isSelected ? '0 4px 10px rgba(0, 0, 0, 0.1)' : 'none'};

    &:hover {
        border-color: ${props => props.$color};
        transform: translateY(-1px);
    }
`;

export const ChipCircle = styled.div`
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background-color: ${props => props.$isSelected ? '#fff' : props.$color};
`;

export const Controls = styled.div`
    width: 100%;
    max-width: 800px;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 24px;
    margin-bottom: 20px;
`;

export const SearchBox = styled.div`
    display: flex;
    align-items: center;
    background-color: #ffffff;
    border-radius: 30px;
    padding: 12px 24px;
    width: 600px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);

    input {
        border: none;
        background: transparent;
        outline: none;
        flex: 1;
        font-size: 15px;
        color: #333;

        &::placeholder {
            color: #aaa;
        }
    }

    i {
        color: #888;
        cursor: pointer;
        font-size: 18px;
    }
`;

export const FilterButton = styled.button`
    background: none;
    border: none;
    font-size: 15px;
    color: #555;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;

    i {
        font-size: 12px;
        color: #555;
    }
`;

export const ListWrapper = styled.div`
    width: 100%;
    max-width: 800px;
    padding: 10px 20px 40px 10px;
`;

export const ListItem = styled.div`
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 60px;
    padding: 0 30px;
    border-radius: 30px;
    margin-bottom: 20px;
    background-color: #fff;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
    }
`;

export const DayText = styled.span`
    font-size: 16px;
    font-weight: 600;
    color: #333;
    width: 60px;
`;

export const ResultDateText = styled.span`
    font-size: 14px;
    font-weight: 500;
    color: #888;
    width: 100px;
`;

export const TitleText = styled.span`
    flex: 1;
    text-align: left;
    font-size: 15px;
    color: #333;
    font-weight: 500;
    padding-left: 20px;
`;

export const AddIcon = styled.div`
    flex: 1;
    text-align: center;
    color: #FFD166;
    font-size: 18px;
`;

export const ColorCircle = styled.div`
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background-color: ${props => props.$color};
`;

export const NoResultText = styled.div`
    text-align: center;
    padding: 50px 0;
    color: #888;
    font-size: 16px;
`;