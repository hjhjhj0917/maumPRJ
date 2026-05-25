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
        isDropdownOpen,
        activeModalType,
        editForm,
        verifyState,
        openModal,
        selectCharacter,
        closeModal,
        cancelModal,
        toggleDropdown,
        openActionModal,
        closeActionModal,
        handleEditChange,
        verifyCurrentPasswordAction,
        sendEmailCodeAction,
        verifyEmailCodeAction,
        searchAddressAction,
        updateAccountAction,
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

                    <S.OptionsWrapper>
                        <S.EllipsisIcon className="fa-solid fa-ellipsis-vertical" onClick={toggleDropdown} />
                        {isDropdownOpen && (
                            <S.DropdownMenu>
                                <S.DropdownItem onClick={() => openActionModal('edit')}>프로필 수정</S.DropdownItem>
                                <S.DropdownItem onClick={() => openActionModal('withdraw')}>회원 탈퇴</S.DropdownItem>
                            </S.DropdownMenu>
                        )}
                    </S.OptionsWrapper>
                </S.HeaderContent>
            </S.HeaderBanner>

            <S.MainContent>
            </S.MainContent>

            {isModalOpen && (
                <S.ModalOverlay onClick={cancelModal}>
                    <S.ModalContent onClick={e => e.stopPropagation()}>
                        <S.ModalHeader>
                            <h2>
                                <img src={logo} alt="logo" />
                                마음 캐릭터
                            </h2>
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

            {activeModalType === 'edit' && (
                <S.ModalOverlay onClick={closeActionModal}>
                    <S.ModalContent onClick={e => e.stopPropagation()}>
                        <S.ModalHeader>
                            <h2>프로필 수정</h2>
                            <S.CloseIcon onClick={closeActionModal}>&times;</S.CloseIcon>
                        </S.ModalHeader>

                        <S.ModalScrollContent>
                            <S.FormGroup>
                                <S.FormLabel>비밀번호 변경</S.FormLabel>
                                <S.InputRow>
                                    <S.FormInput
                                        type="password"
                                        name="currentPassword"
                                        placeholder="현재 비밀번호"
                                        value={editForm.currentPassword}
                                        onChange={handleEditChange}
                                        disabled={verifyState.isPasswordVerified}
                                    />
                                    <S.VerifyButton onClick={verifyCurrentPasswordAction} disabled={verifyState.isPasswordVerified}>
                                        {verifyState.isPasswordVerified ? '인증완료' : '인증'}
                                    </S.VerifyButton>
                                </S.InputRow>
                                {verifyState.isPasswordVerified && (
                                    <S.FormInput
                                        type="password"
                                        name="newPassword"
                                        placeholder="새 비밀번호 입력"
                                        value={editForm.newPassword}
                                        onChange={handleEditChange}
                                        style={{marginTop: '8px'}}
                                    />
                                )}
                            </S.FormGroup>

                            <S.FormGroup>
                                <S.FormLabel>이메일 변경</S.FormLabel>
                                <S.InputRow>
                                    <S.FormInput
                                        type="email"
                                        name="newEmail"
                                        placeholder="새 이메일 주소"
                                        value={editForm.newEmail}
                                        onChange={handleEditChange}
                                        disabled={verifyState.isEmailVerified}
                                    />
                                    <S.VerifyButton onClick={sendEmailCodeAction} disabled={verifyState.isEmailVerified}>
                                        발송
                                    </S.VerifyButton>
                                </S.InputRow>
                                {verifyState.isEmailCodeSent && !verifyState.isEmailVerified && (
                                    <S.InputRow style={{marginTop: '8px'}}>
                                        <S.FormInput
                                            type="text"
                                            name="emailCode"
                                            placeholder="인증번호 입력"
                                            value={editForm.emailCode}
                                            onChange={handleEditChange}
                                        />
                                        <S.VerifyButton onClick={verifyEmailCodeAction}>
                                            확인
                                        </S.VerifyButton>
                                    </S.InputRow>
                                )}
                                {verifyState.isEmailVerified && (
                                    <div style={{ fontSize: '13px', color: '#28a745', marginTop: '8px', fontWeight: 'bold' }}>
                                        이메일 인증이 완료되었습니다.
                                    </div>
                                )}
                            </S.FormGroup>

                            <S.FormGroup>
                                <S.FormLabel>주소 변경</S.FormLabel>
                                <S.InputRow>
                                    <S.FormInput
                                        type="text"
                                        name="newAddr"
                                        value={editForm.newAddr}
                                        readOnly
                                    />
                                    <S.VerifyButton onClick={searchAddressAction}>
                                        우편번호
                                    </S.VerifyButton>
                                </S.InputRow>
                                <S.FormInput
                                    type="text"
                                    name="newDetailAddr"
                                    placeholder="상세 주소 입력"
                                    value={editForm.newDetailAddr}
                                    onChange={handleEditChange}
                                    style={{marginTop: '8px'}}
                                />
                            </S.FormGroup>
                        </S.ModalScrollContent>

                        <S.ModalFooter>
                            <S.CancelButton onClick={closeActionModal}>취소</S.CancelButton>
                            <S.ConfirmButton onClick={updateAccountAction} $themeColor={currentColor}>수정 완료</S.ConfirmButton>
                        </S.ModalFooter>
                    </S.ModalContent>
                </S.ModalOverlay>
            )}

            {activeModalType === 'withdraw' && (
                <S.ModalOverlay onClick={closeActionModal}>
                    <S.ModalContent onClick={e => e.stopPropagation()}>
                        <S.ModalHeader>
                            <h2>회원 탈퇴</h2>
                            <S.CloseIcon onClick={closeActionModal}>&times;</S.CloseIcon>
                        </S.ModalHeader>
                        <div style={{ textAlign: 'center', padding: '40px 0', fontSize: '16px', color: '#333', lineHeight: '1.6' }}>
                            정말 탈퇴하시겠습니까?<br />
                            탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.
                        </div>
                        <S.ModalFooter>
                            <S.CancelButton onClick={closeActionModal}>취소</S.CancelButton>
                            <S.ConfirmButton onClick={handleWithdrawal} style={{ backgroundColor: '#ff4d4f', color: '#fff' }}>탈퇴하기</S.ConfirmButton>
                        </S.ModalFooter>
                    </S.ModalContent>
                </S.ModalOverlay>
            )}
        </S.PageWrapper>
    );
};

export default ProfilePage;