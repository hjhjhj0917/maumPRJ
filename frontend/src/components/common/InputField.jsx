import React, { useState } from 'react';
import * as S from '../../style/components/common/InputField.styles';

const InputField = ({
                        label, name, type = "text", value, onChange, placeholder,
                        errorMsg, readOnly, actionBtn, isPassword, onClick, children
                    }) => {
    const [showPw, setShowPw] = useState(false);
    const inputType = isPassword ? (showPw ? "text" : "password") : type;

    return (
        <S.InputGroup>
            <S.LabelRow>
                <label>{label}</label>
                <S.FieldMessage $show={!!errorMsg} $type={errorMsg?.type}>
                    {errorMsg?.text}
                </S.FieldMessage>
            </S.LabelRow>

            <S.InputWrapper>
                <S.StyledInput
                    type={inputType}
                    name={name}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    readOnly={readOnly}
                    onClick={onClick}
                />
                {actionBtn && (
                    <S.ActionButton
                        type="button"
                        onClick={actionBtn.onClick}
                        disabled={actionBtn.disabled}
                    >
                        {actionBtn.text}
                    </S.ActionButton>
                )}
                {isPassword && (
                    <S.ToggleIcon
                        className={`fa-regular ${showPw ? 'fa-eye-slash' : 'fa-eye'}`}
                        $active={showPw}
                        onClick={() => setShowPw(!showPw)}
                    />
                )}
            </S.InputWrapper>
            {children}
        </S.InputGroup>
    );
};

export default InputField;