import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';

const Layout = () => {
    return (
        <div className="layout-wrapper" style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>

            <Header />

            <main className="layout-content" style={{ flex: 1 }}>
                <Outlet />
            </main>

        </div>
    );
};

export default Layout;