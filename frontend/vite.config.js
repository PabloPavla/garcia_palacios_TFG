import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * Configuración de Vite para el frontend React.
 *
 * - Proxy: redirige /api al API Gateway (puerto 8080) para evitar CORS en desarrollo
 * - Puerto: 5173 (por defecto de Vite)
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Redirige todas las llamadas /api/* al API Gateway en desarrollo
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
