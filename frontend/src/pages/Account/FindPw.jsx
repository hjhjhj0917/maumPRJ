import React from 'react';
import { Link } from 'react-router-dom';
import InputField from '../../components/common/InputField';
import CustomModal from '../../components/common/CustomModal';
import { useFindPwForm } from '../../hooks/account/useFindPwForm';
import * as S from '../../style/pages/Account/FindPw.styles';

const FindPw = () => {
    const {
        step,
        formData,
        handleChange,
        handleOtpChange,
        handleKeyDown,
        handlePaste,
        messages,
        modal,
        inputRefs,
        handleModalConfirm,
        handleModalCancel,
        handleStep1Submit,
        handleStep2Submit,
        handleStep3Submit,
        handleResend,
        navigate
    } = useFindPwForm();

    const steps = [
        { num: 1, label: '정보입력' },
        { num: 2, label: '인증' },
        { num: 3, label: '새 비밀번호' }
    ];

    return (
        <S.FindPwWrapper>
            <CustomModal
                isOpen={modal.show}
                title={modal.title}
                message={modal.message}
                isConfirm={modal.isConfirm}
                onCancel={handleModalCancel}
                onConfirm={handleModalConfirm}
            />

            <S.Container>
                <S.FindPwCard>
                    <S.StepperWrapper>
                        {steps.map((s, idx) => (
                            <React.Fragment key={s.num}>
                                <S.StepperItem $active={step === s.num} $completed={step > s.num}>
                                    <div className="step-label">{s.label}</div>
                                    <div className="step-circle">
                                        <i className="fa-solid fa-check"></i>
                                    </div>
                                </S.StepperItem>
                                {idx < steps.length - 1 && <S.StepLine />}
                            </React.Fragment>
                        ))}
                    </S.StepperWrapper>

                    <S.SlideViewport>
                        <S.SlideTrack $step={step}>
                            <S.FormStep $active={step === 1}>
                                <S.StepTitle>비밀번호 찾기</S.StepTitle>
                                <S.StepSubTitle>가입하신 아이디와 이메일을 입력해 주세요.</S.StepSubTitle>
                                <form onSubmit={handleStep1Submit}>
                                    <S.FormStepFieldWrapper $hasMessage={!!messages.userIdMsg || !!messages.userEmailMsg}>
                                        <InputField label="ID" name="userId" value={formData.userId}
                                                    onChange={handleChange} errorMsg={messages.userIdMsg}
                                                    placeholder="아이디를 입력하세요." />
                                        <InputField label="E-mail" name="userEmail" value={formData.userEmail}
                                                    onChange={handleChange} errorMsg={messages.userEmailMsg}
                                                    placeholder="이메일을 입력하세요." />
                                    </S.FormStepFieldWrapper>
                                    <S.BtnConfirm type="submit">인증번호 발송</S.BtnConfirm>
                                </form>
                            </S.FormStep>

                            <S.FormStep $active={step === 2}>
                                <S.StepTitle>인증 코드가 전송되었습니다</S.StepTitle>
                                <S.StepSubTitle>
                                    아래 메일로 받은 인증 코드를 입력해 주세요.<br />
                                    <span>{formData.userEmail}</span>
                                </S.StepSubTitle>
                                <form onSubmit={handleStep2Submit}>
                                    <S.VerificationWrapper onPaste={handlePaste}>
                                        {[0, 1, 2, 3, 4, 5].map((idx) => (
                                            <input
                                                key={idx}
                                                type="text"
                                                inputMode="numeric"
                                                ref={(el) => (inputRefs.current[idx] = el)}
                                                value={formData.code?.[idx] || ""}
                                                onChange={(e) => handleOtpChange(e, idx)}
                                                onKeyDown={(e) => handleKeyDown(e, idx)}
                                                placeholder="0"
                                            />
                                        ))}
                                    </S.VerificationWrapper>
                                    <S.FieldMessage $show={!!messages.codeMsg} $type={messages.codeMsg?.type}>
                                        {messages.codeMsg?.text}
                                    </S.FieldMessage>
                                    <S.ResendText>
                                        혹시 이메일을 못 받으셨나요?
                                        <button type="button" onClick={handleResend}>재전송</button>
                                    </S.ResendText>
                                    <S.BtnConfirm type="submit">인증하기</S.BtnConfirm>
                                </form>
                            </S.FormStep>

                            <S.FormStep $active={step === 3}>
                                <S.StepTitle>새 비밀번호 설정</S.StepTitle>
                                <S.StepSubTitle>보안을 위해 새로운 비밀번호를 설정해 주세요.</S.StepSubTitle>
                                <form onSubmit={handleStep3Submit}>
                                    <S.FormStepFieldWrapper $hasMessage={!!messages.passwordMsg || !!messages.passwordConfirmMsg}>
                                        <InputField label="New Password" name="password" type="password" value={formData.password}
                                                    onChange={handleChange} errorMsg={messages.passwordMsg}
                                                    placeholder="새 비밀번호를 입력하세요." />
                                        <InputField label="Confirm Password" name="passwordConfirm" type="password" value={formData.passwordConfirm}
                                                    onChange={handleChange} errorMsg={messages.passwordConfirmMsg}
                                                    placeholder="비밀번호를 한 번 더 입력하세요." />
                                    </S.FormStepFieldWrapper>
                                    <S.BtnConfirm type="submit">비밀번호 변경</S.BtnConfirm>
                                </form>
                            </S.FormStep>
                        </S.SlideTrack>
                    </S.SlideViewport>

                    <S.AuthLinks>
                        <Link to="/account/login">로그인</Link>
                        <S.Separator>|</S.Separator>
                        <Link to="/account/findId">아이디 찾기</Link>
                    </S.AuthLinks>

                    <S.SignupBox>
                        아직 회원이 아니시라면, 지금 바로 마음을 시작해 보세요.
                        <S.LinkSignup to="/account/register">가입하기</S.LinkSignup>
                    </S.SignupBox>
                </S.FindPwCard>
            </S.Container>
        </S.FindPwWrapper>
    );
};

export default FindPw;