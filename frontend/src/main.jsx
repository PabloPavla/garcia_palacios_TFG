import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import './index.css'
import App from './App.jsx'

/**
 * Punto de entrada principal del frontend React.
 *
 * Importa Bootstrap 5 CSS y Bootstrap Icons antes que el CSS propio
 * para permitir que los estilos personalizados puedan sobrescribir los de Bootstrap.
 */
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
