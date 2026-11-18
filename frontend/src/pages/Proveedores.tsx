import { useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useGuardarProveedor, useProductos, useProveedores } from '../api/hooks'
import type { Proveedor } from '../api/types'
import { Empty, ErrorState, Field, FormError, Loading, PageHead, Sheet } from '../components/ui'
import { useToast } from '../components/toast'

export default function Proveedores() {
  const [params, setParams] = useSearchParams()
  const proveedores = useProveedores()
  const productos = useProductos()
  const [editando, setEditando] = useState<Proveedor | null>(null)
  const abierto = params.get('nuevo') === '1' || editando !== null

  const cerrar = () => {
    setEditando(null)
    params.delete('nuevo')
    setParams(params, { replace: true })
  }

  const productosDe = (id: number) => productos.data?.filter((p) => p.proveedor_id === id).length ?? 0
  const lista = [...(proveedores.data ?? [])].sort((a, b) => a.nombre.localeCompare(b.nombre))

  return (
    <div className="page">
      <PageHead
        title="Proveedores"
        actions={
          <button
            className="btn btn--primary"
            onClick={() => {
              params.set('nuevo', '1')
              setParams(params, { replace: true })
            }}
          >
            Nuevo proveedor
          </button>
        }
      />

      <section className="card card--flush">
        {proveedores.isLoading ? (
          <Loading />
        ) : proveedores.error ? (
          <ErrorState error={proveedores.error} onRetry={() => proveedores.refetch()} />
        ) : lista.length === 0 ? (
          <Empty title="Aún no hay proveedores." />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>NIT</th>
                  <th>Contacto</th>
                  <th className="num">Productos</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {lista.map((p) => (
                  <tr key={p.id}>
                    <td className="strong">{p.nombre}</td>
                    <td className="mono">{p.nit}</td>
                    <td className="muted">{p.contacto || '—'}</td>
                    <td className="num mono">{productosDe(p.id)}</td>
                    <td className="actions">
                      <button className="btn btn--ghost btn--sm" onClick={() => setEditando(p)}>
                        Editar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Sheet open={abierto} title={editando ? 'Editar proveedor' : 'Nuevo proveedor'} onClose={cerrar}>
        <ProveedorForm key={editando?.id ?? 'nuevo'} proveedor={editando} onDone={cerrar} />
      </Sheet>
    </div>
  )
}

function ProveedorForm({ proveedor, onDone }: { proveedor: Proveedor | null; onDone: () => void }) {
  const guardar = useGuardarProveedor()
  const toast = useToast()
  const [nombre, setNombre] = useState(proveedor?.nombre ?? '')
  const [nit, setNit] = useState(proveedor?.nit ?? '')
  const [contacto, setContacto] = useState(proveedor?.contacto ?? '')

  const submit = (e: FormEvent) => {
    e.preventDefault()
    guardar.mutate(
      { id: proveedor?.id, input: { nombre: nombre.trim(), nit: nit.trim(), contacto: contacto.trim() || undefined } },
      {
        onSuccess: (p) => {
          toast(proveedor ? 'Cambios guardados.' : `${p.nombre} registrado.`)
          onDone()
        },
      },
    )
  }

  return (
    <form className="form" onSubmit={submit}>
      <Field label="Razón social">
        {(id) => (
          <input id={id} value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Laboratorios Genfar S.A." required />
        )}
      </Field>
      <Field label="NIT" hint="Con dígito de verificación.">
        {(id) => (
          <input id={id} className="mono" value={nit} onChange={(e) => setNit(e.target.value)} placeholder="900123456-1" required />
        )}
      </Field>
      <Field label="Contacto (opcional)">
        {(id) => (
          <input id={id} value={contacto} onChange={(e) => setContacto(e.target.value)} placeholder="ventas@genfar.com" />
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
