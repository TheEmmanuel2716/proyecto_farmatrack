// Contratos tal como los devuelven los servicios (snake_case).

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface Proveedor {
  id: number
  nombre: string
  nit: string
  contacto: string | null
}

export interface ProveedorInput {
  nombre: string
  nit: string
  contacto?: string
}

export interface Producto {
  id: number
  nombre: string
  principio_activo: string
  proveedor_id: number
}

export interface ProductoInput {
  nombre: string
  principio_activo: string
  proveedor_id: number
}

export interface Lote {
  id: number
  numero_lote: string
  fecha_vencimiento: string
  cantidad_inicial: number
  producto_id: number
  stock_actual: number
  creado_en: string
}

export interface LoteInput {
  numero_lote: string
  fecha_vencimiento: string
  cantidad_inicial: number
}

export type TipoMovimiento = 'INGRESO' | 'VENTA' | 'DEVOLUCION'

export interface Movimiento {
  id: number
  lote_id: number
  tipo: TipoMovimiento
  cantidad: number
  fecha_hora: string
  evento_uuid: string
}

export interface MovimientoInput {
  cantidad: number
  observacion?: string
}

export type EstadoVencimiento = 'VIGENTE' | 'PROXIMO_A_VENCER' | 'VENCIDO'

export interface LoteEstado {
  id: number
  lote_id: number
  stock_actual: number
  estado: EstadoVencimiento
  dias_para_vencer: number
  actualizado_en: string
}

export type TipoAlerta =
  | 'PROXIMO_A_VENCER_30'
  | 'PROXIMO_A_VENCER_15'
  | 'PROXIMO_A_VENCER_7'
  | 'VENCIDO'

export interface Alerta {
  id: number
  lote_id: number
  tipo_alerta: TipoAlerta
  dias_restantes: number
  generada_en: string
  atendida: boolean
}
