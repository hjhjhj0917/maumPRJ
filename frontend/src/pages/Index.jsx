import React from 'react';
import {Link} from 'react-router-dom';
import Footer from '../components/common/Footer';
import {useIndex} from '../hooks/pages/useIndex';
import * as S from '../style/pages/Index.styles';
import mainBg from '../assets/images/main-background.png';

const Index = () => {
    const {currentSectionIndex, isMobile, stats, statsRef} = useIndex();

    return (
        <S.ViewportWrapper>
            <S.MainContent $index={currentSectionIndex} $isMobile={isMobile}>

                <S.Section>
                    <S.HeroBackground $src={mainBg}/>
                    <S.HeroNav>
                        <S.Logo>wit.ai</S.Logo>
                        <S.NavLinks>
                            <a href="#none">Get Started</a>
                            <a href="#none">Docs</a>
                            <a href="#none">Blog</a>
                            <a href="#none">Ecosystem</a>
                            <a href="#none">FAQ</a>
                            <a href="#none">Jobs</a>
                            <Link to="/account/login">Login</Link>
                        </S.NavLinks>
                    </S.HeroNav>

                    <S.HeroGrid>
                        <S.HeroTextContent>
                            <h1>수많은 소음 속,<br/>온전히 나에게 집중하는 시간</h1>
                            <p>
                                누구에게도 말 못한 고민이 있다면 MAUM에 털어놓으세요.
                                <br/>AI가 당신의 하루를 듣고 따듯한 위로와 분석을 건넵니다.
                            </p>
                            <a href="/account/register" className="sign-in">마음(MAÜM) 시작하기&gt;</a>
                        </S.HeroTextContent>
                    </S.HeroGrid>

                    <S.HeroFeatures>
                        <S.FeatureItem>
                            <div className="icon-box"><i className="fa-solid fa-robot"></i></div>
                            <span>ChatBots</span>
                        </S.FeatureItem>
                        <S.FeatureItem>
                            <div className="icon-box"><i className="fa-solid fa-book"></i></div>
                            <span>Diary</span>
                        </S.FeatureItem>
                        <S.FeatureItem>
                            <div className="icon-box"><i className="fa-solid fa-brain"></i></div>
                            <span>Analysis</span>
                        </S.FeatureItem>
                        <S.FeatureItem>
                            <div className="icon-box"><i className="fa-solid fa-map"></i></div>
                            <span>Locations</span>
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
                    <Footer/>
                </S.FooterWrapper>

            </S.MainContent>
        </S.ViewportWrapper>
    );
};

export default Index;