import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Spinner, Container } from 'react-bootstrap'

/**
 * Componente guard para rutas privadas que requieren autenticación.
 *
 * Si el usuario no está autenticado, redirige a /login.
 * Mientras se comprueba el estado de autenticación (loading),
 * muestra un spinner de carga centrado.
 *
 * Uso en el router:
 * @example
 * <Route element={<PrivateRoute />}>
 *   <Route path="/dashboard" element={<DashboardPage />} />
 * </Route>
 *
 * @returns {JSX.Element} el componente hijo o una redirección a /login
 */
function PrivateRoute() {
  const { user, loading } = useAuth()

  // Mostrar spinner mientras se recupera la sesión de localStorage
  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center min-vh-100">
        <Spinner animation="border" variant="primary" role="status">
          <span className="visually-hidden">Cargando...</span>
        </Spinner>
      </Container>
    )
  }

  // Si no hay usuario autenticado, redirigir a login
  return user ? <Outlet /> : <Navigate to="/login" replace />
}

export default PrivateRoute
