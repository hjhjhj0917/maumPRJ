import React, { useState } from 'react';

const InputField = ({
                        label, name, type = "text", value, onChange, placeholder,
                        errorMsg, readOnly, actionBtn, isPassword, onClick, children
                    }) => {
    const [showPw, setShowPw] = useState(false);
    const inputType = isPassword ? (showPw ? "text" : "password") : type;

    return (
        <div className="input-group" style={{ position: 'relative' }}>
            <div className="label-row">
                <label>{label}</label>
                <span className={`field-message ${errorMsg?.type || ''} ${errorMsg ? 'show' : ''}`}>
                    {errorMsg?.text}
                </span>
            </div>
            <div className={isPassword || actionBtn ? "flex-row password-wrapper" : ""}>
                <input
                    type={inputType}
                    name={name}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    readOnly={readOnly}
                    style={readOnly && !actionBtn ? { cursor: 'pointer', backgroundColor: '#fff' } : {}}
                    onClick={onClick}
                />
                {actionBtn && (
                    <button
                        type="button"
                        className="btn-check"
                        onClick={actionBtn.onClick}
                        disabled={actionBtn.disabled}
                    >
                        {actionBtn.text}
                    </button>
                )}
                {isPassword && (
                    <i
                        className={`fa-regular ${showPw ? 'fa-eye-slash active' : 'fa-eye'} toggle-password`}
                        onClick={() => setShowPw(!showPw)}
                    ></i>
                )}
            </div>
            {children}
        </div>
    );
};

export default InputField;