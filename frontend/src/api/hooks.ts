import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError, gateway } from './client'
import type { LoteInput, MovimientoInput, ProductoInput, ProveedorInput } from './types'

export const keys = {
  proveedores: ['proveedores'] as const,
  productos: ['productos'] as const,
  lotes: ['lotes'] as const,
  lote: (id: number) => ['lotes', id] as const,
  detalleGateway: (id: number) => ['gateway', 'lote', id] as const,
  movimientos: (id: number) => ['lotes', id, 'movimientos'] as const,
  estado: (id: number) => ['estado', id] as const,
  alertas: ['alertas'] as const,
}

export const useProveedores = () => useQuery({ queryKey: keys.proveedores, queryFn: gateway.proveedores })
export const useProductos = () => useQuery({ queryKey: keys.productos, queryFn: gateway.productos })
export const useLotes = () => useQuery({ queryKey: keys.lotes, queryFn: gateway.lotes })
export const useAlertas = () =>
  useQuery({ queryKey: keys.alertas, queryFn: gateway.alertas, refetchInterval: 60_000 })

export const useLote = (id: number) =>
  useQuery({ queryKey: keys.lote(id), queryFn: () => gateway.lote(id) })

export const useDetalleGateway = (id: number) =>
  useQuery({
    queryKey: keys.detalleGateway(id),
    queryFn: () => gateway.detalleLote(id),
    refetchInterval: (q) => (q.state.data && !q.state.data.estado ? 3000 : false),
  })

export const useMovimientos = (id: number) =>
  useQuery({ queryKey: keys.movimientos(id), queryFn: () => gateway.movimientos(id) })

/**
 * Vencimientos se entera de los lotes por Kafka, así que justo después de crear
 * uno puede responder LOTE_NO_SINCRONIZADO. En ese caso se reintenta cada 3 s.
 */
export const useEstadoLote = (id: number) =>
  useQuery({
    queryKey: keys.estado(id),
    queryFn: () => gateway.estado(id),
    retry: false,
    refetchInterval: (q) => {
      const err = q.state.error
      return err instanceof ApiError && err.codigo === 'LOTE_NO_SINCRONIZADO' ? 3000 : false
    },
  })

export function useGuardarProveedor() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: ProveedorInput }) =>
      id ? gateway.editarProveedor(id, input) : gateway.crearProveedor(input),
    onSuccess: () => qc.invalidateQueries({ queryKey: keys.proveedores }),
  })
}

export function useGuardarProducto() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: ProductoInput }) =>
      id ? gateway.editarProducto(id, input) : gateway.crearProducto(input),
    onSuccess: () => qc.invalidateQueries({ queryKey: keys.productos }),
  })
}

/** Refresca lo que depende de vencimientos un poco después, cuando Kafka ya entregó el evento. */
function refrescarVencimientos(qc: ReturnType<typeof useQueryClient>, loteId?: number) {
  window.setTimeout(() => {
    qc.invalidateQueries({ queryKey: keys.alertas })
    if (loteId) qc.invalidateQueries({ queryKey: keys.estado(loteId) })
  }, 1500)
}

export function useCrearLote() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ productoId, input }: { productoId: number; input: LoteInput }) =>
      gateway.crearLote(productoId, input),
    onSuccess: (lote) => {
      qc.invalidateQueries({ queryKey: keys.lotes })
      refrescarVencimientos(qc, lote.id)
    },
  })
}

export function useMovimiento(loteId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ tipo, input }: { tipo: 'VENTA' | 'DEVOLUCION'; input: MovimientoInput }) =>
      tipo === 'VENTA' ? gateway.vender(loteId, input) : gateway.devolver(loteId, input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: keys.lotes })
      refrescarVencimientos(qc, loteId)
    },
  })
}

export function useMarcarAlerta() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, atendida }: { id: number; atendida: boolean }) =>
      gateway.marcarAlerta(id, atendida),
    onSuccess: (alerta) => {
      qc.invalidateQueries({ queryKey: keys.alertas })
      qc.invalidateQueries({ queryKey: keys.detalleGateway(alerta.lote_id) })
    },
  })
}
