CREATE SCHEMA IF NOT EXISTS inventario;

CREATE UNIQUE INDEX IF NOT EXISTS uk_proveedor_nit
    ON inventario.proveedores (nit);

CREATE UNIQUE INDEX IF NOT EXISTS uk_proveedor_nombre_nit
    ON inventario.proveedores (nombre, nit);

CREATE UNIQUE INDEX IF NOT EXISTS uk_producto_nombre_principio_proveedor
    ON inventario.productos (nombre, principio_activo, proveedor_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_lote_numero_producto
    ON inventario.lotes (numero_lote, producto_id);
