import { useMemo, useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useGuardarProducto, useProductos, useProveedores } from '../api/hooks'
import type { Producto } from '../api/types'
import { useLotesVista } from '../lib/lotes'
import { fmtNum } from '../lib/format'
import { Empty, ErrorState, Field, FormError, Loading, PageHead, Sheet } from '../components/ui'
import { useToast } from '../components/toast'

export default function Productos() {
  const [params, setParams] = useSearchParams()
  const productos = useProductos()
  const proveedores = useProveedores()
  const lotes = useLotesVista()
  const [q, setQ] = useState('')
  const [editando, setEditando] = useState<Producto | null>(null)
  const abierto = params.get('nuevo') === '1' || editando !== null

  const cerrar = () => {
    setEditando(null)
    params.delete('nuevo')
    setParams(params, { replace: true })
  }

  const stockPorProducto = useMemo(() => {
    const m = new Map<number, { stock: number; lotes: number }>()
    lotes.data?.forEach((l) => {
      const cur = m.get(l.producto_id) ?? { stock: 0, lotes: 0 }
      cur.stock += l.stock_actual
      if (l.stock_actual > 0) cur.lotes += 1
      m.set(l.producto_id, cur)
    })
    return m
  }, [lotes.data])

  const provPorId = new Map(proveedores.data?.map((p) => [p.id, p]))
  const term = q.trim().toLowerCase()
  const lista = [...(productos.data ?? [])]
    .filter((p) => !term || p.nombre.toLowerCase().includes(term) || p.principio_activo.toLowerCase().includes(term))
    .sort((a, b) => a.nombre.localeCompare(b.nombre))

  return (
    <div className="page">
      <PageHead
        title="Productos"
        actions={
          <button
            className="btn btn--primary"
            onClick={() => {
              params.set('nuevo', '1')
              setParams(params, { replace: true })
            }}
          >
            Nuevo producto
          </button>
        }
      />

      <div className="toolbar">
        <input
          className="search"
          type="search"
          placeholder="Buscar por nombre o principio activo"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
      </div>

      <section className="card card--flush">
        {productos.isLoading ? (
          <Loading />
        ) : productos.error ? (
          <ErrorState error={productos.error} onRetry={() => productos.refetch()} />
        ) : lista.length === 0 ? (
          <Empty title={productos.data?.length ? 'Sin coincidencias.' : 'Aún no hay productos.'} />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Producto</th>
                  <th>Principio activo</th>
                  <th>Proveedor</th>
                  <th className="num">Lotes activos</th>
                  <th className="num">Stock</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {lista.map((p) => {
                  const s = stockPorProducto.get(p.id)
                  return (
                    <tr key={p.id}>
                      <td className="strong">{p.nombre}</td>
                      <td>{p.principio_activo}</td>
                      <td className="muted">{provPorId.get(p.proveedor_id)?.nombre ?? `#${p.proveedor_id}`}</td>
                      <td className="num mono">
                        {s?.lotes ? (
                          <Link to={`/lotes?q=${encodeURIComponent(p.nombre)}`} className="strong-link">
                            {s.lotes}
                          </Link>
                        ) : (
                          <span className="muted">0</span>
                        )}
                      </td>
                      <td className="num mono">{fmtNum(s?.stock ?? 0)}</td>
                      <td className="actions">
                        <button className="btn btn--ghost btn--sm" onClick={() => setEditando(p)}>
                          Editar
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Sheet open={abierto} title={editando ? 'Editar producto' : 'Nuevo producto'} onClose={cerrar}>
        <ProductoForm key={editando?.id ?? 'nuevo'} producto={editando} onDone={cerrar} />
      </Sheet>
    </div>
  )
}

function ProductoForm({ producto, onDone }: { producto: Producto | null; onDone: () => void }) {
  const proveedores = useProveedores()
  const guardar = useGuardarProducto()
  const toast = useToast()
  const [nombre, setNombre] = useState(producto?.nombre ?? '')
  const [principio, setPrincipio] = useState(producto?.principio_activo ?? '')
  const [proveedorId, setProveedorId] = useState(producto ? String(producto.proveedor_id) : '')

  const submit = (e: FormEvent) => {
    e.preventDefault()
    guardar.mutate(
      {
        id: producto?.id,
        input: { nombre: nombre.trim(), principio_activo: principio.trim(), proveedor_id: Number(proveedorId) },
      },
      {
        onSuccess: (p) => {
          toast(producto ? 'Cambios guardados.' : `${p.nombre} creado.`)
          onDone()
        },
      },
    )
  }

  if (proveedores.isLoading) return <Loading label="Cargando proveedores" />
  if (proveedores.data?.length === 0)
    return (
      <Empty title="Primero registra un proveedor.">
        <Link to="/proveedores?nuevo=1">Nuevo proveedor</Link>
      </Empty>
    )

  return (
    <form className="form" onSubmit={submit}>
      <Field label="Nombre comercial" hint="Incluye concentración y presentación.">
        {(id) => (
          <input
            id={id}
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Acetaminofén 500 mg x 20 tabletas"
            required
          />
        )}
      </Field>
      <Field label="Principio activo">
        {(id) => (
          <input id={id} value={principio} onChange={(e) => setPrincipio(e.target.value)} placeholder="Acetaminofén" required />
        )}
      </Field>
      <Field label="Proveedor">
        {(id) => (
          <select id={id} value={proveedorId} onChange={(e) => setProveedorId(e.target.value)} required>
            <option value="" disabled>
              Selecciona…
            </option>
            {proveedores.data?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nombre}
              </option>
            ))}
          </select>
        )}
      </Field>
      <FormError error={guardar.error} />
      <div className="form-actions">
        <button type="button" className="btn btn--ghost" onClick={onDone}>
          Cancelar
        </button>
        <button className="btn btn--primary" disabled={guardar.isPending}>
          {guardar.isPending ? 'Guardando…' : 'Guardar'}
        </button>
      </div>
    </form>
  )
}
