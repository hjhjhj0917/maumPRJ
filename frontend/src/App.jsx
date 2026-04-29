import { BrowserRouter, Routes, Route } from 'react-router-dom';
import {GlobalStyle} from './style/GlobalStyle';
import HeaderLayout from './components/layout/HeaderLayout';
import Layout from './components/layout/Layout';
import Index from './pages/Index';
import DiaryWrite from './pages/Diary/DiaryWrite';
import AccountRoutes from './routes/AccountRoutes';
import DiaryList from "./pages/Diary/DiaryList.jsx";
// 1. 상세 페이지 컴포넌트를 임포트합니다.
import DiaryDetail from "./pages/Diary/DiaryDetail.jsx";

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
                    <Route path="/diary/list" element={<DiaryList />} />

                    {/* 2. 상세 조회 라우트를 추가합니다. :diaryNo는 파라미터입니다. */}
                    <Route path="/diary/:diaryNo" element={<DiaryDetail />} />

                    {/* 추가될 상담소 등... */}
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;