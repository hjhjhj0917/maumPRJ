import React from 'react';
import { Link } from 'react-router-dom';

const SplitPanelSection = ({ isActive }) => {
    return (
        <section className={`hero-section ${isActive ? 'active' : ''}`}>
            <div className="split-container">
                <Link to="/diary/write" className="split-panel">
                    <div className="panel-overlay"></div>
                    <div className="panel-content">
                        <h4>일기 작성하기</h4>
                    </div>
                </Link>
                <Link to="/diary/analysis" className="split-panel">
                    <div className="panel-overlay"></div>
                    <div className="panel-content">
                        <h4>분석 결과</h4>
                    </div>
                </Link>
                <Link to="/diary/list" className="split-panel">
                    <div className="panel-overlay"></div>
                    <div className="panel-content">
                        <h4>일기 보관소</h4>
                    </div>
                </Link>
            </div>
        </section>
    );
};

export default SplitPanelSection;