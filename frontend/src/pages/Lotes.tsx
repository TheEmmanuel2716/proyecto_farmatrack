import { useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useCrearLote, useProductos } from '../api/hooks'
import type { EstadoVencimiento } from '../api/types'
import { useLotesVista } from '../lib/lotes'
import { estadoLabel, estadoTono, fmtDias, fmtFecha, fmtNum } from '../lib/format'
import { Empty, ErrorState, Field, FormError, Loading, PageHead, Sheet, StockBar, Tag } from '../components/ui'
import { useToast } from '../components/toast'

const filtros: { value: EstadoVencimiento | ''; label: string }[] = [
  { value: '', label: 'Todos' },
  { value: 'VIGENTE', label: 'Vigentes' },
  { value: 'PROXIMO_A_VENCER', label: 'Por vencer' },
  { value: 'VENCIDO', label: 'Vencidos' },
]

export default function Lotes() {
  const [params, setParams] = useSearchParams()
  const estado = (params.get('estado') ?? '') as EstadoVencimiento | ''
  const q = params.get('q') ?? ''
  const sinStock = params.get('agotados') === '1'
  const nuevo = params.get('nuevo') === '1'
  const lotes = useLotesVista()

  const set = (key: string, value: string | null) => {
    const next = new URLSearchParams(params)
    if (value) next.set(key, value)
    else next.delete(key)
    setParams(next, { replace: true })
  }

  const filtrados = useMemo(() => {
    const term = q.trim().toLowerCase()
    return (lotes.data ?? [])
      .filter((l) => (sinStock ? true : l.stock_actual > 0))
      .filter((l) => !estado || l.estado === estado)
      .filter(
        (l) =>
          !term ||
          l.numero_lote.toLowerCase().includes(term) ||
          l.producto?.nombre.toLowerCase().includes(term) ||
          l.producto?.principio_activo.toLowerCase().includes(term),
      )
      .sort((a, b) => a.dias - b.dias)
  }, [lotes.data, estado, q, sinStock])

  const conteo = (e: EstadoVencimiento | '') =>
    (lotes.data ?? []).filter((l) => (sinStock || l.stock_actual > 0) && (!e || l.estado === e)).length

  return (
    <div className="page">
      <PageHead
        title="Lotes"
        lead="Ordenados por fecha de vencimiento, el más próximo arriba."
        actions={
          <button className="btn btn--primary" onClick={() => set('nuevo', '1')}>
            Registrar lote
          </button>
        }
      />

      <div className="toolbar">
        <div className="segmented" role="tablist" aria-label="Filtrar por estado">
          {filtros.map((f) => (
            <button
              key={f.value}
              role="tab"
              aria-selected={estado === f.value}
              onClick={() => set('estado', f.value || null)}
            >
              {f.label}
              {lotes.data && <span className="seg-n">{conteo(f.value)}</span>}
            </button>
          ))}
        </div>
        <div className="toolbar-right">
          <label className="check">
            <input type="checkbox" checked={sinStock} onChange={(e) => set('agotados', e.target.checked ? '1' : null)} />
            Incluir agotados
          </label>
          <input
            className="search"
            type="search"
            placeholder="Buscar lote, producto o principio activo"
            value={q}
            onChange={(e) => set('q', e.target.value || null)}
          />
        </div>
      </div>

      <section className="card card--flush">
        {lotes.isLoading ? (
          <Loading />
        ) : lotes.error ? (
          <ErrorState error={lotes.error} onRetry={lotes.refetch} />
        ) : filtrados.length === 0 ? (
          <Empty title={lotes.data?.length ? 'Ningún lote coincide con el filtro.' : 'Aún no hay lotes registrados.'} />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Lote</th>
                  <th>Producto</th>
                  <th>Vence</th>
                  <th className="num">Stock</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {filtrados.map((l) => (
                  <tr key={l.id}>
                    <td>
                      <Link to={`/lotes/${l.id}`} className="mono strong-link">
                        {l.numero_lote}
                      </Link>
                    </td>
                    <td>
                      <span className="cell-2">
                        <span>{l.producto?.nombre ?? `#${l.producto_id}`}</span>
                        <span className="muted small">{l.producto?.principio_activo}</span>
                      </span>
                    </td>
                    <td>
                      <span className="cell-2">
                        <span>{fmtFecha(l.fecha_vencimiento)}</span>
                        <span className={`small ${l.dias <= 30 ? `txt-${estadoTono[l.estado]}` : 'muted'}`}>
                          {fmtDias(l.dias)}
                        </span>
                      </span>
                    </td>
                    <td className="num">
                      <span className="stock-cell">
                        <span className="mono">
                          {fmtNum(l.stock_actual)}
                          <span className="muted"> / {fmtNum(l.cantidad_inicial)}</span>
                        </span>
                        <StockBar actual={l.stock_actual} inicial={l.cantidad_inicial} />
                      </span>
                    </td>
                    <td>
                      <Tag tono={estadoTono[l.estado]}>{estadoLabel[l.estado]}</Tag>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Sheet open={nuevo} title="Registrar lote" onClose={() => set('nuevo', null)}>
        <NuevoLote onDone={() => set('nuevo', null)} />
      </Sheet>
    </div>
  )
}

function NuevoLote({ onDone }: { onDone: () => void }) {
  const productos = useProductos()
  const crear = useCrearLote()
  const toast = useToast()
  const navigate = useNavigate()
  const [productoId, setProductoId] = useState('')
  const [numero, setNumero] = useState('')
  const [fecha, setFecha] = useState('')
  const [cantidad, setCantidad] = useState('')

  const submit = (e: FormEvent) => {
    e.preventDefault()
    crear.mutate(
      {
        productoId: Number(productoId),
        input: { numero_lote: numero.trim(), fecha_vencimiento: fecha, cantidad_inicial: Number(cantidad) },
      },
      {
        onSuccess: (lote) => {
          toast(`Lote ${lote.numero_lote} registrado con ${fmtNum(lote.cantidad_inicial)} unidades.`)
          onDone()
          navigate(`/lotes/${lote.id}`)
        },
      },
    )
  }

  if (productos.isLoading) return <Loading label="Cargando productos" />
  if (productos.data?.length === 0)
    return (
      <Empty title="Primero necesitas un producto.">
        <Link to="/productos?nuevo=1">Crear producto</Link>
      </Empty>
    )

  return (
    <form className="form" onSubmit={submit}>
      <Field label="Producto">
        {(id) => (
          <select id={id} value={productoId} onChange={(e) => setProductoId(e.target.value)} required>
            <option value="" disabled>
              Selecciona…
            </option>
            {[...(productos.data ?? [])]
              .sort((a, b) => a.nombre.localeCompare(b.nombre))
              .map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nombre}
                </option>
              ))}
          </select>
        )}
      </Field>
      <Field label="Número de lote" hint="Tal como aparece en la caja.">
        {(id) => (
          <input
            id={id}
            className="mono"
            value={numero}
            onChange={(e) => setNumero(e.target.value)}
            placeholder="L-2409-118"
            required
          />
        )}
      </Field>
      <div className="row-2">
        <Field label="Vence">
          {(id) => <input id={id} type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} required />}
        </Field>
        <Field label="Unidades">
          {(id) => (
            <input
              id={id}
              type="number"
              inputMode="numeric"
              min={1}
              value={cantidad}
              onChange={(e) => setCantidad(e.target.value)}
              required
            />
          )}
        </Field>
      </div>
      <p className="note">Se registra automáticamente el movimiento de ingreso.</p>
      <FormError error={crear.error} />
      <div className="form-actions">
        <button type="button" className="btn btn--ghost" onClick={onDone}>
          Cancelar
        </button>
        <button className="btn btn--primary" disabled={crear.isPending}>
          {crear.isPending ? 'Guardando…' : 'Registrar'}
        </button>
      </div>
    </form>
  )
}
