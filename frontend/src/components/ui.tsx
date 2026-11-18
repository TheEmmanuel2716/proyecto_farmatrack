import { useEffect, useId, useRef, type ReactNode } from 'react'
import type { Tono } from '../lib/format'
import { ApiError } from '../api/client'

export function Tag({ tono, children }: { tono: Tono; children: ReactNode }) {
  return <span className={`tag tag--${tono}`}>{children}</span>
}

export function PageHead({
  title,
  lead,
  actions,
}: {
  title: string
  lead?: ReactNode
  actions?: ReactNode
}) {
  return (
    <header className="page-head">
      <div>
        <h1>{title}</h1>
        {lead && <p className="page-lead">{lead}</p>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </header>
  )
}

export function Field({
  label,
  hint,
  children,
}: {
  label: string
  hint?: string
  children: (id: string) => ReactNode
}) {
  const id = useId()
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      {children(id)}
      {hint && <small>{hint}</small>}
    </div>
  )
}

export function FormError({ error }: { error: unknown }) {
  if (!error) return null
  const msg = error instanceof Error ? error.message : 'Algo salió mal.'
  const code = error instanceof ApiError ? error.codigo : undefined
  return (
    <div className="form-error" role="alert">
      {code && <code>{code}</code>}
      <span>{msg}</span>
    </div>
  )
}

export function Loading({ label = 'Cargando' }: { label?: string }) {
  return (
    <div className="state state--loading" aria-live="polite">
      <span className="spinner" aria-hidden /> {label}…
    </div>
  )
}

export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const msg = error instanceof Error ? error.message : 'No se pudo cargar la información.'
  return (
    <div className="state state--error">
      <p>{msg}</p>
      {onRetry && (
        <button className="btn btn--ghost btn--sm" onClick={onRetry}>
          Reintentar
        </button>
      )}
    </div>
  )
}

export function Empty({ title, children }: { title: string; children?: ReactNode }) {
  return (
    <div className="state state--empty">
      <p className="state-title">{title}</p>
      {children && <div className="state-body">{children}</div>}
    </div>
  )
}

/** Panel lateral para formularios. Se cierra con Escape o clic fuera. */
export function Sheet({
  open,
  title,
  onClose,
  children,
}: {
  open: boolean
  title: string
  onClose: () => void
  children: ReactNode
}) {
  const ref = useRef<HTMLDivElement>(null)
  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose()
    document.addEventListener('keydown', onKey)
    const first = ref.current?.querySelector<HTMLElement>('input, select, textarea')
    first?.focus()
    return () => document.removeEventListener('keydown', onKey)
  }, [open, onClose])

  if (!open) return null
  return (
    <div className="sheet-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="sheet" role="dialog" aria-modal="true" aria-label={title} ref={ref}>
        <div className="sheet-head">
          <h2>{title}</h2>
          <button className="icon-btn" onClick={onClose} aria-label="Cerrar">
            <svg viewBox="0 0 20 20" width="18" height="18" aria-hidden>
              <path d="M5 5l10 10M15 5L5 15" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
            </svg>
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

/** Barra de stock restante respecto a la cantidad inicial. */
export function StockBar({ actual, inicial }: { actual: number; inicial: number }) {
  const pct = inicial > 0 ? Math.max(0, Math.min(100, (actual / inicial) * 100)) : 0
  return (
    <span className="stockbar" aria-hidden>
      <span style={{ width: `${pct}%` }} />
    </span>
  )
}
