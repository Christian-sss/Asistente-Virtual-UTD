-- =====================================================================
-- Asistente Virtual Web de Gestión y Orientación de Trámites
-- Documentarios - UTD UNJBG
-- Esquema de base de datos (PostgreSQL) - Versión 2.0: RF-01 a RF-17
-- =====================================================================

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

-- ---------------------------------------------------------------------
-- Base de conocimiento
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id_categoria   SERIAL PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL UNIQUE,
    descripcion    VARCHAR(300),
    icono          VARCHAR(40)  NOT NULL DEFAULT 'bi-folder',
    orden          SMALLINT     NOT NULL DEFAULT 0
);

CREATE TABLE dependencia (
    id_dependencia SERIAL PRIMARY KEY,
    nombre         VARCHAR(150) NOT NULL UNIQUE,
    sigla          VARCHAR(20),
    funciones      VARCHAR(400)
);

CREATE TABLE punto_atencion (
    id_punto       SERIAL PRIMARY KEY,
    id_dependencia INTEGER      NOT NULL REFERENCES dependencia,
    nombre         VARCHAR(150) NOT NULL,
    sede           VARCHAR(80)  NOT NULL,
    direccion      VARCHAR(150) NOT NULL,
    horario        VARCHAR(120) NOT NULL,
    telefono       VARCHAR(200),
    correo         VARCHAR(400),
    referencia     VARCHAR(200),                 -- pabellón, piso o ventanilla
    latitud        NUMERIC(9,6),                 -- ubicación real en el mapa (WGS84, OpenStreetMap) (RF-09, RF-14)
    longitud       NUMERIC(9,6),
    fecha_ultima_verificacion DATE NOT NULL DEFAULT CURRENT_DATE
);

-- Portales, mesa de partes virtual, correos y plataformas de cada dependencia (RF-09, RF-14)
CREATE TABLE canal_digital (
    id_canal       SERIAL PRIMARY KEY,
    id_dependencia INTEGER      NOT NULL REFERENCES dependencia ON DELETE CASCADE,
    tipo           VARCHAR(30)  NOT NULL
                   CHECK (tipo IN ('PORTAL', 'MESA_PARTES_VIRTUAL', 'CORREO', 'PLATAFORMA', 'TELEFONO', 'RED_SOCIAL')),
    nombre         VARCHAR(150) NOT NULL,
    valor          VARCHAR(300) NOT NULL,         -- URL, correo o número
    descripcion    VARCHAR(300),
    fecha_ultima_verificacion DATE NOT NULL DEFAULT CURRENT_DATE
);

