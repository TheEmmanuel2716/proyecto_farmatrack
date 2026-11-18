const KEY = 'farmatrack.token'

export function getToken(): string | null {
  try {
    return localStorage.getItem(KEY)
  } catch {
    return null
  }
}

export function setToken(token: string) {
  try {
    localStorage.setItem(KEY, token)
  } catch {
    /* modo privado: la sesión dura lo que dure la pestaña */
  }
  memoryToken = token
}

export function clearToken() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    /* nada que limpiar */
  }
  memoryToken = null
}

// Respaldo por si localStorage no está disponible.
let memoryToken: string | null = null
export function currentToken(): string | null {
  return getToken() ?? memoryToken
}

export interface Claims {
  sub?: string
  exp?: number
  iat?: number
}

function b64urlDecode(value: string): string {
  const pad = value.length % 4 === 0 ? '' : '='.repeat(4 - (value.length % 4))
  const b64 = (value + pad).replace(/-/g, '+').replace(/_/g, '/')
  const bin = atob(b64)
  const bytes = Uint8Array.from(bin, (c) => c.charCodeAt(0))
  return new TextDecoder().decode(bytes)
}

export function readClaims(token: string): Claims | null {
  const parts = token.trim().split('.')
  if (parts.length !== 3) return null
  try {
    return JSON.parse(b64urlDecode(parts[1])) as Claims
  } catch {
    return null
  }
}

export function isUsable(token: string | null): boolean {
  if (!token) return false
  const claims = readClaims(token)
  if (!claims) return false
  if (!claims.exp) return true
  return claims.exp * 1000 > Date.now() + 5_000
}
