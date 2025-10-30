-- =====================================================
-- DATOS INICIALES
-- =====================================================

-- Tipos de IVA
INSERT INTO Tipo_IVA (nombre, porcentaje) VALUES
('General', 21.00),
('Reducido', 10.00),
('Superreducido', 4.00),
('Exento', 0.00);

-- Empresa por defecto (ejemplo)
INSERT INTO Empresa (
  nombre_comercial, 
  razon_social, 
  nif, 
  direccion, 
  codigo_postal, 
  ciudad, 
  provincia,
  email,
  telefono,
  por_defecto
) VALUES (
  'Mi Empresa',
  'MI EMPRESA S.L.',
  'B00000000',
  'Calle Principal, 1',
  '03000',
  'Alicante',
  'Alicante',
  'contacto@miempresa.com',
  '965000000',
  TRUE
);