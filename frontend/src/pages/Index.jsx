import React from 'react';
import { Link } from 'react-router-dom';
import Footer from '../components/common/Footer';
import { useIndex } from '../hooks/pages/useIndex';
import * as S from '../style/pages/Index.styles';

const Index = () => {
    const { currentSectionIndex, isMobile, stats, statsRef } = useIndex();

    return (
        <S.ViewportWrapper>
            <S.MainContent $index={currentSectionIndex} $isMobile={isMobile}>

                <S.Section $bg="#f4f7f9">
                    <S.HeroGrid>
                        <S.HeroTextContent>
                            <h1>Build Natural<br />Language<br />Experiences</h1>
                            <p>Enable people to interact with your products<br />using voice and text.</p>
                            <a href="/account/register" className="how-it-works">See How It Works &gt;</a>
                        </S.HeroTextContent>

                        <S.HeroCtaContent>
                            <Link to="/account/login" style={{ textDecoration: 'none' }}>
                                <S.MetaButton>
                                    마음 MAUM 이용 시작하기
                                </S.MetaButton>
                            </Link>
                        </S.HeroCtaContent>
                    </S.HeroGrid>

                    <S.HeroFeatures>
                        <S.FeatureItem>
                            <div className="icon-box"><i className="fa-solid fa-robot"></i></div>
                            <span>챗봇</span>
                        </S.FeatureItem>
                        <S.FeatureItem>
                            <div className="icon-box"><i className="fa-solid fa-bars-staggered"></i></div>
                            <span>일기 작성</span>
                        </S.FeatureItem>
                        <S.FeatureItem>
                            <div className="icon-box"><i className="fa-solid fa-chart-pie"></i></div>
                            <span>AI 감정 분석</span>
                        </S.FeatureItem>
                        <S.FeatureItem>
                            <div className="icon-box"><i className="fa-solid fa-map-location-dot"></i></div>
                            <span>주변 상담소 위치</span>
                        </S.FeatureItem>
                    </S.HeroFeatures>
                </S.Section>

                <S.Section>
                    <S.SplitContainer>
                        <S.SplitPanel as={Link} to="/diary/write">
                            <h4>일기 작성하기</h4>
                        </S.SplitPanel>
                        <S.SplitPanel as={Link} to="/diary/analysis">
                            <h4>분석 결과</h4>
                        </S.SplitPanel>
                        <S.SplitPanel as={Link} to="/diary/list">
                            <h4>일기 보관소</h4>
                        </S.SplitPanel>
                    </S.SplitContainer>
                </S.Section>

                <S.FooterWrapper>
                    <S.StatsSection ref={statsRef}>
                        <S.StatItem>
                            <div className="num">{stats.records.toLocaleString()}</div>
                            <div className="label">기록된 마음</div>
                        </S.StatItem>
                        <S.StatItem>
                            <div className="num">{stats.comforted.toLocaleString()}</div>
                            <div className="label">위로받은 사람</div>
                        </S.StatItem>
                        <S.StatItem>
                            <div className="num">{stats.hours.toLocaleString()}</div>
                            <div className="label">함께한 시간</div>
                        </S.StatItem>
                    </S.StatsSection>
                    <Footer />
                </S.FooterWrapper>

            </S.MainContent>
        </S.ViewportWrapper>
    );
};

export default Index;