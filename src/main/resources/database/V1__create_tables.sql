-- =====================================================
-- SCRIPT DE CREACIÓN DE TABLAS BASE - H2 Database
-- Aplicación: Facturación
-- =====================================================

-- ======================
-- Tabla: Empresa
-- ======================
CREATE TABLE IF NOT EXISTS Empresa (
  id_empresa INT PRIMARY KEY AUTO_INCREMENT,
  nombre_comercial VARCHAR(200) NOT NULL,
  razon_social VARCHAR(200) NOT NULL,
  nif VARCHAR(20) NOT NULL UNIQUE,
  direccion VARCHAR(255) NOT NULL,
  telefono VARCHAR(20),
  email VARCHAR(100),
  tipo_retencion_irpf DECIMAL(5,2) DEFAULT 15.00,
  por_defecto BOOLEAN DEFAULT FALSE
);

-- ======================
-- Tabla: Cliente
-- ======================
CREATE TABLE IF NOT EXISTS Cliente (
  id_cliente INT PRIMARY KEY AUTO_INCREMENT,
  nombre_fiscal VARCHAR(255) NOT NULL,
  nif VARCHAR(20) NOT NULL UNIQUE,
  direccion VARCHAR(255),
  telefono VARCHAR(20),
  email VARCHAR(100)
);

-- ======================
-- Tabla: Producto
-- ======================
CREATE TABLE IF NOT EXISTS Producto (
  id_producto INT PRIMARY KEY AUTO_INCREMENT,
  tipo_iva DECIMAL(5,2) DEFAULT 21.00,
  codigo VARCHAR(50) UNIQUE,
  nombre VARCHAR(255) NOT NULL,
  precio DECIMAL(10,2),
  precio_base DECIMAL(10,2),
  tipo_retencion DECIMAL(5,2) DEFAULT 0,
  CONSTRAINT chk_precio_no_nulo CHECK (
    precio IS NOT NULL OR precio_base IS NOT NULL
  ),
  CONSTRAINT chk_valores_validos CHECK (
    (precio IS NULL OR precio >= 0)
    AND (precio_base IS NULL OR precio_base >= 0)
    AND tipo_retencion BETWEEN 0 AND 100
    AND tipo_iva BETWEEN 0 AND 100
  )
);

-- ======================
-- Tabla: Factura
-- ======================
CREATE TABLE IF NOT EXISTS Factura (
  id_factura VARCHAR(20) PRIMARY KEY NOT NULL,
  id_empresa INT NOT NULL,
  id_cliente INT NOT NULL,
  fecha_emision DATE NOT NULL DEFAULT CURRENT_DATE,
  metodo_pago VARCHAR(50) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
  total_iva DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (total_iva >= 0),
  total_retencion DECIMAL(10,2) DEFAULT 0 CHECK (total_retencion >= 0),
  total DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
  FOREIGN KEY (id_empresa) REFERENCES Empresa(id_empresa)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  FOREIGN KEY (id_cliente) REFERENCES Cliente(id_cliente)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

-- ======================
-- Tabla: Linea_factura
-- ======================
CREATE TABLE IF NOT EXISTS Linea_factura (
  id_linea INT PRIMARY KEY AUTO_INCREMENT,
  id_factura VARCHAR(20) NOT NULL,
  cantidad DECIMAL(10,2) NOT NULL CHECK (cantidad > 0),
  precio_base DECIMAL(10,2) NOT NULL CHECK (precio_base >= 0),
  precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
  descuento DECIMAL(5,2) DEFAULT 0 CHECK (descuento >= 0 AND descuento <= 100),
  subtotal_linea DECIMAL(10,2) NOT NULL CHECK (subtotal_linea >= 0),
  porcentaje_iva DECIMAL(5,2) NOT NULL CHECK (porcentaje_iva >= 0),
  importe_iva DECIMAL(10,2) NOT NULL CHECK (importe_iva >= 0),
  porcentaje_retencion DECIMAL(5,2) DEFAULT 0 CHECK (porcentaje_retencion >= 0),
  importe_retencion DECIMAL(10,2) DEFAULT 0 CHECK (importe_retencion >= 0),
  total_linea DECIMAL(10,2) NOT NULL CHECK (total_linea >= 0),
  numero_linea INT NOT NULL,
  FOREIGN KEY (id_factura) REFERENCES Factura(id_factura)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

-- ======================
-- Índices recomendados
-- ======================
CREATE INDEX IF NOT EXISTS idx_factura_empresa ON Factura(id_empresa);
CREATE INDEX IF NOT EXISTS idx_factura_cliente ON Factura(id_cliente);
CREATE INDEX IF NOT EXISTS idx_factura_fecha ON Factura(fecha_emision);
CREATE INDEX IF NOT EXISTS idx_linea_factura_factura ON Linea_factura(id_factura);