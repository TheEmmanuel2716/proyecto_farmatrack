# FarmaTrack — frontend

React 19 + Vite + TypeScript, con pnpm. Toda la comunicación de datos pasa por
el gateway GraphQL; el gateway es quien integra los microservicios.

## Con Docker (todo el sistema)

Desde la raíz del proyecto:

```bash
podman-compose up -d --build     # o: docker compose up -d --build
```

Abrir http://localhost:3000.

El contenedor nginx sirve la app compilada y reenvía únicamente
`/api/graphql` a `graphql-gateway:8083`. El navegador no se conecta directamente
a los microservicios ni necesita CORS.

## Solo el frontend (desarrollo)

Con los servicios corriendo y el gateway disponible en el puerto 8083:

```bash
cd frontend
pnpm install
pnpm dev
```

Abre http://localhost:5173. Vite reenvía GraphQL al gateway. Si el gateway está
en otro host, crear `frontend/.env.local`:

```
GATEWAY_URL=http://otro-host:8083
```

## Acceso

No hay proveedor de identidad todavía. La pantalla de entrada solicita un JWT
generado localmente:

- Ejecutar desde la raíz `python3 scripts/generate-test-jwt.py '<JWT_SECRET>'`.
- Pegar en la pantalla de acceso el JWT que imprime el script.

El secreto no se introduce ni se firma en el navegador.

Si el token vence o un servicio responde 401 a través del gateway, la app vuelve
a esta pantalla.

## Estructura

```
src/
  api/        cliente HTTP, tipos y hooks de React Query
  components/ layout, panel lateral, etiquetas, avisos
  lib/        formato de fechas y estado de vencimiento
  pages/      Resumen, Lotes, Detalle de lote, Alertas, Productos, Proveedores, Acceso
```

El estado de cada lote en los listados se calcula en el navegador con la misma
regla del `VencimientoCalculator` (vencido < 0 días, por vencer ≤ 30). Las
consultas y mutaciones de todas las pantallas usan el endpoint GraphQL del
gateway.
