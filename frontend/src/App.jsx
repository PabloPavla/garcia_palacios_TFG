import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import AdminRoute from './components/AdminRoute';
import Navigation from './components/Navbar';

// Páginas
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import MyClubPage from './pages/MyClubPage';
import MarketPage from './pages/MarketPage';
import TransfersPage from './pages/TransfersPage';
import LeaguePage from './pages/LeaguePage';
import MatchesPage from './pages/MatchesPage';
import AdminPage from './pages/AdminPage';
import PlayerPage from './pages/PlayerPage';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Navigation />
        <div style={{ paddingTop: '80px', paddingBottom: '40px' }} className="container-fluid min-vh-100">
          <Routes>
            {/* Rutas Públicas */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/league" element={<LeaguePage />} />
            <Route path="/league/matches" element={<MatchesPage />} />
            <Route path="/players/:id" element={<PlayerPage />} />

            {/* Redirigir raíz a dashboard o login */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            {/* Rutas Privadas (Requieren login) */}
            <Route element={<PrivateRoute />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/my-club" element={<MyClubPage />} />
              <Route path="/market" element={<MarketPage />} />
              <Route path="/transfers" element={<TransfersPage />} />
            </Route>

            {/* Rutas de Admin */}
            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<AdminPage />} />
            </Route>

            {/* Ruta 404 */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;
