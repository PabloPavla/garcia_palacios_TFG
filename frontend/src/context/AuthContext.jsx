import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import authService from '../services/authService'

/**
 * Contexto de autenticación global.
 *
 * Proporciona el estado del usuario autenticado y las funciones
 * de login, registro y logout a todos los componentes de la app
 * sin necesidad de prop-drilling.
 */
const AuthContext = createContext(null)

/**
 * Hook personalizado para acceder al contexto de autenticación.
 *
 * @returns {{ user, token, loading, login, register, logout }} el estado y funciones de auth
 * @throws {Error} si se usa fuera de un AuthProvider
 */
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de un AuthProvider')
  }
  return context
}

/**
 * Proveedor del contexto de autenticación.
 *
 * Persiste el token y los datos del usuario en localStorage para
 * mantener la sesión al recargar la página. También configura el
 * token en los headers de Axios al iniciar.
 *
 * @param {{ children: React.ReactNode }} props
 * @returns {JSX.Element}
 */
export function AuthProvider({ children }) {
  const [user,    setUser]    = useState(null)
  const [token,   setToken]   = useState(null)
  const [loading, setLoading] = useState(true)

  /**
   * Al montar el componente, recupera la sesión guardada en localStorage.
   * Si el token existe, restaura el estado del usuario.
   */
  useEffect(() => {
    const savedToken = localStorage.getItem('accessToken')
    const savedUser  = localStorage.getItem('user')

    if (savedToken && savedUser) {
      try {
        const parsedUser = JSON.parse(savedUser)
        setToken(savedToken)
        setUser(parsedUser)
        authService.setAuthHeader(savedToken)
      } catch {
        // Si los datos guardados están corruptos, limpiar
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
      }
    }
    setLoading(false)
  }, [])

  /**
   * Inicia sesión con username y password.
   *
   * Llama al Auth Service, persiste los tokens en localStorage
   * y actualiza el estado global del usuario.
   *
   * @param {string} username nombre de usuario
   * @param {string} password contraseña
   * @returns {Promise<void>}
   */
  const login = useCallback(async (username, password) => {
    const data = await authService.login({ username, password })
    persistSession(data)
  }, [])

  /**
   * Registra un nuevo usuario y lo autentica automáticamente.
   *
   * @param {string} username nombre de usuario
   * @param {string} email correo electrónico
   * @param {string} password contraseña
   * @returns {Promise<void>}
   */
  const register = useCallback(async (username, email, password) => {
    const data = await authService.register({ username, email, password })
    persistSession(data)
  }, [])

  /**
   * Cierra la sesión del usuario actual.
   *
   * Llama al endpoint de logout del backend, limpia el localStorage
   * y resetea el estado global.
   *
   * @returns {Promise<void>}
   */
  const logout = useCallback(async () => {
    try {
      await authService.logout()
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      authService.clearAuthHeader()
      setUser(null)
      setToken(null)
    }
  }, [])

  /**
   * Persiste los datos de sesión en localStorage y actualiza el estado.
   *
   * @param {Object} data respuesta del backend con tokens y datos de usuario
   */
  function persistSession(data) {
    localStorage.setItem('accessToken',  data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify({
      id:       data.userId,
      username: data.username,
      email:    data.email,
      role:     data.role,
      profilePictureUrl: data.profilePictureUrl,
    }))
    authService.setAuthHeader(data.accessToken)
    setToken(data.accessToken)
    setUser({
      id:       data.userId,
      username: data.username,
      email:    data.email,
      role:     data.role,
      profilePictureUrl: data.profilePictureUrl,
    })
  }

  /**
   * Actualiza los datos del usuario en el contexto (sin re-autenticar).
   * Se usa tras actualizar el perfil para refrescar la UI.
   *
   * @param {Object} updatedUser datos actualizados del usuario
   */
  const updateUser = useCallback((updatedUser) => {
    setUser(updatedUser)
    localStorage.setItem('user', JSON.stringify(updatedUser))
  }, [])

  const value = { user, token, loading, login, register, logout, updateUser }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
