  # FarmaTrack

Sistema distribuido de inventario y vencimientos para farmacias. La solución
contiene dos microservicios Spring Boot y un gateway GraphQL:

| Servicio | Puerto | Responsabilidad |
|---|---:|---|
| `inventario-service` | 8081 | Proveedores, productos, lotes, stock y movimientos |
| `vencimientos-service` | 8082 | Estados de vencimiento y alertas |
| `graphql-gateway` | 8083 | API GraphQL agregada sobre los dos servicios REST |
| `frontend` | 3000 | Interfaz web en React (nginx; proxy únicamente al gateway GraphQL) |

Ambos servicios utilizan Java 21, PostgreSQL 16, Kafka 3.9.1 y un JWT HMAC
compartido. El gateway también valida y reenvía ese JWT; no tiene base de datos.

## Levantar los servicios

Requisito: Podman y `podman-compose`.

```bash
podman-compose up -d --build
```

Los servicios quedan disponibles en:

| Servicio | URL base |
|---|---|
| Inventario | `http://localhost:8081` |
| Vencimientos | `http://localhost:8082` |
| GraphQL | `http://localhost:8083/graphql` |
| GraphiQL (desarrollo) | `http://localhost:8083/graphiql` |
| Frontend | `http://localhost:3000` |

La documentación Swagger está disponible en:

```text
http://localhost:8081/swagger-ui.html
http://localhost:8082/swagger-ui.html
```

El gateway valida el mismo JWT HS256 que los microservicios y reenvía el
encabezado `Authorization` en sus peticiones REST internas. GraphiQL se
habilita en el compose de desarrollo; para detalles y ejemplos consulta
[`graphql-gateway/README.md`](graphql-gateway/README.md).

El frontend envía todas sus operaciones a `http://localhost:3000/api/graphql`;
nginx las reenvía al gateway en el puerto 8083. No se conecta directamente a
los endpoints REST de los microservicios. Después de actualizar el código,
recrear el frontend con `podman-compose up -d --build --force-recreate frontend`
(o `docker compose up -d --build --force-recreate frontend`) para reemplazar
la imagen que ya estuviera ejecutándose. Si cambias el esquema o los resolvers
GraphQL, también debes reconstruir el gateway:
`podman-compose up -d --build --force-recreate graphql-gateway` (o el comando
equivalente con `docker compose`). Recrear solo el frontend no actualiza el
esquema que atiende el gateway.

## Configuración de Postman

Crear un environment llamado, por ejemplo, `FarmaTrack Local`, con estas
variables:

| Variable | Valor inicial |
|---|---|
| `base_inventario` | `http://localhost:8081` |
| `base_vencimientos` | `http://localhost:8082` |
| `jwt_secret` | `dev-only-secret-change-me-32-bytes` |
| `token` | Se genera en el siguiente paso |
| `proveedor_id` | Vacío |
| `producto_id` | Vacío |
| `lote_id` | Vacío |
| `lote_alerta_id` | Vacío |
| `alerta_id` | Vacío |

En las requests protegidas, en **Authorization**, seleccionar:

```text
Type: Bearer Token
Token: {{token}}
```

En requests con body, seleccionar **Body → raw → JSON**. Postman debe enviar
automáticamente:

```http
Content-Type: application/json
Authorization: Bearer {{token}}
```

## Generar el JWT de prueba

No existe todavía un proveedor de identidad. El proyecto incluye un script que
genera un token HS256 compatible con ambos servicios:

```bash
python3 scripts/generate-test-jwt.py 'dev-only-secret-change-me-32-bytes'
```

Copiar el token completo y pegarlo en la variable `token` del environment de
Postman. El token contiene `sub=demo-user` y dura una hora.

## Procedimiento de prueba: inventario-service

Ejecutar las requests en este orden. Los IDs obtenidos de las respuestas se
utilizan en las siguientes requests.

### 1. Crear proveedor

```http
POST {{base_inventario}}/proveedores
```

Body:

```json
{
  "nombre": "Laboratorios Genfar S.A.",
  "nit": "900123456-1",
  "contacto": "ventas@genfar.com"
}
```

Respuesta esperada: `201 Created`.

En **Tests**, guardar el ID:

```javascript
const data = pm.response.json();
pm.environment.set("proveedor_id", data.id);
```

### 2. Listar proveedores

```http
GET {{base_inventario}}/proveedores?page=0&size=20
```

Respuesta esperada: `200 OK`.

### 3. Crear producto

```http
POST {{base_inventario}}/productos
```

Body:

```json
{
  "nombre": "Acetaminofen 500mg x 20",
  "principio_activo": "Acetaminofen",
  "proveedor_id": {{proveedor_id}}
}
```

Usar exactamente `principio_activo` y `proveedor_id`; no utilizar
`principioActivo` ni `proveedorId`.

Respuesta esperada: `201 Created`.

```javascript
const data = pm.response.json();
pm.environment.set("producto_id", data.id);
```

### 4. Listar productos

```http
GET {{base_inventario}}/productos?page=0&size=20
```

Respuesta esperada: `200 OK`.

### 5. Consultar producto

```http
GET {{base_inventario}}/productos/{{producto_id}}
```

Respuesta esperada: `200 OK`.

### 6. Crear lote de inventario

Para una prueba normal, usar una fecha lejana:

```http
POST {{base_inventario}}/productos/{{producto_id}}/lotes
```

Body:

```json
{
  "numero_lote": "L-DEMO-001",
  "fecha_vencimiento": "2027-12-01",
  "cantidad_inicial": 20
}
```

Respuesta esperada: `201 Created`.

En **Tests**:

```javascript
const data = pm.response.json();
pm.environment.set("lote_id", data.id);
```

