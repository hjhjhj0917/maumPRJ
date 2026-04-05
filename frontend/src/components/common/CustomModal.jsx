import React from 'react';
import './CustomModal.css';

const CustomModal = ({ isOpen, title, message, isConfirm, onConfirm, onCancel }) => {
    if (!isOpen) return null;

    return (
        <div className="custom-modal-overlay">
            <div className="custom-modal-box">
                <span className="custom-modal-close" onClick={onCancel}>
                    <i className="fa-solid fa-xmark"></i>
                </span>

                <div className="custom-modal-content">
                    <h2>{title || (isConfirm ? '확인' : '알림')}</h2>
                    <p>{message}</p>
                </div>

                <div className="custom-modal-actions">
                    {isConfirm && (
                        <button type="button" className="btn-modal-cancel" onClick={onCancel}>
                            취소
                        </button>
                    )}
                    <button type="button" className="btn-modal-ok" onClick={onConfirm || onCancel}>
                        확인
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CustomModal;