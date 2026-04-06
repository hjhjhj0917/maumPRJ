import React from 'react';
import { Link } from 'react-router-dom';
import InputField from '../../components/common/InputField';
import CustomModal from '../../components/common/CustomModal';
import { useFindIdForm } from '../../hooks/account/useFindIdForm';
import * as S from '../../style/pages/Account/FindId.styles';

const FindId = () => {
    const {
        step,
        formData,
        handleChange,
        handleOtpChange,
        handleKeyDown,
        handlePaste,
        messages,
        foundId,
        modal,
        inputRefs,
        handleModalConfirm,
        handleModalCancel,
        handleStep1Submit,
        handleStep2Submit,
        handleResend,
        navigate
    } = useFindIdForm();

    const steps = [
        { num: 1, label: '정보입력' },
        { num: 2, label: '인증' },
        { num: 3, label: '결과확인' }
    ];

    return (
        <S.FindIdWrapper>
            <CustomModal
                isOpen={modal.show}
                title={modal.title}
                message={modal.message}
                isConfirm={false}
                onCancel={handleModalCancel}
                onConfirm={handleModalConfirm}
            />

            <S.Container>
                <S.FindIdCard>
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
                                <form onSubmit={handleStep1Submit}>
                                    <S.FormStepFieldWrapper $hasMessage={!!messages.userEmailMsg || !!messages.userNameMsg}>
                                        <InputField label="E-mail" name="userEmail" value={formData.userEmail}
                                                    onChange={handleChange} errorMsg={messages.userEmailMsg}
                                                    placeholder="이메일을 입력하세요." />
                                        <InputField label="Name" name="userName" value={formData.userName}
                                                    onChange={handleChange} errorMsg={messages.userNameMsg}
                                                    placeholder="이름을 입력하세요." />
                                    </S.FormStepFieldWrapper>
                                    <S.BtnConfirm type="submit">확인</S.BtnConfirm>
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
                                    <S.BtnConfirm type="submit">확인</S.BtnConfirm>
                                </form>
                            </S.FormStep>

                            <S.FormStep $active={step === 3}>
                                <S.FadeInResult>
                                    <S.CheckCircle>
                                        <i className="fa-solid fa-check"></i>
                                    </S.CheckCircle>
                                    <S.ResultText>
                                        {formData.userName}님의 아이디는<br />
                                        <S.HighlightId>{foundId}</S.HighlightId> 입니다.
                                    </S.ResultText>
                                    <S.BtnConfirm type="button" onClick={() => navigate('/account/login')}>
                                        확인
                                    </S.BtnConfirm>
                                </S.FadeInResult>
                            </S.FormStep>
                        </S.SlideTrack>
                    </S.SlideViewport>

                    <S.AuthLinks>
                        <Link to="/account/login">로그인</Link>
                        <S.Separator>|</S.Separator>
                        <Link to="/account/find-pw">비밀번호 찾기</Link>
                    </S.AuthLinks>

                    <S.SignupBox>
                        아직 회원이 아니시라면, 지금 바로 마음을 시작해 보세요.
                        <S.LinkSignup to="/account/register">가입하기</S.LinkSignup>
                    </S.SignupBox>
                </S.FindIdCard>
            </S.Container>
        </S.FindIdWrapper>
    );
};

export default FindId;