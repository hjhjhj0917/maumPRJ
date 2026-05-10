import React from 'react';
import { Link } from 'react-router-dom';
import { useIndex, useDraggable } from '../hooks/useIndex.js';
import * as S from '../style/pages/Index.styles';
import logoImg from '../assets/images/includes/logo.png';

const Index = () => {
    const { scrolled, stats, statsRef1, statsRef2 } = useIndex();

    const dragPill1 = useDraggable(-400, -150);
    const dragPill2 = useDraggable(-350, 100);
    const dragPill3 = useDraggable(350, -50);

    return (
        <S.PageWrapper>
            <S.Header $scrolled={scrolled}>
                <S.Logo>
                    <img src={logoImg} alt="MAUM" />MauM
                </S.Logo>
                <S.NavLinks>
                    <Link to="#skills">Skills</Link>
                    <Link to="#members">Members</Link>
                    <Link to="#plans">Plans</Link>
                </S.NavLinks>
                <S.AuthButtons>
                    <Link to="/account/login">
                        <S.Button>Sign in</S.Button>
                    </Link>
                </S.AuthButtons>
            </S.Header>

            <S.HeroSection>
                <S.InteractiveCanvas>
                    <S.DraggableItem {...dragPill1}>
                        <S.FloatingElement $isDragging={dragPill1.isDragging} $duration="5s" $delay="0s">
                            <S.HeroPill $color="#4ade80" style={{ transform: 'rotate(-10deg)' }}>Stephanie</S.HeroPill>
                        </S.FloatingElement>
                    </S.DraggableItem>

                    <S.DraggableItem {...dragPill2}>
                        <S.FloatingElement $isDragging={dragPill2.isDragging} $duration="6s" $delay="1s">
                            <S.HeroPill $color="#60a5fa" style={{ transform: 'rotate(5deg)' }}>Ahmed</S.HeroPill>
                        </S.FloatingElement>
                    </S.DraggableItem>

                    <S.DraggableItem {...dragPill3}>
                        <S.FloatingElement $isDragging={dragPill3.isDragging} $duration="5.5s" $delay="0.5s">
                            <S.HeroPill $color="#facc15" style={{ transform: 'rotate(15deg)' }}>Mike</S.HeroPill>
                        </S.FloatingElement>
                    </S.DraggableItem>
                </S.InteractiveCanvas>

                <S.HeroContent>
                    <S.Title>Become a<br />10x Designer</S.Title>
                    <S.Subtitle>
                        Expand your design skillset through live workshops and async lessons, connect with like-minded individuals and have direct access to industry professionals.
                    </S.Subtitle>
                    <S.Button $primary style={{ padding: '16px 32px', fontSize: '18px' }}>
                        Start your journey
                    </S.Button>

                    <S.LogoTicker>
                        <i className="fa-brands fa-apple"></i>
                        <i className="fa-brands fa-google"></i>
                        <i className="fa-brands fa-figma"></i>
                        <i className="fa-brands fa-spotify"></i>
                        <i className="fa-brands fa-stripe"></i>
                    </S.LogoTicker>
                </S.HeroContent>
            </S.HeroSection>

            <S.Section id="skills">
                <S.SectionHeader>
                    <S.SectionTitle>Expand your skillset</S.SectionTitle>
                    <S.Subtitle style={{ margin: '0 auto' }}>
                        We're here for those who want to keep growing. Currently you can jump into 5 skills, while we're actively working on adding more
                    </S.Subtitle>
                </S.SectionHeader>

                <S.Grid>
                    <S.Card>
                        <h3>Visual Design</h3>
                        <p>Improve your visual skills through hands-on lessons and exercises.</p>
                    </S.Card>
                    <S.Card>
                        <h3>Web Design</h3>
                        <p>Learn how to design stunning, high converting web pages.</p>
                    </S.Card>
                    <S.Card>
                        <h3>Product Design</h3>
                        <p>Learn product design from research to UI design and prototyping.</p>
                    </S.Card>
                    <S.Card>
                        <h3>Brand Design</h3>
                        <p>Learn how to create high quality brand languages that stand out.</p>
                    </S.Card>
                    <S.Card>
                        <h3>Career & Freelance</h3>
                        <p>Learn how to present, handle feedback, brand yourself and more.</p>
                    </S.Card>
                    <S.Card>
                        <h3>Prompt Engineering</h3>
                        <p>Elevate your designs with top notch graphics and imagery.</p>
                    </S.Card>
                </S.Grid>
            </S.Section>

            <S.Section $center ref={statsRef1}>
                <S.SectionTitle>Unlock hours of<br />design education</S.SectionTitle>
                <S.StatsGrid>
                    <S.StatBox>
                        <div className="number">{stats.lessons}+</div>
                        <div className="label">Lessons</div>
                    </S.StatBox>
                    <S.StatBox>
                        <div className="number">{stats.workshops}+</div>
                        <div className="label">Workshops</div>
                    </S.StatBox>
                    <S.StatBox>
                        <div className="number">{stats.challenges}+</div>
                        <div className="label">Challenges</div>
                    </S.StatBox>
                </S.StatsGrid>

                <S.SectionTitle style={{ fontSize: '32px', marginTop: '60px' }}>Try a lesson</S.SectionTitle>
                <S.Subtitle style={{ margin: '0 auto' }}>Drop your email below and we'll send you one video lesson for free.</S.Subtitle>
                <S.EmailForm>
                    <input type="email" placeholder="Email" />
                    <button>Send preview</button>
                </S.EmailForm>
            </S.Section>

            <S.Section $center id="members" ref={statsRef2}>
                <S.SectionTitle style={{ fontSize: '64px' }}>Become part of our<br />Community</S.SectionTitle>
                <S.CommunityGrid>
                    Member Avatars Grid Area
                </S.CommunityGrid>

                <S.StatsGrid>
                    <S.StatBox>
                        <div className="number">{stats.friends >= 15000 ? '15k' : stats.friends}</div>
                        <div className="label">Friends</div>
                    </S.StatBox>
                    <S.StatBox>
                        <div className="number">{stats.members}+</div>
                        <div className="label">Members</div>
                    </S.StatBox>
                    <S.StatBox>
                        <div className="number">{stats.nationalities}+</div>
                        <div className="label">Nationalities</div>
                    </S.StatBox>
                </S.StatsGrid>
            </S.Section>

            <S.Section>
                <S.SectionHeader>
                    <S.SectionTitle>And there's more,<br />A lot more.</S.SectionTitle>
                </S.SectionHeader>

                <S.BentoGrid>
                    <S.Card>
                        <h3>Live workshops</h3>
                        <p>Tune in every Friday for a live workshop hosted by our mentors and guests.</p>
                    </S.Card>
                    <S.Card>
                        <h3>Design Crits</h3>
                        <p>Weekly design feedback on shared design work in the community.</p>
                    </S.Card>
                    <S.Card className="large">
                        <h3>Lessons</h3>
                        <p>A wide variety of hands-on lessons and exercises by our mentors.</p>
                    </S.Card>
                </S.BentoGrid>
            </S.Section>

            <S.Footer>
                <S.FooterGrid>
                    <div>
                        <S.Logo style={{ marginBottom: '20px' }}>
                            <img src={logoImg} alt="MAUM" />
                        </S.Logo>
                    </div>
                    <div>
                        <h4>Company</h4>
                        <ul>
                            <li><Link to="#">Our story</Link></li>
                            <li><Link to="#">Support</Link></li>
                            <li><Link to="#">Press</Link></li>
                        </ul>
                    </div>
                    <div>
                        <h4>Legal</h4>
                        <ul>
                            <li><Link to="#">Privacy policy</Link></li>
                            <li><Link to="#">Refund policy</Link></li>
                            <li><Link to="#">Community rules</Link></li>
                        </ul>
                    </div>
                    <div>
                        <h4>Let's work together</h4>
                        <ul>
                            <li><Link to="#">Sponsor a member deal</Link></li>
                            <li><Link to="#">Apply as a mentor</Link></li>
                            <li><Link to="#">Host a workshop</Link></li>
                        </ul>
                    </div>
                </S.FooterGrid>
            </S.Footer>
        </S.PageWrapper>
    );
};

export default Index;