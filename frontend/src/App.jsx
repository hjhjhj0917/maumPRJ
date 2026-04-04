import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/common/Layout';
import Index from './pages/Index';
import Main from './pages/Main';
import AccountRoutes from './routes/AccountRoutes';

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* 1. 헤더가 없는 단독 페이지 */}

                {/* 2. 공통 헤더(Layout)가 적용되는 페이지 그룹 */}
                <Route element={<Layout />}>
                    <Route path="/" element={<Index />} />
                    <Route path="/main" element={<Main />} />

                    {/* /account로 시작하는 모든 주소는 AccountRoutes로 넘김 */}
                    <Route path="/account/*" element={<AccountRoutes />} />

                    {/* 추후 추가될 라우터들 */}
                    {/* <Route path="/diary/*" element={<DiaryRoutes />} /> */}
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;