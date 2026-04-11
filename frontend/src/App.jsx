import { BrowserRouter, Routes, Route } from 'react-router-dom';
import {GlobalStyle} from './style/GlobalStyle';
import HeaderLayout from './components/layout/HeaderLayout';
import Layout from './components/layout/Layout';
import Index from './pages/Index';
import DiaryWrite from './pages/Diary/DiaryWrite';
import AccountRoutes from './routes/AccountRoutes';

function App() {
    return (
        <BrowserRouter>
            <GlobalStyle />
            <Routes>
                {/* 그룹 1: 사이드바 없이 [헤더만] 필요한 페이지들 */}
                <Route element={<HeaderLayout />}>
                    <Route path="/" element={<Index />} />
                    <Route path="/account/*" element={<AccountRoutes />} />
                </Route>

                {/* 그룹 2: [헤더 + 사이드바]가 모두 필요한 서비스 내부 페이지들 */}
                <Route element={<Layout />}>
                    <Route path="/diary/write" element={<DiaryWrite />} />
                    {/* 추가될 일기 목록, 상담소 등... */}
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;