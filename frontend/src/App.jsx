import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import PrivateRoute from './components/PrivateRoute'
import AdminRoute from './components/AdminRoute'

// Páginas públicas
import LoginPage    from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'

// Páginas privadas (OWNER)
import DashboardPage  from './pages/DashboardPage'
import MyClubPage     from './pages/MyClubPage'
import MarketPage     from './pages/MarketPage'
import TransfersPage  from './pages/TransfersPage'
import PlayerPage     from './pages/PlayerPage'

// Páginas públicas con datos
import LeaguePage  from './pages/LeaguePage'
import MatchesPage from './pages/MatchesPage'

// Páginas de administrador
import AdminPage from './pages/AdminPage'

/**
 * Componente raíz de la aplicación.
 *
 * Configura el enrutado con React Router v6 y el contexto de autenticación.
 * Las rutas privadas redirigen a /login si el usuario no está autenticado.
 *
 * @returns {JSX.Element} La aplicación con el sistema de rutas configurado
 */
function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* ── Rutas públicas ───────────────────────────── */}
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Redirigir raíz a dashboard o login */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* ── Rutas privadas (requieren autenticación) ─── */}
          <Route element={<PrivateRoute />}>
            <Route path="/dashboard"  element={<DashboardPage />} />
            <Route path="/my-club"    element={<MyClubPage />} />
            <Route path="/market"     element={<MarketPage />} />
            <Route path="/transfers"  element={<TransfersPage />} />
            <Route path="/players/:id" element={<PlayerPage />} />
          </Route>

          {/* ── Rutas públicas con datos ─────────────────── */}
          <Route path="/league"         element={<LeaguePage />} />
          <Route path="/league/matches" element={<MatchesPage />} />

          {/* ── Rutas de administrador ───────────────────── */}
          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<AdminPage />} />
          </Route>

          {/* Ruta 404 – redirige a dashboard */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