-- Reglas de derivación: palabras clave que llevan a la dependencia competente (RF-10, RF-14, RN-13)
CREATE TABLE regla_derivacion (
    id_regla_derivacion SERIAL PRIMARY KEY,
    id_dependencia INTEGER      NOT NULL REFERENCES dependencia ON DELETE CASCADE,
    tema           VARCHAR(150) NOT NULL,
    palabras_clave VARCHAR(400) NOT NULL,         -- separadas por comas
    indicacion     VARCHAR(400),
    activa         BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_ultima_verificacion DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE tramite (
    id_tramite                 SERIAL PRIMARY KEY,
    id_categoria               INTEGER      NOT NULL REFERENCES categoria,
    id_punto                   INTEGER      REFERENCES punto_atencion ON DELETE SET NULL,
    codigo                     VARCHAR(20)  NOT NULL UNIQUE,
    codigo_tupa                VARCHAR(20),
    nombre                     VARCHAR(200) NOT NULL,
    descripcion                TEXT,
    dirigido_a                 VARCHAR(150),
    derecho_pago               NUMERIC(8,2),
    detalle_pago               VARCHAR(200),
    plazo_referencial          VARCHAR(120),
    calificacion               VARCHAR(150),
    -- Ciclo de validación (RF-15, RN-10, RN-11): toda modificación queda PENDIENTE hasta que
    -- el personal de la UTD la aprueba (PUBLICADO) o la devuelve con observaciones (DEVUELTO).
    estado_contenido           VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                               CHECK (estado_contenido IN ('PENDIENTE', 'PUBLICADO', 'DEVUELTO')),
    -- Visible al público. Mientras hay cambios pendientes se muestra la última versión publicada.
    publicado                  BOOLEAN      NOT NULL DEFAULT FALSE,
    version_publicada          BYTEA,
    fecha_publicacion          TIMESTAMP,
    observacion_validacion     VARCHAR(600),
    fecha_ultima_verificacion  DATE         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE paso (
    id_paso        SERIAL PRIMARY KEY,
    id_tramite     INTEGER      NOT NULL REFERENCES tramite ON DELETE CASCADE,
    orden          SMALLINT     NOT NULL,
    descripcion    VARCHAR(400) NOT NULL,
    es_pago_previo BOOLEAN      NOT NULL DEFAULT FALSE,
    id_punto       INTEGER      REFERENCES punto_atencion ON DELETE SET NULL  -- secuencia de visita (RF-09)
);

CREATE TABLE pregunta_guia (
    id_pregunta    SERIAL PRIMARY KEY,
    texto          VARCHAR(250) NOT NULL,
    es_inicial     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE opcion_respuesta (
    id_opcion            SERIAL PRIMARY KEY,
    id_pregunta          INTEGER      NOT NULL REFERENCES pregunta_guia ON DELETE CASCADE,
    texto                VARCHAR(250) NOT NULL,
    id_pregunta_siguiente INTEGER     REFERENCES pregunta_guia ON DELETE SET NULL,
    id_tramite           INTEGER      REFERENCES tramite ON DELETE SET NULL,
    orden                SMALLINT     NOT NULL DEFAULT 0
);

CREATE TABLE requisito (
    id_requisito   SERIAL PRIMARY KEY,
    id_tramite     INTEGER      NOT NULL REFERENCES tramite ON DELETE CASCADE,
    orden          SMALLINT     NOT NULL DEFAULT 0,
    descripcion    TEXT         NOT NULL,
    obligatorio    BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE normativa (
    id_normativa   SERIAL PRIMARY KEY,
    tipo           VARCHAR(60)  NOT NULL,
    numero         VARCHAR(60)  NOT NULL,
    titulo         VARCHAR(250) NOT NULL,
    organo_emisor  VARCHAR(150) NOT NULL,
    fecha_aprobacion DATE,
    vigente        BOOLEAN      NOT NULL DEFAULT TRUE,
    fuente_oficial VARCHAR(300),
    fecha_ultima_verificacion DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE articulo_normativo (
    id_articulo    SERIAL PRIMARY KEY,
    id_normativa   INTEGER      NOT NULL REFERENCES normativa ON DELETE CASCADE,
    numero         VARCHAR(40)  NOT NULL,
    contenido      TEXT         NOT NULL
);

CREATE TABLE requisito_articulo (
    id_requisito   INTEGER NOT NULL REFERENCES requisito ON DELETE CASCADE,
    id_articulo    INTEGER NOT NULL REFERENCES articulo_normativo ON DELETE CASCADE,
    PRIMARY KEY (id_requisito, id_articulo)
);

CREATE TABLE regla_revision (
    id_regla           SERIAL PRIMARY KEY,
    id_tramite         INTEGER      NOT NULL REFERENCES tramite ON DELETE CASCADE,
    criterio           VARCHAR(200) NOT NULL,
    tipo               VARCHAR(30)  NOT NULL
                       CHECK (tipo IN ('DATO_OBLIGATORIO', 'CONFIRMACION_ASISTIDA')),
    expresion          VARCHAR(300),
    mensaje_correccion VARCHAR(300) NOT NULL,
    activa             BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- Revisión preliminar (datos temporales, se eliminan a las 24 horas)
-- ---------------------------------------------------------------------
CREATE TABLE revision_documento (
    id_revision         SERIAL PRIMARY KEY,
    id_tramite          INTEGER      NOT NULL REFERENCES tramite ON DELETE CASCADE,
    id_requisito        INTEGER      REFERENCES requisito ON DELETE SET NULL,   -- requisito que acredita (RF-06)
    token_sesion        VARCHAR(64)  NOT NULL,
    nombre_original     VARCHAR(200) NOT NULL,
    nombre_archivo_uuid CHAR(36)     NOT NULL UNIQUE,
    tipo_mime           VARCHAR(50)  NOT NULL
                        CHECK (tipo_mime IN ('application/pdf', 'image/jpeg', 'image/png')),
    tamano_bytes        BIGINT       NOT NULL,
    numero_paginas      SMALLINT     CHECK (numero_paginas <= 20),
    fecha_carga         TIMESTAMP    NOT NULL DEFAULT now(),
    fecha_expiracion    TIMESTAMP    NOT NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'NO_VERIFICABLE'
);
CREATE INDEX idx_revision_token ON revision_documento (token_sesion);
CREATE INDEX idx_revision_expiracion ON revision_documento (fecha_expiracion);

CREATE TABLE resultado_criterio (
    id_resultado   SERIAL PRIMARY KEY,
    id_revision    INTEGER      NOT NULL REFERENCES revision_documento ON DELETE CASCADE,
    id_regla       INTEGER      REFERENCES regla_revision ON DELETE SET NULL,
    criterio       VARCHAR(200) NOT NULL,
    tipo           VARCHAR(30)  NOT NULL,
    resultado      VARCHAR(20)  NOT NULL DEFAULT 'NO_VERIFICABLE'
                   CHECK (resultado IN ('CUMPLE', 'OBSERVADO', 'NO_VERIFICABLE')),
    detalle        VARCHAR(400)
);

-- ---------------------------------------------------------------------
-- Seguridad y trazabilidad
-- ---------------------------------------------------------------------
CREATE TABLE rol (
    id_rol         SERIAL PRIMARY KEY,
    nombre         VARCHAR(30) NOT NULL UNIQUE
                   CHECK (nombre IN ('ADMIN_CONTENIDOS', 'PERSONAL_UTD'))
);

CREATE TABLE usuario_administrativo (
    id_usuario      SERIAL PRIMARY KEY,
    id_rol          INTEGER      NOT NULL REFERENCES rol,
    usuario         VARCHAR(60)  NOT NULL UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    hash_contrasena CHAR(60)     NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE bitacora_cambios (
    id_bitacora    BIGSERIAL PRIMARY KEY,
    id_usuario     INTEGER      REFERENCES usuario_administrativo,
    entidad        VARCHAR(60)  NOT NULL,
    accion         VARCHAR(30)  NOT NULL,
    valor_anterior TEXT,
    valor_nuevo    TEXT,
    fecha          TIMESTAMP    NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Estadísticas anónimas y retroalimentación (RF-11, RF-16, RN-14)
-- ---------------------------------------------------------------------
CREATE TABLE registro_consulta (
    id_consulta    BIGSERIAL PRIMARY KEY,
    tipo           VARCHAR(20)  NOT NULL
                   CHECK (tipo IN ('HOJA_RUTA', 'PDF', 'ORIENTACION', 'NORMATIVA', 'UBICACION', 'DERIVACION', 'REVISION')),
    id_tramite     INTEGER      REFERENCES tramite ON DELETE SET NULL,
    id_dependencia INTEGER      REFERENCES dependencia ON DELETE SET NULL,
    fecha          TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_consulta_fecha ON registro_consulta (fecha);

-- Copia anónima de cada criterio evaluado: se conserva cuando la revisión se elimina a las 24 h
CREATE TABLE registro_observacion (
    id_registro    BIGSERIAL PRIMARY KEY,
    id_revision    INTEGER      REFERENCES revision_documento ON DELETE SET NULL,
    id_tramite     INTEGER      REFERENCES tramite ON DELETE SET NULL,
    criterio       VARCHAR(200) NOT NULL,
    tipo           VARCHAR(30)  NOT NULL,
    resultado      VARCHAR(20)  NOT NULL CHECK (resultado IN ('CUMPLE', 'OBSERVADO', 'NO_VERIFICABLE')),
    fecha          TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_observacion_fecha ON registro_observacion (fecha);

CREATE TABLE retroalimentacion (
    id_retroalimentacion SERIAL PRIMARY KEY,
    id_tramite     INTEGER      REFERENCES tramite ON DELETE SET NULL,
    tipo           VARCHAR(30)  NOT NULL
                   CHECK (tipo IN ('REQUISITO', 'PAGO', 'PLAZO', 'UBICACION', 'NORMATIVA', 'OTRO')),
    comentario     VARCHAR(500) NOT NULL,
    atendida       BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha          TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE ejecucion_limpieza (
    id_ejecucion       SERIAL PRIMARY KEY,
    fecha              TIMESTAMP NOT NULL DEFAULT now(),
    archivos_eliminados INTEGER  NOT NULL,
    origen             VARCHAR(20) NOT NULL
);
