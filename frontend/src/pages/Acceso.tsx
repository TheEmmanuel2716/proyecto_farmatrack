import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { isUsable, readClaims, setToken } from '../auth'
import { Brand } from '../components/Layout'
import { Field, FormError } from '../components/ui'

export default function Acceso() {
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as { from?: string; expired?: boolean } | null
  const [token, setTokenInput] = useState('')
  const [error, setError] = useState<Error | null>(null)

  const entrar = (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    const jwt = token.trim().replace(/^Bearer\s+/i, '')
    if (!readClaims(jwt)) {
      setError(new Error('Eso no parece un JWT (deben ser tres partes separadas por puntos).'))
      return
    }
    if (!isUsable(jwt)) {
      setError(new Error('El token ya expiró. Genera uno nuevo.'))
      return
    }
    setToken(jwt)
    navigate(state?.from && state.from !== '/acceso' ? state.from : '/', { replace: true })
  }

  return (
    <div className="access">
      <section className="access-art" aria-hidden>
        <Brand />
        <div className="access-copy">
          <p className="access-big">Cada lote, con su fecha a la vista.</p>
          <p>Inventario, ventas y vencimientos de la farmacia en un mismo lugar.</p>
        </div>
      </section>

      <section className="access-form">
        <form onSubmit={entrar} className="card access-card">
          <h1>Conectar a FarmaTrack</h1>
          {state?.expired && <p className="note note--warn">La sesión expiró o el token fue rechazado.</p>}

          <div className="jwt-help">
            <p>Genera un token local desde la raíz del proyecto con el script incluido:</p>
            <code>python3 scripts/generate-test-jwt.py 'dev-only-secret-change-me-32-bytes'</code>
            <p className="muted small">El token dura una hora. El secreto nunca se envía al navegador.</p>
          </div>

          <Field label="Token JWT" hint="Pega aquí el token completo que imprimió el script.">
            {(id) => (
              <textarea
                id={id}
                rows={5}
                className="mono"
                value={token}
                onChange={(e) => setTokenInput(e.target.value)}
                placeholder="eyJhbGciOiJIUzI1NiIs..."
                spellCheck={false}
                required
              />
            )}
          </Field>

          <FormError error={error} />
          <button className="btn btn--primary btn--block">Entrar</button>
        </form>
      </section>
    </div>
  )
}
