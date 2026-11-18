import { clearToken, currentToken } from '../auth'
import type {
  Alerta,
  EstadoVencimiento,
  Lote,
  LoteEstado,
  LoteInput,
  Movimiento,
  MovimientoInput,
  Producto,
  ProductoInput,
  Proveedor,
  ProveedorInput,
} from './types'

const GRAPHQL = '/api/graphql'

export class ApiError extends Error {
  status: number
  codigo?: string

  constructor(status: number, message: string, codigo?: string) {
    super(message)
    this.status = status
    this.codigo = codigo
  }
}

export const UNAUTHORIZED_EVENT = 'farmatrack:unauthorized'

interface GraphqlError {
  message: string
  extensions?: { code?: string; httpStatus?: number }
}

interface GraphqlResponse<T> {
  data?: T
  errors?: GraphqlError[]
}

async function graphql<T>(query: string, variables: Record<string, unknown> = {}): Promise<T> {
  const token = currentToken()
  let response: Response
  try {
    response = await fetch(GRAPHQL, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({ query, variables }),
    })
  } catch {
    throw new ApiError(0, 'No hay conexión con el servidor.', 'SIN_CONEXION')
  }

  if (response.status === 401) {
    clearToken()
    window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    throw new ApiError(401, 'La sesión no es válida o ya expiró.', 'NO_AUTORIZADO')
  }
  if (!response.ok) {
    throw new ApiError(
      response.status,
      response.status >= 500 ? 'El gateway respondió con un error interno.' : `Error ${response.status}`,
    )
  }

  const result = (await response.json()) as GraphqlResponse<T>
  if (result.errors?.length) {
    const error = result.errors[0]
    const code = error.extensions?.code
    if (code === 'UNAUTHORIZED') {
      clearToken()
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    }
    throw new ApiError(
      error.extensions?.httpStatus ?? (code === 'UNAUTHORIZED' ? 401 : 502),
      error.message,
      code,
    )
  }
  if (!result.data) throw new ApiError(502, 'El gateway respondió sin datos.')
  return result.data
}

interface GatewayProveedor {
  id: string
  nombre: string
  nit: string
  contacto: string | null
}

interface GatewayProducto {
  id: string
  nombre: string
  principioActivo: string
  proveedorId: string
}

interface GatewayLote {
  id: string
  numeroLote: string
  fechaVencimiento: string
  cantidadInicial: number
  productoId: string
  stockActual: number
  creadoEn: string
}

interface GatewayMovimiento {
  id: string
  loteId: string
  tipo: Movimiento['tipo']
  cantidad: number
  fechaHora: string
  eventoUuid: string
}

interface GatewayEstado {
  id: string
  loteId: string
  stockActual: number
  estado: EstadoVencimiento
  diasParaVencer: number
  actualizadoEn: string
}

interface GatewayAlerta {
  id: string
  loteId: string
  tipoAlerta: Alerta['tipo_alerta']
  diasRestantes: number
  generadaEn: string
  atendida: boolean
}

interface GatewayLoteDetalle extends GatewayLote {
  producto: (GatewayProducto & {
    proveedor: GatewayProveedor | null
  }) | null
  estado: GatewayEstado | null
}

const mapProveedor = (value: GatewayProveedor): Proveedor => ({
  id: Number(value.id),
  nombre: value.nombre,
  nit: value.nit,
  contacto: value.contacto,
})

const mapProducto = (value: GatewayProducto): Producto => ({
  id: Number(value.id),
  nombre: value.nombre,
  principio_activo: value.principioActivo,
  proveedor_id: Number(value.proveedorId),
})

const mapLote = (value: GatewayLote): Lote => ({
  id: Number(value.id),
  numero_lote: value.numeroLote,
  fecha_vencimiento: value.fechaVencimiento,
  cantidad_inicial: value.cantidadInicial,
  producto_id: Number(value.productoId),
  stock_actual: value.stockActual,
  creado_en: value.creadoEn,
})

const mapMovimiento = (value: GatewayMovimiento): Movimiento => ({
  id: Number(value.id),
  lote_id: Number(value.loteId),
  tipo: value.tipo,
  cantidad: value.cantidad,
  fecha_hora: value.fechaHora,
  evento_uuid: value.eventoUuid,
})

const mapEstado = (value: GatewayEstado): LoteEstado => ({
  id: Number(value.id),
  lote_id: Number(value.loteId),
  stock_actual: value.stockActual,
  estado: value.estado,
  dias_para_vencer: value.diasParaVencer,
  actualizado_en: value.actualizadoEn,
})

