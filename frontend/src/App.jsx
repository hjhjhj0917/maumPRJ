import { BrowserRouter, Routes, Route } from 'react-router-dom';
import {GlobalStyle} from './style/GlobalStyle';
import HeaderLayout from './components/layout/HeaderLayout';
import Layout from './components/layout/Layout';
import Index from './pages/Index';
import DiaryWrite from './pages/Diary/DiaryWrite';
import AccountRoutes from './routes/AccountRoutes';
import DiaryList from "./pages/Diary/DiaryList.jsx";
import DiaryDetail from "./pages/Diary/DiaryDetail.jsx";
import ChatBot from "./pages/ChatBot/ChatBot.jsx";
import Map from "./pages/Map/Map.jsx";
import Profile from "./pages/Account/Profile.jsx";

function App() {
    return (
        <BrowserRouter>
            <GlobalStyle />
            <Routes>
                <Route path="/" element={<Index />} />

                {/* Header */}
                <Route element={<HeaderLayout />}>
                    <Route path="/account/*" element={<AccountRoutes />} />
                </Route>

                {/* Header + Sidebar */}
                <Route element={<Layout />}>
                    <Route path="/diary/write" element={<DiaryWrite />} />
                    <Route path="/diary/list" element={<DiaryList />} />
                    <Route path="/chatbot" element={<ChatBot />} />
                    <Route path="/account/profile" element={<Profile />}/>
                    <Route path="/map" element={<Map />} />
                    <Route path="/diary/:diaryNo" element={<DiaryDetail />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;