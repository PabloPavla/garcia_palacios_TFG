import axios from 'axios'

/**
 * Instancia de Axios configurada para comunicarse con el API Gateway.
 *
 * En desarrollo, las peticiones van a /api/* y Vite las redirige
 * al API Gateway en localhost:8080. En producción, se usa la URL
 * completa del gateway configurada en la variable de entorno.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * Interceptor de solicitud: agrega el token Bearer a cada solicitud.
 * 
 * Garantiza que el token siempre se envíe en los headers de autenticación,
 * incluso si se ha actualizado o cambiado en localStorage.
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * Interceptor de respuesta: gestiona la expiración del token JWT.
 *
 * Si el servidor devuelve 401 Unauthorized y hay un refresh token
 * disponible, intenta renovar el access token automáticamente y
 * reintenta la petición original.
 */
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Si es 401 y aún no hemos reintentado la petición
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          // Intentar renovar el token
          const { data } = await axios.post('/api/auth/refresh', { refreshToken })
          localStorage.setItem('accessToken', data.accessToken)
          localStorage.setItem('refreshToken', data.refreshToken)
          api.defaults.headers.common['Authorization'] = `Bearer ${data.accessToken}`
          originalRequest.headers['Authorization'] = `Bearer ${data.accessToken}`
          return api(originalRequest)
        } catch {
          // Si el refresh falla, limpiar sesión y redirigir a login
          localStorage.clear()
          window.location.href = '/login'
        }
      }
    }

    return Promise.reject(error)
  }
)

export default api
