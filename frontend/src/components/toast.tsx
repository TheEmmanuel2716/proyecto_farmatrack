import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'

type Kind = 'ok' | 'error'
interface Toast {
  id: number
  text: string
  kind: Kind
}

const Ctx = createContext<(text: string, kind?: Kind) => void>(() => {})

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<Toast[]>([])

  const push = useCallback((text: string, kind: Kind = 'ok') => {
    const id = Date.now() + Math.random()
    setItems((prev) => [...prev.slice(-2), { id, text, kind }])
    window.setTimeout(() => setItems((prev) => prev.filter((t) => t.id !== id)), 4200)
  }, [])

  return (
    <Ctx.Provider value={push}>
      {children}
      <div className="toasts" aria-live="polite">
        {items.map((t) => (
          <div key={t.id} className={`toast toast--${t.kind}`}>
            {t.text}
          </div>
        ))}
      </div>
    </Ctx.Provider>
  )
}

export const useToast = () => useContext(Ctx)
