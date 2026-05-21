import React from 'react';
import * as S from '../../style/pages/Account/Profile.styles';
import { useProfile } from '../../hooks/account/useProfileForm';
import logo from '../../assets/images/includes/logo.png';

const ProfilePage = () => {
    const {
        userInfo,
        characters,
        isModalOpen,
        selectedCharacterUrl,
        currentColor,
        handleChange,
        openModal,
        selectCharacter,
        closeModal,
        cancelModal,
        handleUpdate,
        handleWithdrawal
    } = useProfile();

    return (
        <S.PageWrapper>
            <S.HeaderBanner $themeColor={currentColor}>
                <S.HeaderContent>
                    <S.AvatarWrapper onClick={openModal} $themeColor={currentColor}>
                        <S.AvatarImage src={userInfo.profileImgUrl} alt="Profile" />
                        <S.EditIcon className="fa-solid fa-pencil" />
                    </S.AvatarWrapper>
                    <S.HeaderInfo>
                        <S.UserName>{userInfo.userName || 'User'}님</S.UserName>
                        <S.UserId>@{userInfo.userId}</S.UserId>
                        <S.ContactInfo>
                            <S.ContactItem>
                                <span><i className="fa-solid fa-envelope"></i></span> {userInfo.email}
                            </S.ContactItem>
                            <S.ContactItem>
                                <span><i className="fa-solid fa-location-dot"></i></span> {userInfo.addr + userInfo.detailAddr || '등록된 주소가 없습니다'}
                            </S.ContactItem>
                        </S.ContactInfo>
                    </S.HeaderInfo>
                </S.HeaderContent>
            </S.HeaderBanner>

            <S.MainContent>
                {/*<S.CardsGrid>*/}
                {/*    <S.Card>*/}
                {/*        <S.CardTitle>기본 정보</S.CardTitle>*/}
                {/*        <S.InputGroup>*/}
                {/*            <S.Label>이름</S.Label>*/}
                {/*            <S.Input name="userName" value={userInfo.userName} onChange={handleChange} $themeColor={currentColor} />*/}
                {/*        </S.InputGroup>*/}
                {/*        <S.InputGroup>*/}
                {/*            <S.Label>생년월일</S.Label>*/}
                {/*            <S.Input type="date" name="birthDate" value={userInfo.birthDate} onChange={handleChange} $themeColor={currentColor} />*/}
                {/*        </S.InputGroup>*/}
                {/*        <S.InputGroup>*/}
                {/*            <S.Label>프로필 이미지 URL</S.Label>*/}
                {/*            <S.Input name="profileImgUrl" value={userInfo.profileImgUrl} onChange={handleChange} disabled $themeColor={currentColor} />*/}
                {/*        </S.InputGroup>*/}
                {/*    </S.Card>*/}

                {/*    <S.Card>*/}
                {/*        <S.CardTitle>연락처 및 주소</S.CardTitle>*/}
                {/*        <S.InputGroup>*/}
                {/*            <S.Label>이메일 (수정불가)</S.Label>*/}
                {/*            <S.Input value={userInfo.email} disabled $themeColor={currentColor} />*/}
                {/*        </S.InputGroup>*/}
                {/*        <S.InputGroup>*/}
                {/*            <S.Label>기본 주소</S.Label>*/}
                {/*            <S.Input name="addr" value={userInfo.addr} onChange={handleChange} $themeColor={currentColor} />*/}
                {/*        </S.InputGroup>*/}
                {/*        <S.InputGroup>*/}
                {/*            <S.Label>상세 주소</S.Label>*/}
                {/*            <S.Input name="detailAddr" value={userInfo.detailAddr} onChange={handleChange} $themeColor={currentColor} />*/}
                {/*        </S.InputGroup>*/}
                {/*    </S.Card>*/}

                {/*    <S.Card>*/}
                {/*        <S.CardTitle>보안 및 계정 관리</S.CardTitle>*/}
                {/*        <S.InputGroup>*/}
                {/*            <S.Label>새 비밀번호</S.Label>*/}
                {/*            <S.Input type="password" name="password" value={userInfo.password} placeholder="변경 시에만 입력" onChange={handleChange} $themeColor={currentColor} />*/}
                {/*        </S.InputGroup>*/}

                {/*        <S.ButtonGroup>*/}
                {/*            <S.SaveButton onClick={handleUpdate} $themeColor={currentColor}>정보 수정 완료</S.SaveButton>*/}
                {/*            <S.DeleteButton onClick={handleWithdrawal}>회원 탈퇴</S.DeleteButton>*/}
                {/*        </S.ButtonGroup>*/}
                {/*    </S.Card>*/}
                {/*</S.CardsGrid>*/}
            </S.MainContent>

            {isModalOpen && (
                <S.ModalOverlay onClick={cancelModal}>
                    <S.ModalContent onClick={e => e.stopPropagation()}>
                        <S.ModalHeader>
                            <img src={logo} alt="logo" />
                            <h2>마음 캐릭터</h2>
                            <S.CloseIcon onClick={cancelModal}>&times;</S.CloseIcon>
                        </S.ModalHeader>

                        <S.CharacterGrid>
                            {characters.map((url, index) => (
                                <S.CharacterItem
                                    key={index}
                                    $isSelected={url === selectedCharacterUrl}
                                    $themeColor={currentColor}
                                    onClick={() => selectCharacter(url)}
                                >
                                    <img src={url} alt={`Character ${index + 1}`} />
                                </S.CharacterItem>
                            ))}
                        </S.CharacterGrid>

                        <S.ModalFooter>
                            <S.CancelButton onClick={cancelModal}>취소</S.CancelButton>
                            <S.ConfirmButton onClick={closeModal} $themeColor={currentColor}>완료</S.ConfirmButton>
                        </S.ModalFooter>
                    </S.ModalContent>
                </S.ModalOverlay>
            )}
        </S.PageWrapper>
    );
};

export default ProfilePage;