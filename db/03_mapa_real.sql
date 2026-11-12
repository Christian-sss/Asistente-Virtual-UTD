-- =====================================================================
-- Migración: del plano dibujado (coordenadas X/Y en píxeles) al mapa real
-- de OpenStreetMap (latitud y longitud WGS84).
--
-- Solo es necesaria en una base creada ANTES de este cambio. Una base nueva
-- creada con 01_esquema.sql y 02_datos.sql ya tiene las columnas nuevas.
-- Las posiciones quedan APROXIMADAS (centro de la Ciudad Universitaria):
-- ubique cada oficina con un clic en el mapa de «Ubicaciones y canales».
-- =====================================================================
BEGIN;

ALTER TABLE punto_atencion DROP COLUMN IF EXISTS coord_x;
ALTER TABLE punto_atencion DROP COLUMN IF EXISTS coord_y;
ALTER TABLE punto_atencion ADD COLUMN IF NOT EXISTS latitud  NUMERIC(9,6);
ALTER TABLE punto_atencion ADD COLUMN IF NOT EXISTS longitud NUMERIC(9,6);

UPDATE punto_atencion p SET latitud = v.lat, longitud = v.lon
FROM (VALUES ('UTD',   -18.024900, -70.249100),
             ('TES',   -18.024760, -70.248940),
             ('DSARC', -18.024760, -70.249260),
             ('SG',    -18.025040, -70.248940),
             ('DEC',   -18.025040, -70.249260),
             ('SAA',   -18.024900, -70.248800),
             ('EP',    -18.024900, -70.249400)) AS v(sigla, lat, lon)
JOIN dependencia d ON d.sigla = v.sigla
WHERE p.id_dependencia = d.id_dependencia AND p.latitud IS NULL;

COMMIT;
