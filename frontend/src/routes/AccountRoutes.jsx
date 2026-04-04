import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from '../pages/Account/Login';
import FindId from '../pages/Account/FindId';
import Register from '../pages/Account/Register';
import Profile from '../pages/Account/Profile';

const AccountRoutes = () => {
    return (
        <Routes>
            <Route path="login" element={<Login />} />
            <Route path="findId" element={<FindId />} />
            <Route path="register" element={<Register />} />
            <Route path="profile" element={<Profile />} />
        </Routes>
    );
};

export default AccountRoutes;