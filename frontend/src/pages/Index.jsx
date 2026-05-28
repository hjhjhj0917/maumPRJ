import React from 'react';
import { Link } from 'react-router-dom';
import { useIndex, useDraggable } from '../hooks/useIndex.js';
import * as S from '../style/pages/Index.styles';
import logoImg from '../assets/images/includes/logo.png';

const Index = () => {
    const { scrolled, stats, statsRef1, statsRef2 } = useIndex();

    const emotionData = [
        { label: "무감정", color: "#9E9E9E", x: -620, y: -240 },
        { label: "신뢰", color: "#66CDAA", x: -480, y: 200 },
        { label: "혐오", color: "#556B2F", x: -750, y: 0 },
        { label: "공포", color: "#4B0082", x: -380, y: -330 },
        { label: "슬픔", color: "#1E3A8A", x: -550, y: 270 },
        { label: "놀람", color: "#00BFFF", x: 580, y: -290 },
        { label: "기대", color: "#FFA500", x: 420, y: 110 },
        { label: "기쁨", color: "#FFD700", x: 700, y: -130 },
        { label: "분노", color: "#FF3B30", x: 630, y: 240 },
    ];

    const draggables = emotionData.map(data => useDraggable(data.x, data.y));

    return (
        <S.PageWrapper>
            <S.Header $scrolled={scrolled}>
                <S.Logo>
                    <img src={logoImg} alt="MAUM" />MauM
                </S.Logo>
                <S.NavLinks>
                    <Link to="#features">Features</Link>
                    <Link to="#technology">Technology</Link>
                    <Link to="#about">About</Link>
                </S.NavLinks>
                <S.AuthButtons>
                    <Link to="/account/login">
                        <S.Button>Sign in</S.Button>
                    </Link>
                </S.AuthButtons>
            </S.Header>

            <S.HeroSection>
                <S.HeroContent>
                    <S.Title>감정을 기록하는<br />AI 마음 연구소, MauM</S.Title>
                    <S.Subtitle>
                        HyperCLOVA X와 감정 분석 모델을 통해 당신의 일상 속에 숨겨진 감정을 발견하세요.
                        AI와의 대화를 통해 지친 마음을 다독이고 내일을 위한 힘을 <br />얻어 보세요.
                    </S.Subtitle>
                    <Link to="/account/login">
                        <S.Button $primary style={{ padding: '16px 32px', fontSize: '18px' }}>
                            지금 시작하기
                        </S.Button>
                    </Link>

                    <S.LogoTicker>
                        <i className="fa-brands fa-java" title="Java"></i>
                        <i className="fa-brands fa-python" title="Python"></i>
                        <i className="fa-brands fa-react" title="React"></i>
                        <i className="fa-brands fa-github" title="GitHub"></i>
                        <i className="fa-brands fa-js"></i>
                    </S.LogoTicker>
                </S.HeroContent>

                <S.InteractiveCanvas>
                    {draggables.map((draggable, index) => (
                        <S.DraggableItem key={index} {...draggable.dragProps}>
                            <S.FloatingElement
                                $isDragging={draggable.isDragging}
                                $duration={`${5 + (index % 3)}s`}
                                $delay={`${index * 0.2}s`}
                            >
                                <S.HeroPill
                                    $color={emotionData[index].color}
                                    $isDark={['#4B0082', '#1E3A8A', '#556B2F'].includes(emotionData[index].color)}
                                >
                                    #{emotionData[index].label}
                                </S.HeroPill>
                            </S.FloatingElement>
                        </S.DraggableItem>
                    ))}
                </S.InteractiveCanvas>
            </S.HeroSection>

            <S.Section id="features">
                <S.SectionHeader>
                    <S.SectionTitle>당신의 마음을 돌보는 기술</S.SectionTitle>
                    <S.Subtitle style={{ margin: '0 auto' }}>
                        단순한 기록을 넘어, MauM은 첨단 AI 기술을 활용하여 당신의 정서적 안정을 돕는 다양한 기능을 제공합니다.
                    </S.Subtitle>
                </S.SectionHeader>

                <S.Grid>
                    <S.Card>
                        <h3>AI 감정 분석</h3>
                        <p>KOTE 모델을 활용하여 44가지의 세분화된 감정 수치를 분석하고 시각화합니다.</p>
                    </S.Card>
                    <S.Card>
                        <h3>서술형 일기 요약</h3>
                        <p>HyperCLOVA X(HCX-007)가 긴 일기를 따뜻한 문체로 요약하여 기록해줍니다.</p>
                    </S.Card>
                    <S.Card>
                        <h3>RAG 기반 AI 챗봇</h3>
                        <p>과거의 기록을 기억하고 공감하는 당신만을 위한 개인화 상담사와 대화하세요.</p>
                    </S.Card>
                    <S.Card>
                        <h3>정서적 트래킹</h3>
                        <p>우울증 수치 변화와 감정 컬러 캘린더를 통해 마음의 변화를 한눈에 확인합니다.</p>
                    </S.Card>
                    <S.Card>
                        <h3>안전한 보안 세션</h3>
                        <p>JWT와 Redis를 활용한 철저한 이중 보안으로 당신의 소중한 기록을 보호합니다.</p>
                    </S.Card>
                    <S.Card>
                        <h3>지능형 벡터 검색</h3>
                        <p>MongoDB Atlas Vector Search를 통해 문맥에 맞는 대화와 검색을 지원합니다.</p>
                    </S.Card>
                </S.Grid>
            </S.Section>

            <S.Section $center ref={statsRef1}>
                <S.SectionTitle>매일 성장하는<br />MAUM 데이터</S.SectionTitle>
                <S.StatsGrid>
                    <S.StatBox>
                        <div className="number">{stats.lessons}+</div>
                        <div className="label">기록된 일기</div>
                    </S.StatBox>
                    <S.StatBox>
                        <div className="number">{stats.workshops}+</div>
                        <div className="label">분석된 감정</div>
                    </S.StatBox>
                    <S.StatBox>
                        <div className="number">{stats.challenges}+</div>
                        <div className="label">위로의 대화</div>
                    </S.StatBox>
                </S.StatsGrid>

                <S.SectionTitle style={{ fontSize: '32px', marginTop: '60px' }}>오늘 당신의 마음은 어떤가요?</S.SectionTitle>
                <S.Subtitle style={{ margin: '0 auto' }}>이메일을 등록하시면 MauM의 새로운 소식과 마음 건강 가이드를 보내드립니다.</S.Subtitle>
                <S.EmailForm>
                    <input type="email" placeholder="이메일 주소" />
                    <button>구독하기</button>
                </S.EmailForm>
            </S.Section>

            <S.Section $center id="technology" ref={statsRef2}>
                <S.SectionTitle style={{ fontSize: '64px' }}>함께 나누는<br />마음 공동체</S.SectionTitle>
                <S.CommunityGrid>
                    당신의 감정을 컬러로 공유하고 서로 응원하세요.
                </S.CommunityGrid>

                <S.StatsGrid>
                    <S.StatBox>
                        <div className="number">{stats.friends >= 15000 ? '15k' : stats.friends}</div>
                        <div className="label">전체 사용자</div>
                    </S.StatBox>
                    <S.StatBox>
                        <div className="number">{stats.members}+</div>
                        <div className="label">오늘의 기록</div>
                    </S.StatBox>
                    <S.StatBox>
                        <div className="number">{stats.nationalities}+</div>
                        <div className="label">공감의 수</div>
                    </S.StatBox>
                </S.StatsGrid>
            </S.Section>

            <S.Section>
                <S.SectionHeader>
                    <S.SectionTitle>MauM은 끊임없이<br />연구합니다.</S.SectionTitle>
                </S.SectionHeader>

                <S.BentoGrid>
                    <S.Card>
                        <h3>자연어 처리 연구</h3>
                        <p>한국어 감정 표현에 최적화된 KoElectra 및 텍스트 마이닝 기술을 적용합니다.</p>
                    </S.Card>
                    <S.Card>
                        <h3>LLM 최적화</h3>
                        <p>HyperCLOVA X 임베딩을 활용하여 더 깊은 문맥적 공감을 실현합니다.</p>
                    </S.Card>
                    <S.Card className="large">
                        <h3>데이터 분석 기반 케어</h3>
                        <p>누적된 심리 데이터를 기반으로 사용자에게 가장 필요한 위로의 방식을 제안합니다.</p>
                    </S.Card>
                </S.BentoGrid>
            </S.Section>

            <S.Footer>
                <S.FooterGrid>
                    <div>
                        <S.Logo style={{ marginBottom: '20px' }}>
                            <img src={logoImg} alt="MAUM" />MauM
                        </S.Logo>
                        <p>당신의 마음을 위한 AI 파트너</p>
                    </div>
                    <div>
                        <h4>Service</h4>
                        <ul>
                            <li><Link to="#">감정 일기</Link></li>
                            <li><Link to="#">AI 챗봇</Link></li>
                            <li><Link to="#">마음 리포트</Link></li>
                        </ul>
                    </div>
                    <div>
                        <h4>Technology</h4>
                        <ul>
                            <li><Link to="#">HyperCLOVA X</Link></li>
                            <li><Link to="#">NLP Models</Link></li>
                            <li><Link to="#">RAG Architecture</Link></li>
                        </ul>
                    </div>
                    {/*<div>*/}
                    {/*    <h4>Support</h4>*/}
                    {/*    <ul>*/}
                    {/*        <li><Link to="#">이용약관</Link></li>*/}
                    {/*        <li><Link to="#">개인정보 처리방침</Link></li>*/}
                    {/*        <li><Link to="#">문의하기</Link></li>*/}
                    {/*    </ul>*/}
                    {/*</div>*/}
                </S.FooterGrid>
            </S.Footer>
        </S.PageWrapper>
    );
};

export default Index;