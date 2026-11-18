import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { clearToken, currentToken, readClaims } from '../auth'
import { useAlertas } from '../api/hooks'

const nav = [
  { to: '/', label: 'Resumen', end: true, icon: 'M3 10.5 12 3l9 7.5V21h-6v-6H9v6H3z' },
  { to: '/lotes', label: 'Lotes', icon: 'M4 5h16M4 12h16M4 19h16M8 3v4m8 3v4m-8 3v4' },
  { to: '/alertas', label: 'Alertas', icon: 'M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9m-8 12h4' },
  { to: '/productos', label: 'Productos', icon: 'M12 3 4 7v10l8 4 8-4V7zm0 0v9m8-5-8 5-8-5' },
  { to: '/proveedores', label: 'Proveedores', icon: 'M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2m6-10a4 4 0 1 0 0-8 4 4 0 0 0 0 8m7-7.5a4 4 0 0 1 0 7.7m5 7.8v-2a4 4 0 0 0-3-3.9' },
]

export function Brand() {
  return (
    <span className="brand">
      <svg viewBox="0 0 32 32" width="26" height="26" aria-hidden>
        <rect x="3" y="11" width="26" height="10" rx="5" fill="#BAC8B1" />
        <rect x="16" y="11" width="13" height="10" rx="5" fill="#7B9669" />
      </svg>
      farmatrack
    </span>
  )
}

export default function Layout() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const alertas = useAlertas()
  const pendientes = alertas.data?.filter((a) => !a.atendida).length ?? 0

  const token = currentToken()
  const claims = token ? readClaims(token) : null
  const expira = claims?.exp
    ? new Date(claims.exp * 1000).toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' })
    : null

  const salir = () => {
    clearToken()
    qc.clear()
    navigate('/acceso')
  }

  return (
    <div className="shell">
      <aside className="side">
        <Brand />
        <nav className="side-nav" aria-label="Principal">
          {nav.map((n) => (
            <NavLink key={n.to} to={n.to} end={n.end}>
              <span className="nav-label">
                <svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <path d={n.icon} />
                </svg>
                {n.label}
              </span>
              {n.to === '/alertas' && pendientes > 0 && <span className="count">{pendientes}</span>}
            </NavLink>
          ))}
        </nav>
        <div className="side-foot">
          <div>
            <span className="side-user">{claims?.sub ?? 'sesión'}</span>
            {expira && <span className="side-exp">token hasta las {expira}</span>}
          </div>
          <button className="link-btn" onClick={salir}>
            Salir
          </button>
        </div>
      </aside>
      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
