import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAlertas, useMarcarAlerta } from '../api/hooks'
import type { Alerta } from '../api/types'
import { useLotesVista } from '../lib/lotes'
import { alertaLabel, alertaTono, fmtDias, fmtFecha, fmtFechaHora } from '../lib/format'
import { Empty, ErrorState, Loading, PageHead, Tag } from '../components/ui'
import { useToast } from '../components/toast'

type Vista = 'pendientes' | 'atendidas' | 'todas'

export default function Alertas() {
  const [vista, setVista] = useState<Vista>('pendientes')
  const alertas = useAlertas()
  const lotes = useLotesVista()

  const lista = useMemo(() => {
    const all = alertas.data ?? []
    const f =
      vista === 'pendientes' ? all.filter((a) => !a.atendida) : vista === 'atendidas' ? all.filter((a) => a.atendida) : all
    return [...f].sort((a, b) => b.generada_en.localeCompare(a.generada_en))
  }, [alertas.data, vista])

  const n = (v: Vista) =>
    (alertas.data ?? []).filter((a) => (v === 'todas' ? true : v === 'atendidas' ? a.atendida : !a.atendida)).length

  return (
    <div className="page">
      <PageHead
        title="Alertas"
        lead="Las genera el servicio de vencimientos a los 30, 15 y 7 días, y cuando el lote vence. Marcarlas como atendidas no cambia el stock."
      />

      <div className="toolbar">
        <div className="segmented" role="tablist">
          {(['pendientes', 'atendidas', 'todas'] as Vista[]).map((v) => (
            <button key={v} role="tab" aria-selected={vista === v} onClick={() => setVista(v)}>
              {v[0].toUpperCase() + v.slice(1)}
              {alertas.data && <span className="seg-n">{n(v)}</span>}
            </button>
          ))}
        </div>
      </div>

      <section className="card card--flush">
        {alertas.isLoading ? (
          <Loading />
        ) : alertas.error ? (
          <ErrorState error={alertas.error} onRetry={() => alertas.refetch()} />
        ) : lista.length === 0 ? (
          <Empty title={vista === 'pendientes' ? 'No hay alertas pendientes.' : 'No hay alertas en esta vista.'} />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Aviso</th>
                  <th>Lote</th>
                  <th>Vence</th>
                  <th>Generada</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {lista.map((a) => {
                  const l = lotes.data?.find((x) => x.id === a.lote_id)
                  return (
                    <tr key={a.id} className={a.atendida ? 'row-dim' : undefined}>
                      <td>
                        <Tag tono={a.atendida ? 'muted' : alertaTono(a.tipo_alerta)}>{alertaLabel[a.tipo_alerta]}</Tag>
                      </td>
                      <td>
                        <span className="cell-2">
                          <Link to={`/lotes/${a.lote_id}`} className="strong-link">
                            {l?.producto?.nombre ?? `Lote ${a.lote_id}`}
                          </Link>
                          <span className="mono muted small">{l?.numero_lote}</span>
                        </span>
                      </td>
                      <td>
                        {l ? (
                          <span className="cell-2">
                            <span>{fmtFecha(l.fecha_vencimiento)}</span>
                            <span className="muted small">{fmtDias(l.dias)}</span>
                          </span>
                        ) : (
                          <span className="muted">—</span>
                        )}
                      </td>
                      <td className="muted">{fmtFechaHora(a.generada_en)}</td>
                      <td className="actions">
                        <Accion alerta={a} />
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}

function Accion({ alerta }: { alerta: Alerta }) {
  const marcar = useMarcarAlerta()
  const toast = useToast()
  return (
    <button
      className={`btn btn--sm ${alerta.atendida ? 'btn--ghost' : 'btn--soft'}`}
      disabled={marcar.isPending}
      onClick={() =>
        marcar.mutate(
          { id: alerta.id, atendida: !alerta.atendida },
          {
            onSuccess: (a) => toast(a.atendida ? 'Alerta marcada como atendida.' : 'Alerta reabierta.'),
            onError: (e) => toast(e.message, 'error'),
          },
        )
      }
    >
      {alerta.atendida ? 'Reabrir' : 'Marcar atendida'}
    </button>
  )
}
