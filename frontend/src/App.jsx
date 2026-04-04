import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Header from './components/layout/Header.jsx'; // 기존에 만든 헤더

function App() {
  return (
      <Router>
        <Header />
        <Routes>
          <Route path="/" element={<Home />} />
          {/* 다른 페이지들도 여기에 추가 */}
        </Routes>
      </Router>
  );
}

export default App;