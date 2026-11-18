import type { EstadoVencimiento, TipoAlerta, TipoMovimiento } from '../api/types'

const DAY = 86_400_000

/** "2027-03-14" → Date local (sin el corrimiento de zona horaria de new Date(str)). */
export function parseDate(iso: string): Date {
  const [y, m, d] = iso.slice(0, 10).split('-').map(Number)
  return new Date(y, m - 1, d)
}

export function today(): Date {
  const n = new Date()
  return new Date(n.getFullYear(), n.getMonth(), n.getDate())
}

export function daysUntil(iso: string): number {
  return Math.round((parseDate(iso).getTime() - today().getTime()) / DAY)
}

/** Misma regla que VencimientoCalculator en el servicio. */
export function estadoPorDias(dias: number): EstadoVencimiento {
  if (dias < 0) return 'VENCIDO'
  if (dias <= 30) return 'PROXIMO_A_VENCER'
  return 'VIGENTE'
}

const fecha = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short', year: 'numeric' })
const fechaCorta = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short' })
const fechaHora = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'short',
  hour: 'numeric',
  minute: '2-digit',
})
const numero = new Intl.NumberFormat('es-CO')

export const fmtFecha = (iso: string) => fecha.format(iso.length === 10 ? parseDate(iso) : new Date(iso)).replace('.', '')
export const fmtFechaCorta = (d: Date) => fechaCorta.format(d).replace('.', '')
export const fmtFechaHora = (iso: string) => fechaHora.format(new Date(iso)).replace('.', '')
export const fmtNum = (n: number) => numero.format(n)

export function fmtDias(dias: number): string {
  if (dias === 0) return 'vence hoy'
  if (dias === 1) return 'mañana'
  if (dias === -1) return 'venció ayer'
  if (dias < 0) return `venció hace ${fmtNum(-dias)} días`
  return `en ${fmtNum(dias)} días`
}

export const estadoLabel: Record<EstadoVencimiento, string> = {
  VIGENTE: 'Vigente',
  PROXIMO_A_VENCER: 'Por vencer',
  VENCIDO: 'Vencido',
}

export const alertaLabel: Record<TipoAlerta, string> = {
  PROXIMO_A_VENCER_30: '30 días',
  PROXIMO_A_VENCER_15: '15 días',
  PROXIMO_A_VENCER_7: '7 días',
  VENCIDO: 'Vencido',
}

export const movimientoLabel: Record<TipoMovimiento, string> = {
  INGRESO: 'Ingreso',
  VENTA: 'Venta',
  DEVOLUCION: 'Devolución',
}

export type Tono = 'ok' | 'aviso' | 'warn' | 'danger' | 'muted'

export const estadoTono: Record<EstadoVencimiento, Tono> = {
  VIGENTE: 'ok',
  PROXIMO_A_VENCER: 'warn',
  VENCIDO: 'danger',
}

export function alertaTono(tipo: TipoAlerta): Tono {
  if (tipo === 'VENCIDO') return 'danger'
  if (tipo === 'PROXIMO_A_VENCER_30') return 'aviso'
  return 'warn'
}
