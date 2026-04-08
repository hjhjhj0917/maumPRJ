import React from 'react';
import * as S from '../../style/components/common/CustomModal.styles';

const CustomModal = ({ isOpen, title, message, isConfirm, onConfirm, onCancel }) => {
    if (!isOpen) return null;

    return (
        <S.ModalOverlay>
            <S.ModalBox>
                <S.CloseButton onClick={onCancel}>
                    <i className="fa-solid fa-xmark"></i>
                </S.CloseButton>

                <S.ModalContent>
                    <h2>{title || (isConfirm ? '확인' : '알림')}</h2>
                    <p>{message}</p>
                </S.ModalContent>

                <S.ModalActions>
                    {isConfirm && (
                        <S.CancelButton type="button" onClick={onCancel}>
                            취소
                        </S.CancelButton>
                    )}
                    <S.OkButton type="button" onClick={onConfirm || onCancel}>
                        확인
                    </S.OkButton>
                </S.ModalActions>
            </S.ModalBox>
        </S.ModalOverlay>
    );
};

export default CustomModal;