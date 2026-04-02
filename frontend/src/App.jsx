import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Index from './pages/Index';
import Login from './pages/Account/Login';

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* 메인 랜딩 페이지 (index.html 역할) */}
                <Route path="/" element={<Index />} />

                {/* 로그인 페이지 */}
                <Route path="/account/login" element={<Login />} />

                {/* 추후 페이지가 추가될 때마다 아래에 Route를 작성하면 됩니다. */}
                {/* <Route path="/diary/write" element={<DiaryWrite />} /> */}
            </Routes>
        </BrowserRouter>
    );
}

export default App;