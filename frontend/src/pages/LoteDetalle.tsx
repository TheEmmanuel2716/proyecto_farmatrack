import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  useDetalleGateway,
  useAlertas,
  useMarcarAlerta,
  useMovimiento,
  useMovimientos,
} from '../api/hooks'
import {
  alertaLabel,
  alertaTono,
  daysUntil,
  estadoLabel,
  estadoPorDias,
  estadoTono,
  fmtDias,
  fmtFecha,
  fmtFechaHora,
  fmtNum,
  movimientoLabel,
} from '../lib/format'
import { Empty, ErrorState, Field, FormError, Loading, StockBar, Tag } from '../components/ui'
import { useToast } from '../components/toast'

export default function LoteDetalle() {
  const id = Number(useParams().id)
  const detalle = useDetalleGateway(id)
  const movimientos = useMovimientos(id)
  const alertas = useAlertas()

  if (detalle.isLoading) return <Loading />
  if (detalle.error)
    return (
      <div className="page">
        <Link to="/lotes" className="back">
          ← Lotes
        </Link>
        <ErrorState error={detalle.error} onRetry={() => detalle.refetch()} />
      </div>
    )

  const { lote: l, producto, proveedor, estado } = detalle.data!
  const dias = daysUntil(l.fecha_vencimiento)
  const estadoLocal = estadoPorDias(dias)
  const alertasLote = (alertas.data ?? [])
    .filter((a) => a.lote_id === id)
    .sort((a, b) => b.generada_en.localeCompare(a.generada_en))

  return (
    <div className="page">
      <Link to="/lotes" className="back">
        ← Lotes
      </Link>

      <header className="lote-head">
        <div>
          <span className="eyebrow mono">Lote {l.numero_lote}</span>
          <h1>{producto?.nombre ?? 'Producto sin información'}</h1>
          <p className="page-lead">
            {producto?.principio_activo}
            {proveedor && <> · {proveedor.nombre}</>}
          </p>
        </div>
        <Tag tono={estadoTono[estado?.estado ?? estadoLocal]}>{estadoLabel[estado?.estado ?? estadoLocal]}</Tag>
      </header>

      <section className="facts">
        <div>
          <span className="fact-label">Vence</span>
          <span className="fact-value">{fmtFecha(l.fecha_vencimiento)}</span>
          <span className={`small ${dias <= 30 ? `txt-${estadoTono[estadoLocal]}` : 'muted'}`}>{fmtDias(dias)}</span>
        </div>
        <div>
          <span className="fact-label">Stock</span>
          <span className="fact-value mono">
            {fmtNum(l.stock_actual)}
            <span className="muted"> / {fmtNum(l.cantidad_inicial)}</span>
          </span>
          <StockBar actual={l.stock_actual} inicial={l.cantidad_inicial} />
        </div>
        <div>
          <span className="fact-label">Seguimiento de vencimientos</span>
          {!estado ? (
            <span className="sync">
              <span className="spinner" aria-hidden /> Esperando el evento de Kafka
            </span>
          ) : (
            <>
              <span className="fact-value">{estadoLabel[estado.estado]}</span>
              <span className="muted small">actualizado {fmtFechaHora(estado.actualizado_en)}</span>
            </>
          )}
        </div>
        <div>
          <span className="fact-label">Registrado</span>
          <span className="fact-value">{fmtFecha(l.creado_en)}</span>
        </div>
      </section>

      <div className="grid-detail">
        <div className="stack">
          <section className="card card--flush">
            <div className="card-head card-head--pad">
              <h2>Movimientos</h2>
            </div>
            {movimientos.isLoading ? (
              <Loading />
            ) : movimientos.error ? (
              <ErrorState error={movimientos.error} onRetry={() => movimientos.refetch()} />
            ) : movimientos.data!.length === 0 ? (
              <Empty title="Sin movimientos." />
            ) : (
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th>Tipo</th>
                      <th className="num">Cantidad</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[...movimientos.data!]
                      .sort((a, b) => b.fecha_hora.localeCompare(a.fecha_hora))
                      .map((m) => (
                        <tr key={m.id}>
                          <td>{fmtFechaHora(m.fecha_hora)}</td>
                          <td>
                            <span className={`mov mov--${m.tipo.toLowerCase()}`}>{movimientoLabel[m.tipo]}</span>
                          </td>
                          <td className="num mono">
                            {m.tipo === 'INGRESO' ? '+' : '−'}
                            {fmtNum(m.cantidad)}
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section className="card">
            <div className="card-head">
              <h2>Alertas del lote</h2>
            </div>
            {alertasLote.length === 0 ? (
              <p className="muted small">
                Este lote no tiene alertas. Se generan a los 30, 15 y 7 días, y cuando vence.
              </p>
            ) : (
              <ul className="list">
                {alertasLote.map((a) => (
                  <AlertaFila key={a.id} id={a.id} atendida={a.atendida} generada={a.generada_en}>
                    <Tag tono={alertaTono(a.tipo_alerta)}>{alertaLabel[a.tipo_alerta]}</Tag>
                  </AlertaFila>
                ))}
              </ul>
            )}
          </section>
        </div>

        <MovimientoForm loteId={id} stock={l.stock_actual} vencido={dias < 0} />
      </div>
    </div>
  )
}

function AlertaFila({
  id,
  atendida,
  generada,
  children,
}: {
  id: number
  atendida: boolean
  generada: string
  children: React.ReactNode
}) {
  const marcar = useMarcarAlerta()
  return (
    <li className="list-row list-row--static">
      <span className="list-main">
        {children}
        <span className="muted small">{fmtFechaHora(generada)}</span>
      </span>
      <button
        className={`btn btn--sm ${atendida ? 'btn--ghost' : 'btn--soft'}`}
        disabled={marcar.isPending}
        onClick={() => marcar.mutate({ id, atendida: !atendida })}
      >
        {atendida ? 'Reabrir' : 'Marcar atendida'}
      </button>
    </li>
  )
}

function MovimientoForm({ loteId, stock, vencido }: { loteId: number; stock: number; vencido: boolean }) {
  const [tipo, setTipo] = useState<'VENTA' | 'DEVOLUCION'>(vencido ? 'DEVOLUCION' : 'VENTA')
  const [cantidad, setCantidad] = useState('')
  const [observacion, setObservacion] = useState('')
  const mov = useMovimiento(loteId)
  const toast = useToast()

  const submit = (e: FormEvent) => {
    e.preventDefault()
    mov.mutate(
      { tipo, input: { cantidad: Number(cantidad), observacion: observacion.trim() || undefined } },
      {
        onSuccess: (m) => {
          toast(`${movimientoLabel[m.tipo]} de ${fmtNum(m.cantidad)} unidades registrada.`)
          setCantidad('')
          setObservacion('')
        },
      },
    )
  }

  const bloqueado = stock === 0 || (tipo === 'VENTA' && vencido)

  return (
    <aside className="card mov-card">
      <h2>Registrar salida</h2>
      <div className="segmented segmented--full" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={tipo === 'VENTA'}
          onClick={() => {
            setTipo('VENTA')
            mov.reset()
          }}
        >
          Venta
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={tipo === 'DEVOLUCION'}
          onClick={() => {
            setTipo('DEVOLUCION')
            mov.reset()
          }}
        >
          Devolución
        </button>
      </div>

      {tipo === 'VENTA' && vencido && (
        <p className="note note--danger">Este lote está vencido y no se puede vender. Puedes devolverlo al proveedor.</p>
      )}
      {stock === 0 && <p className="note">El lote está agotado.</p>}

      <form className="form" onSubmit={submit}>
        <Field label="Cantidad" hint={`Disponibles: ${fmtNum(stock)}`}>
          {(id) => (
            <input
              id={id}
              type="number"
              inputMode="numeric"
              min={1}
              max={stock || undefined}
              value={cantidad}
              onChange={(e) => setCantidad(e.target.value)}
              disabled={bloqueado}
              required
            />
          )}
        </Field>
        <Field label="Observación (opcional)">
          {(id) => (
            <input
              id={id}
              value={observacion}
              onChange={(e) => setObservacion(e.target.value)}
              placeholder={tipo === 'VENTA' ? 'Venta mostrador' : 'Empaque dañado'}
              disabled={bloqueado}
            />
          )}
        </Field>
        <FormError error={mov.error} />
        <button className="btn btn--primary btn--block" disabled={bloqueado || mov.isPending}>
          {mov.isPending ? 'Registrando…' : tipo === 'VENTA' ? 'Registrar venta' : 'Registrar devolución'}
        </button>
      </form>
    </aside>
  )
}