La creación del lote registra automáticamente un movimiento `INGRESO` y
publica el evento `LOTE_INGRESADO` en Kafka.

### 7. Listar lotes

```http
GET {{base_inventario}}/lotes?page=0&size=20
```

Respuesta esperada: `200 OK`.

### 8. Consultar lote

```http
GET {{base_inventario}}/lotes/{{lote_id}}
```

Respuesta esperada: `200 OK`.

### 9. Consultar movimientos del lote

```http
GET {{base_inventario}}/lotes/{{lote_id}}/movimientos?page=0&size=20
```

Debe aparecer al menos el movimiento inicial `INGRESO`.

### 10. Registrar una venta

```http
POST {{base_inventario}}/lotes/{{lote_id}}/ventas
```

Body:

```json
{
  "cantidad": 5,
  "observacion": "Venta mostrador"
}
```

Respuesta esperada: `201 Created`. El stock del lote disminuye y se publica el
evento `MOVIMIENTO_VENTA`.

### 11. Registrar una devolución

```http
POST {{base_inventario}}/lotes/{{lote_id}}/devoluciones
```

Body:

```json
{
  "cantidad": 2,
  "observacion": "Devolución al proveedor"
}
```

Respuesta esperada: `201 Created`. Se publica el evento
`MOVIMIENTO_DEVOLUCION`.

## Procedimiento de prueba: vencimientos-service

Después de crear un lote o registrar un movimiento, esperar unos segundos para
que `vencimientos-service` consuma el evento Kafka.

### 1. Consultar el estado de un lote

```http
GET {{base_vencimientos}}/lotes/{{lote_id}}/estado
```

Respuesta esperada: `200 OK`.

```json
{
  "id": 1,
  "lote_id": 1,
  "stock_actual": 17,
  "estado": "VIGENTE",
  "dias_para_vencer": 400,
  "actualizado_en": "2026-09-19T..."
}
```

Si Kafka todavía no ha procesado el evento, la respuesta será `404` con el
código `LOTE_NO_SINCRONIZADO`. En ese caso, esperar unos segundos y repetir la
request.

### 2. Listar alertas

```http
GET {{base_vencimientos}}/alertas?atendida=false&page=0&size=20
```

Respuesta esperada: `200 OK`.

Un lote con una fecha lejana no genera alertas. Esto es un comportamiento
esperado porque las alertas se crean cuando faltan 30, 15 o 7 días para vencer,
o cuando el lote ya está vencido.

### 3. Crear un lote para probar alertas

Para provocar alertas, volver a la request de creación de lote del servicio de
inventario y usar una fecha cercana. Por ejemplo, si la fecha actual es
`2026-09-19`, utilizar:

```json
{
  "numero_lote": "L-ALERTA-001",
  "fecha_vencimiento": "2026-09-25",
  "cantidad_inicial": 10
}
```

Guardar el ID de este lote en una variable separada:

```javascript
const data = pm.response.json();
pm.environment.set("lote_alerta_id", data.id);
```

Esperar unos segundos y consultar:

```http
GET {{base_vencimientos}}/lotes/{{lote_alerta_id}}/estado
```

La respuesta debe indicar `estado: "PROXIMO_A_VENCER"` y un valor de
`dias_para_vencer` menor o igual a 7.

Después consultar nuevamente:

```http
GET {{base_vencimientos}}/alertas?atendida=false&page=0&size=20
```

Se pueden generar las alertas `PROXIMO_A_VENCER_30`,
`PROXIMO_A_VENCER_15` y `PROXIMO_A_VENCER_7`. El sistema no crea duplicados
para el mismo lote y tipo de alerta.

En **Tests**, guardar el ID de una alerta:

```javascript
const data = pm.response.json();
if (data.length > 0) {
  pm.environment.set("alerta_id", data[0].id);
}
```

### 4. Consultar una alerta

```http
GET {{base_vencimientos}}/alertas/{{alerta_id}}
```

Respuesta esperada: `200 OK`.

### 5. Marcar una alerta como atendida

```http
PATCH {{base_vencimientos}}/alertas/{{alerta_id}}
```

Body:

```json
{
  "atendida": true
}
```

Respuesta esperada: `200 OK`. Esta operación solo cambia el seguimiento de la
alerta; no modifica el stock ni el estado del lote.

## Pruebas de errores

### Request sin JWT

Enviar cualquier request protegida sin autorización. Resultado esperado:
`401 Unauthorized`.

### JWT inválido o vencido

Usar un token incorrecto o expirado. Resultado esperado:
`401 Unauthorized`.

### Proveedor inexistente

Crear un producto con `"proveedor_id": 999999`. Resultado esperado: `404
Not Found`.

### Venta con stock insuficiente

Enviar a `/lotes/{{lote_id}}/ventas`:

```json
{
  "cantidad": 999999
}
```

Resultado esperado: `422 Unprocessable Entity` con código
`STOCK_INSUFICIENTE`.

### Venta de lote vencido

Crear un lote con una fecha anterior a la fecha actual, por ejemplo:

```json
{
  "numero_lote": "L-VENCIDO-001",
  "fecha_vencimiento": "2020-01-01",
  "cantidad_inicial": 10
}
```

Intentar vender una unidad. Resultado esperado: `409 Conflict` con código
`LOTE_VENCIDO`.

## 13. Decisiones pendientes

- **Gateway GraphQL:** resuelto en esta entrega. El gateway (`graphql-gateway`)
  expone `/graphql` en el puerto 8083, agrega ambos contratos REST, valida y
  reenvía el JWT compartido y no mantiene persistencia propia. Ver
  [`graphql-gateway/README.md`](graphql-gateway/README.md).
