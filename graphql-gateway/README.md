# graphql-gateway

Gateway GraphQL de FarmaTrack. Expone las lecturas y operaciones de inventario,
además de las consultas y mutaciones de vencimientos, y las integra mediante
REST sin mantener una base de datos propia.

## Inicio y configuración

Desde la raíz del repositorio:

```bash
podman-compose up -d --build
```

GraphiQL (explorador habilitado para desarrollo):  
`http://localhost:8083/graphiql`

El endpoint GraphQL es `http://localhost:8083/graphql`. La configuración de
desarrollo del compose comparte `JWT_SECRET` con los dos microservicios. En
otros entornos GraphiQL está deshabilitado de forma predeterminada
(`GRAPHIQL_ENABLED=false`).

La aplicación usa Spring MVC para atender GraphQL y `WebClient` de Spring
WebFlux para las llamadas REST, ejecutadas de forma bloqueante y acotadas a
cinco segundos. Esto mantiene una sola cadena de seguridad servlet y limita
dependencias; no se usa un servidor WebFlux reactivo.

El gateway valida el JWT HS256 con el mismo secreto, expiración y, cuando se
configuran, issuer y audience que los servicios existentes. Luego reenvía el
encabezado `Authorization` recibido a cada llamada REST. La red interna no se
considera una frontera de autenticación.

## JWT en GraphiQL

En GraphiQL, abre **Headers** e ingresa el encabezado:

```json
{
  "Authorization": "Bearer <JWT>"
}
```

Puedes generar un token local con el script de la raíz:

```bash
python3 scripts/generate-test-jwt.py 'dev-only-secret-change-me-32-bytes'
```

## Ejemplos GraphQL

### Consulta simple de productos

```graphql
query {
  productos(page: 0, size: 20) {
    id
    nombre
    principioActivo
  }
}
```

### Lote con producto, proveedor y estado de vencimiento

```graphql
query {
  lote(id: "501") {
    id
    numeroLote
    fechaVencimiento
    stockActual
    producto {
      nombre
      principioActivo
      proveedor {
        id
        nombre
        nit
      }
    }
    estado {
      loteId
      estado
      diasParaVencer
      actualizadoEn
    }
  }
}
```

### Consulta agregada estrella: lote, estado y alertas relacionadas

```graphql
query {
  loteConAlertas(loteId: "501") {
    lote {
      id
      numeroLote
      fechaVencimiento
      stockActual
      producto {
        nombre
      }
    }
    estado {
      estado
      diasParaVencer
      actualizadoEn
    }
    alertas {
      id
      tipoAlerta
      diasRestantes
      generadaEn
      atendida
    }
  }
}
```

### Marcar una alerta como atendida

```graphql
mutation {
  marcarAlertaAtendida(alertaId: "77", atendida: true) {
    id
    loteId
    tipoAlerta
    atendida
  }
}
```

Las operaciones de alta y edición de proveedores/productos, creación de lotes y
registro de ventas/devoluciones también se exponen como mutaciones GraphQL
(`crearProveedor`, `actualizarProveedor`, `crearProducto`, `actualizarProducto`,
`crearLote`, `registrarVenta` y `registrarDevolucion`). El frontend consume
únicamente este endpoint.

## Errores y consistencia

Los errores REST se convierten en errores GraphQL con
`errors[].extensions.code`, incluyendo códigos remotos como `LOTE_VENCIDO`,
`LOTE_NO_SINCRONIZADO`, `UNAUTHORIZED` y `STOCK_INSUFICIENTE`. Si
`vencimientos-service` devuelve 404 para el estado de un lote, GraphQL devuelve
`estado: null`: el evento Kafka que inicia la sincronización pudo no haber sido
consumido todavía.

El contrato de inventario no tiene endpoint de proveedor individual, por lo
que el lookup de `Producto.proveedor` recorre las páginas de proveedores hasta
encontrar su ID. La resolución de estado de `Lote` realiza una llamada por lote
(N+1 en listas grandes); los servicios REST no ofrecen una operación por lotes,
por lo que se puede optimizar más adelante con una API de consulta masiva.
