import { useEffect, type ReactNode } from 'react'
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import Layout from './components/Layout'
import { currentToken, isUsable } from './auth'
import { UNAUTHORIZED_EVENT } from './api/client'
import Acceso from './pages/Acceso'
import Resumen from './pages/Resumen'
import Lotes from './pages/Lotes'
import LoteDetalle from './pages/LoteDetalle'
import Alertas from './pages/Alertas'
import Productos from './pages/Productos'
import Proveedores from './pages/Proveedores'

function RequireToken({ children }: { children: ReactNode }) {
  const location = useLocation()
  if (!isUsable(currentToken())) {
    return <Navigate to="/acceso" replace state={{ from: location.pathname }} />
  }
  return children
}

export default function App() {
  const navigate = useNavigate()
  const qc = useQueryClient()

  // Si cualquier petición recibe 401, se vuelve a la pantalla de acceso.
  useEffect(() => {
    const onUnauthorized = () => {
      qc.clear()
      navigate('/acceso', { replace: true, state: { expired: true } })
    }
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
  }, [navigate, qc])

  return (
    <Routes>
      <Route path="/acceso" element={<Acceso />} />
      <Route
        element={
          <RequireToken>
            <Layout />
          </RequireToken>
        }
      >
        <Route index element={<Resumen />} />
        <Route path="lotes" element={<Lotes />} />
        <Route path="lotes/:id" element={<LoteDetalle />} />
        <Route path="alertas" element={<Alertas />} />
        <Route path="productos" element={<Productos />} />
        <Route path="proveedores" element={<Proveedores />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
