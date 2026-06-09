import api from './api'

/**
 * Servicio de autenticación del frontend.
 *
 * Centraliza todas las llamadas HTTP al Auth Service a través del
 * API Gateway. Gestiona también la configuración de headers de Axios.
 */
const authService = {

  /**
   * Registra un nuevo usuario en el sistema.
   *
   * @param {{ username: string, email: string, password: string }} data datos de registro
   * @returns {Promise<Object>} respuesta con accessToken, refreshToken y datos del usuario
   */
  register: async (data) => {
    const response = await api.post('/auth/register', data)
    return response.data
  },

  /**
   * Autentica al usuario con sus credenciales.
   *
   * @param {{ username: string, password: string }} credentials credenciales de login
   * @returns {Promise<Object>} respuesta con accessToken, refreshToken y datos del usuario
   */
  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials)
    return response.data
  },

  /**
   * Renueva el access token usando el refresh token almacenado.
   *
   * @param {string} refreshToken el refresh token UUID
   * @returns {Promise<Object>} nuevo par de tokens
   */
  refreshToken: async (refreshToken) => {
    const response = await api.post('/auth/refresh', { refreshToken })
    return response.data
  },

  /**
   * Cierra la sesión del usuario en el backend.
   *
   * @returns {Promise<void>}
   */
  logout: async () => {
    await api.post('/auth/logout')
  },

  /**
   * Configura el token Bearer en los headers por defecto de Axios.
   * Se llama tras login/registro para que todas las peticiones posteriores
   * incluyan el token automáticamente.
   *
   * @param {string} token el access token JWT
   */
  setAuthHeader: (token) => {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`
  },

  /**
   * Elimina el header de autorización de Axios.
   * Se llama al hacer logout.
   */
  clearAuthHeader: () => {
    delete api.defaults.headers.common['Authorization']
  },

  getAllUsers: async () => {
    const response = await api.get('/auth/users')
    return response.data
  },

  deleteUser: async (userId) => {
    const response = await api.delete(`/auth/users/${userId}`)
    return response.data
  },

  createUserByAdmin: async (userData) => {
    const response = await api.post('/auth/users', userData)
    return response.data
  },
}

export default authService