const mapAlerta = (value: GatewayAlerta): Alerta => ({
  id: Number(value.id),
  lote_id: Number(value.loteId),
  tipo_alerta: value.tipoAlerta,
  dias_restantes: value.diasRestantes,
  generada_en: value.generadaEn,
  atendida: value.atendida,
})

async function allPages<T, G>(
  field: string,
  selection: string,
  map: (value: G) => T,
  argumentsList = '',
  variables: Record<string, unknown> = {},
  argumentDefinitions = '',
): Promise<T[]> {
  const result: T[] = []
  for (let page = 0; page < 100; page++) {
    const data = await graphql<Record<string, G[]>>(
      `query Pagina(${argumentDefinitions}$page: Int!, $size: Int!) {
        ${field}(${argumentsList}page: $page, size: $size) { ${selection} }
      }`,
      { ...variables, page, size: 100 },
    )
    const rows = data[field]
    result.push(...rows.map(map))
    if (rows.length < 100) break
  }
  return result
}

function proveedorInput(input: ProveedorInput) {
  return { nombre: input.nombre, nit: input.nit, contacto: input.contacto ?? null }
}

function productoInput(input: ProductoInput) {
  return {
    nombre: input.nombre,
    principioActivo: input.principio_activo,
    proveedorId: String(input.proveedor_id),
  }
}

function movimientoInput(input: MovimientoInput) {
  return { cantidad: input.cantidad, observacion: input.observacion ?? null }
}

async function guardarProveedor(id: number | undefined, input: ProveedorInput): Promise<Proveedor> {
  const data = await graphql<{ proveedor: GatewayProveedor }>(
    id === undefined
      ? `mutation CrearProveedor($input: ProveedorInput!) {
          proveedor: crearProveedor(input: $input) { id nombre nit contacto }
        }`
      : `mutation ActualizarProveedor($id: ID!, $input: ProveedorInput!) {
          proveedor: actualizarProveedor(id: $id, input: $input) { id nombre nit contacto }
        }`,
    { ...(id === undefined ? {} : { id: String(id) }), input: proveedorInput(input) },
  )
  return mapProveedor(data.proveedor)
}

async function guardarProducto(id: number | undefined, input: ProductoInput): Promise<Producto> {
  const data = await graphql<{ producto: GatewayProducto }>(
    id === undefined
      ? `mutation CrearProducto($input: ProductoInput!) {
          producto: crearProducto(input: $input) { id nombre principioActivo proveedorId }
        }`
      : `mutation ActualizarProducto($id: ID!, $input: ProductoInput!) {
          producto: actualizarProducto(id: $id, input: $input) {
            id nombre principioActivo proveedorId
          }
        }`,
    { ...(id === undefined ? {} : { id: String(id) }), input: productoInput(input) },
  )
  return mapProducto(data.producto)
}

