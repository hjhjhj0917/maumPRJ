import React from 'react';
import CustomModal from '../../components/common/CustomModal';
import ZodiacItem from '../../components/account/ZodiacItem';
import { useProfileForm } from '../../hooks/pages/account/useProfileForm';
import * as S from '../../style/pages/Account/Profile.styles';

const zodiacList = [
    { id: 'mouse', name: '쥐', img: '/images/account/profile1.png' },
    { id: 'cow', name: '소', img: '/images/account/profile2.png' },
    { id: 'tiger', name: '호랑이', img: '/images/account/profile3.png' },
    { id: 'rabbit', name: '토끼', img: '/images/account/profile4.png' },
    { id: 'dragon', name: '용', img: '/images/account/profile5.png' },
    { id: 'snake', name: '뱀', img: '/images/account/profile6.png' },
    { id: 'horse', name: '말', img: '/images/account/profile7.png' },
    { id: 'sheep', name: '양', img: '/images/account/profile8.png' },
    { id: 'monkey', name: '원숭이', img: '/images/account/profile9.png' },
    { id: 'chicken', name: '닭', img: '/images/account/profile10.png' },
    { id: 'dog', name: '개', img: '/images/account/profile11.png' },
    { id: 'pig', name: '돼지', img: '/images/account/profile12.png' },
];

const Profile = () => {
    const {
        selectedZodiac,
        setSelectedZodiac,
        modal,
        handleSubmit,
        handleModalClose,
        handleModalConfirm
    } = useProfileForm(zodiacList[6]);

    return (
        <S.ProfileWrapper>
            <CustomModal
                isOpen={modal.show}
                title={modal.title}
                message={modal.message}
                isConfirm={false}
                onCancel={handleModalClose}
                onConfirm={handleModalConfirm}
            />

            <S.Container>
                <S.ProfileForm onSubmit={handleSubmit}>
                    <S.ProfileLayout>
                        <S.PreviewArea>
                            <S.PreviewCircle>
                                <img src={selectedZodiac.img} alt="Preview" />
                            </S.PreviewCircle>
                        </S.PreviewArea>

                        <S.SelectionArea>
                            <S.ZodiacGrid>
                                {zodiacList.map((zodiac) => (
                                    <ZodiacItem
                                        key={zodiac.id}
                                        data={zodiac}
                                        isSelected={selectedZodiac.id === zodiac.id}
                                        onClick={setSelectedZodiac}
                                    />
                                ))}
                            </S.ZodiacGrid>
                        </S.SelectionArea>
                    </S.ProfileLayout>

                    <S.ButtonArea>
                        <S.BtnSave type="submit">완료</S.BtnSave>
                    </S.ButtonArea>
                </S.ProfileForm>
            </S.Container>
        </S.ProfileWrapper>
    );
};

export default Profile;