import { useMemo } from 'react'
import { useLotes, useProductos } from '../api/hooks'
import type { EstadoVencimiento, Lote, Producto } from '../api/types'
import { daysUntil, estadoPorDias } from './format'

export interface LoteVista extends Lote {
  producto?: Producto
  dias: number
  estado: EstadoVencimiento
}

/** Lotes con su producto y el estado calculado con la misma regla del servicio. */
export function useLotesVista() {
  const lotes = useLotes()
  const productos = useProductos()

  const data = useMemo<LoteVista[] | undefined>(() => {
    if (!lotes.data) return undefined
    const porId = new Map(productos.data?.map((p) => [p.id, p]))
    return lotes.data.map((l) => {
      const dias = daysUntil(l.fecha_vencimiento)
      return { ...l, producto: porId.get(l.producto_id), dias, estado: estadoPorDias(dias) }
    })
  }, [lotes.data, productos.data])

  return {
    data,
    isLoading: lotes.isLoading,
    error: lotes.error,
    refetch: () => {
      lotes.refetch()
      productos.refetch()
    },
  }
}
