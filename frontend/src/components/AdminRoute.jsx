import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Componente guard para rutas exclusivas de administrador (ROLE_ADMIN).
 *
 * Si el usuario no está autenticado o no tiene rol ROLE_ADMIN,
 * redirige al dashboard. Solo los administradores pueden acceder
 * a las rutas envueltas por este guard.
 *
 * @returns {JSX.Element} el componente hijo o una redirección
 */
function AdminRoute() {
  const { user, loading } = useAuth()

  if (loading) return null

  // Verificar que el usuario tenga rol de administrador
  const isAdmin = user?.role === 'ROLE_ADMIN'

  if (!user) return <Navigate to="/login"    replace />
  if (!isAdmin) return <Navigate to="/dashboard" replace />

  return <Outlet />
}

export default AdminRoute
