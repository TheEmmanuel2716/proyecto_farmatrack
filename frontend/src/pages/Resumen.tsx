import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAlertas } from '../api/hooks'
import { useLotesVista, type LoteVista } from '../lib/lotes'
import {
  alertaLabel,
  alertaTono,
  estadoLabel,
  estadoTono,
  fmtDias,
  fmtFecha,
  fmtFechaCorta,
  fmtFechaHora,
  fmtNum,
  today,
} from '../lib/format'
import { Empty, ErrorState, Loading, PageHead, Tag } from '../components/ui'

const VENTANA = 120

export default function Resumen() {
  const lotes = useLotesVista()
  const alertas = useAlertas()

  if (lotes.isLoading) return <Loading />
  if (lotes.error) return <ErrorState error={lotes.error} onRetry={lotes.refetch} />

  const todos = lotes.data ?? []
  const conStock = todos.filter((l) => l.stock_actual > 0)
  const unidades = conStock.reduce((acc, l) => acc + l.stock_actual, 0)
  const porVencer = conStock.filter((l) => l.estado === 'PROXIMO_A_VENCER')
  const vencidos = conStock.filter((l) => l.estado === 'VENCIDO')
  const pendientes = (alertas.data ?? []).filter((a) => !a.atendida)
  const atencion = [...vencidos, ...porVencer].sort((a, b) => a.dias - b.dias)

  const hoy = today().toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' })

  return (
    <div className="page">
      <PageHead
        title="Resumen"
        lead={<span className="cap">{hoy}</span>}
        actions={
          <Link to="/lotes?nuevo=1" className="btn btn--primary">
            Registrar lote
          </Link>
        }
      />

      <section className="figures" aria-label="Indicadores">
        <Figure label="Lotes con existencias" value={conStock.length} detail={`${fmtNum(todos.length)} registrados`} />
        <Figure label="Unidades en stock" value={unidades} />
        <Figure
          label="Por vencer (≤ 30 días)"
          value={porVencer.length}
          tone={porVencer.length ? 'warn' : undefined}
        />
        <Figure
          label="Vencidos con stock"
          value={vencidos.length}
          tone={vencidos.length ? 'danger' : undefined}
          detail={vencidos.length ? 'no se pueden vender' : undefined}
        />
      </section>

      <section className="card">
        <div className="card-head">
          <h2>Próximos {VENTANA} días</h2>
          <div className="legend">
            <span className="legend-i legend-i--ok">vigente</span>
            <span className="legend-i legend-i--warn">por vencer</span>
            <span className="legend-i legend-i--danger">vencido</span>
          </div>
        </div>
        {conStock.length === 0 ? (
          <Empty title="Todavía no hay lotes con existencias.">
            <Link to="/lotes?nuevo=1">Registra el primero</Link> para ver aquí sus fechas.
          </Empty>
        ) : (
          <Timeline lotes={conStock} />
        )}
      </section>

      <div className="grid-2">
        <section className="card">
          <div className="card-head">
            <h2>Requieren atención</h2>
            <Link to="/lotes?estado=PROXIMO_A_VENCER" className="link-sm">
              Ver lotes
            </Link>
          </div>
          {atencion.length === 0 ? (
            <Empty title="Nada por vencer en los próximos 30 días." />
          ) : (
            <ul className="list">
              {atencion.slice(0, 7).map((l) => (
                <li key={l.id}>
                  <Link to={`/lotes/${l.id}`} className="list-row">
                    <span className="list-main">
                      <strong>{l.producto?.nombre ?? `Producto ${l.producto_id}`}</strong>
                      <span className="mono muted">
                        {l.numero_lote} · {fmtNum(l.stock_actual)} und
                      </span>
                    </span>
                    <span className="list-side">
                      <Tag tono={estadoTono[l.estado]}>{estadoLabel[l.estado]}</Tag>
                      <span className="muted small">{fmtDias(l.dias)}</span>
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card">
          <div className="card-head">
            <h2>Alertas sin atender</h2>
            <Link to="/alertas" className="link-sm">
              Ver todas
            </Link>
          </div>
          {alertas.isLoading ? (
            <Loading />
          ) : alertas.error ? (
            <ErrorState error={alertas.error} onRetry={() => alertas.refetch()} />
          ) : pendientes.length === 0 ? (
            <Empty title="No hay alertas pendientes." />
          ) : (
            <ul className="list">
              {pendientes
                .sort((a, b) => b.generada_en.localeCompare(a.generada_en))
                .slice(0, 7)
                .map((a) => {
                  const lote = todos.find((l) => l.id === a.lote_id)
                  return (
                    <li key={a.id}>
                      <Link to={`/lotes/${a.lote_id}`} className="list-row">
                        <span className="list-main">
                          <strong>{lote?.producto?.nombre ?? `Lote ${a.lote_id}`}</strong>
                          <span className="muted small">{fmtFechaHora(a.generada_en)}</span>
                        </span>
                        <Tag tono={alertaTono(a.tipo_alerta)}>{alertaLabel[a.tipo_alerta]}</Tag>
                      </Link>
                    </li>
                  )
                })}
            </ul>
          )}
        </section>
      </div>
    </div>
  )
}

function Figure({
  label,
  value,
  detail,
  tone,
}: {
  label: string
  value: number
  detail?: string
  tone?: 'warn' | 'danger'
}) {
  return (
    <div className={`figure${tone ? ` figure--${tone}` : ''}`}>
      <span className="figure-label">{label}</span>
      <span className="figure-value">{fmtNum(value)}</span>
      {detail && <span className="figure-detail">{detail}</span>}
    </div>
  )
}

/** Línea de tiempo: cada lote es una marca en su fecha de vencimiento. */
function Timeline({ lotes }: { lotes: LoteVista[] }) {
  const vencidos = lotes.filter((l) => l.dias < 0)
  const visibles = lotes.filter((l) => l.dias >= 0 && l.dias <= VENTANA).sort((a, b) => a.dias - b.dias)
  const despues = lotes.filter((l) => l.dias > VENTANA).length

  const trackRef = useRef<HTMLDivElement>(null)
  const [ancho, setAncho] = useState(800)
  useEffect(() => {
    const el = trackRef.current
    if (!el) return
    const ro = new ResizeObserver(([e]) => setAncho(e.contentRect.width))
    ro.observe(el)
    return () => ro.disconnect()
  }, [])

  // Marcas de mes
  const base = today()
  const meses: { left: number; label: string }[] = []
  for (let i = 1; i <= 5; i++) {
    const d = new Date(base.getFullYear(), base.getMonth() + i, 1)
    const dias = Math.round((d.getTime() - base.getTime()) / 86_400_000)
    if (dias > VENTANA) break
    meses.push({ left: (dias / VENTANA) * 100, label: capital(d.toLocaleDateString('es-CO', { month: 'short' }).replace('.', '')) })
  }

  // Apila etiquetas que se encimarían, según el ancho real de la pista
  const filas: number[] = []
  const puestos = visibles.map((l) => {
    const left = (l.dias / VENTANA) * 100
    const x = (left / 100) * ancho
    const w = l.numero_lote.length * 7 + 18
    const [ini, fin] = left > 82 ? [x - w, x] : [x, x + w]
    let fila = filas.findIndex((finPrevio) => ini > finPrevio + 4)
    if (fila === -1) {
      fila = filas.length
      filas.push(fin)
    } else filas[fila] = fin
    return { l, left, fila }
  })
  const altura = Math.max(1, filas.length)

  return (
    <div className="timeline">
      <div className="tl-overdue">
        <span className="tl-overdue-n">{vencidos.length}</span>
        <span className="small">vencidos</span>
      </div>
      <div className="tl-track-wrap">
        <div className="tl-track" ref={trackRef} style={{ height: `${altura * 30 + 26}px` }}>
          <div className="tl-zone" style={{ width: `${(30 / VENTANA) * 100}%` }} title="Zona de 30 días" />
          {meses.map((m) => (
            <span key={m.label} className="tl-month" style={{ left: `${m.left}%` }}>
              {m.label}
            </span>
          ))}
          <span className="tl-today">hoy</span>
          {puestos.map(({ l, left, fila }) => (
            <Link
              key={l.id}
              to={`/lotes/${l.id}`}
              className={`tl-mark tl-mark--${estadoTono[l.estado]}${left > 82 ? ' tl-mark--end' : ''}`}
              style={
                {
                  left: `${left}%`,
                  bottom: `${fila * 30 + 24}px`,
                  zIndex: 10 - fila,
                  '--stem': `${fila * 30 + 6}px`,
                } as React.CSSProperties
              }
              title={`${l.producto?.nombre ?? ''} · ${l.numero_lote} · ${fmtFecha(l.fecha_vencimiento)}`}
            >
              <span className="mono">{l.numero_lote}</span>
            </Link>
          ))}
        </div>
        <div className="tl-foot small muted">
          <span>{fmtFechaCorta(base)}</span>
          <span>
            {despues > 0 ? `+${despues} lote${despues === 1 ? '' : 's'} después de ` : ''}
            {fmtFechaCorta(new Date(base.getTime() + VENTANA * 86_400_000))}
          </span>
        </div>
      </div>
    </div>
  )
}

const capital = (t: string) => t.charAt(0).toUpperCase() + t.slice(1)