export const gateway = {
  proveedores: () => allPages('proveedores', 'id nombre nit contacto', mapProveedor),
  crearProveedor: (input: ProveedorInput) => guardarProveedor(undefined, input),
  editarProveedor: (id: number, input: ProveedorInput) => guardarProveedor(id, input),

  productos: () =>
    allPages('productos', 'id nombre principioActivo proveedorId', mapProducto),
  producto: async (id: number) => {
    const data = await graphql<{ producto: GatewayProducto | null }>(
      `query Producto($id: ID!) {
        producto(id: $id) { id nombre principioActivo proveedorId }
      }`,
      { id: String(id) },
    )
    if (!data.producto) throw new ApiError(404, 'No se encontró el producto.', 'NOT_FOUND')
    return mapProducto(data.producto)
  },
  crearProducto: (input: ProductoInput) => guardarProducto(undefined, input),
  editarProducto: (id: number, input: ProductoInput) => guardarProducto(id, input),

  lotes: () =>
    allPages(
      'lotes',
      'id numeroLote fechaVencimiento cantidadInicial productoId stockActual creadoEn',
      mapLote,
    ),
  lote: async (id: number) => {
    const data = await graphql<{ lote: GatewayLote | null }>(
      `query Lote($id: ID!) {
        lote(id: $id) {
          id numeroLote fechaVencimiento cantidadInicial productoId stockActual creadoEn
        }
      }`,
      { id: String(id) },
    )
    if (!data.lote) throw new ApiError(404, 'No se encontró el lote.', 'NOT_FOUND')
    return mapLote(data.lote)
  },
  crearLote: async (productoId: number, input: LoteInput) => {
    const data = await graphql<{ crearLote: GatewayLote }>(
      `mutation CrearLote($productoId: ID!, $input: LoteInput!) {
        crearLote(productoId: $productoId, input: $input) {
          id numeroLote fechaVencimiento cantidadInicial productoId stockActual creadoEn
        }
      }`,
      {
        productoId: String(productoId),
        input: {
          numeroLote: input.numero_lote,
          fechaVencimiento: input.fecha_vencimiento,
          cantidadInicial: input.cantidad_inicial,
        },
      },
    )
    return mapLote(data.crearLote)
  },

  movimientos: (loteId: number) =>
    allPages(
      'movimientos',
      'id loteId tipo cantidad fechaHora eventoUuid',
      mapMovimiento,
      'loteId: $loteId, ',
      { loteId: String(loteId) },
      '$loteId: ID!, ',
    ),
  vender: async (loteId: number, input: MovimientoInput) => {
    const data = await graphql<{ registrarVenta: GatewayMovimiento }>(
      `mutation RegistrarVenta($loteId: ID!, $input: MovimientoInput!) {
        registrarVenta(loteId: $loteId, input: $input) {
          id loteId tipo cantidad fechaHora eventoUuid
        }
      }`,
      { loteId: String(loteId), input: movimientoInput(input) },
    )
    return mapMovimiento(data.registrarVenta)
  },
  devolver: async (loteId: number, input: MovimientoInput) => {
    const data = await graphql<{ registrarDevolucion: GatewayMovimiento }>(
      `mutation RegistrarDevolucion($loteId: ID!, $input: MovimientoInput!) {
        registrarDevolucion(loteId: $loteId, input: $input) {
          id loteId tipo cantidad fechaHora eventoUuid
        }
      }`,
      { loteId: String(loteId), input: movimientoInput(input) },
    )
    return mapMovimiento(data.registrarDevolucion)
  },

  estado: async (loteId: number) => {
    const data = await graphql<{ estado: GatewayEstado | null }>(
      `query Estado($loteId: ID!) {
        estado(loteId: $loteId) { id loteId stockActual estado diasParaVencer actualizadoEn }
      }`,
      { loteId: String(loteId) },
    )
    if (!data.estado) throw new ApiError(404, 'No se encontró el estado del lote.', 'LOTE_NO_SINCRONIZADO')
    return mapEstado(data.estado)
  },
  alertas: async (): Promise<Alerta[]> => {
    const values = await allPages<Alerta, GatewayAlerta>(
      'alertas',
      'id loteId tipoAlerta diasRestantes generadaEn atendida',
      mapAlerta,
    )
    return values
  },
  marcarAlerta: async (id: number, atendida: boolean) => {
    const data = await graphql<{ marcarAlertaAtendida: GatewayAlerta | null }>(
      `mutation MarcarAlerta($id: ID!, $atendida: Boolean!) {
        marcarAlertaAtendida(alertaId: $id, atendida: $atendida) {
          id loteId tipoAlerta diasRestantes generadaEn atendida
        }
      }`,
      { id: String(id), atendida },
    )
    if (!data.marcarAlertaAtendida) {
      throw new ApiError(404, 'No se encontró la alerta.', 'NOT_FOUND')
    }
    return mapAlerta(data.marcarAlertaAtendida)
  },

  detalleLote: async (id: number) => {
    const data = await graphql<{ lote: GatewayLoteDetalle | null }>(
      `query DetalleLote($id: ID!) {
        lote(id: $id) {
          id numeroLote fechaVencimiento cantidadInicial productoId stockActual creadoEn
          producto {
            id nombre principioActivo proveedorId
            proveedor { id nombre nit contacto }
          }
          estado { id loteId stockActual estado diasParaVencer actualizadoEn }
        }
      }`,
      { id: String(id) },
    )
    if (!data.lote) throw new ApiError(404, 'No se encontró el lote.', 'NOT_FOUND')

    const lote = data.lote
    return {
      lote: mapLote(lote),
      producto: lote.producto ? mapProducto(lote.producto) : null,
      proveedor: lote.producto?.proveedor ? mapProveedor(lote.producto.proveedor) : null,
      estado: lote.estado ? mapEstado(lote.estado) : null,
    }
  },
}
