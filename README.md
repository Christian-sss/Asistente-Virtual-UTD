# Asistente Virtual de Trámites Documentarios – UTD UNJBG

Aplicación web que orienta a estudiantes, egresados y público en general sobre los trámites de la **Unidad de Trámite Documentario (UTD)** de la **Universidad Nacional Jorge Basadre Grohmann** (Tacna, Perú). Indica qué trámite corresponde, cuánto cuesta, qué requisitos se presentan, en qué oficina y en qué orden. También permite revisar la solicitud antes de llevarla a ventanilla.

Proyecto desarrollado para el curso de **Gestión de Proyectos**.

---

## Contenido

- [Contexto y problema](#contexto-y-problema)
- [Objetivo](#objetivo)
- [Funcionalidades](#funcionalidades)
- [Requerimientos funcionales](#requerimientos-funcionales)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Configuración](#configuración)
- [Usuarios y roles](#usuarios-y-roles)
- [Datos pendientes de completar](#datos-pendientes-de-completar)

---

## Contexto y problema

Para pedir una constancia, un certificado o un trámite de matrícula, la persona suele encontrarse con información repartida en avisos, publicaciones y consultas por teléfono o en ventanilla. No siempre queda claro qué trámite corresponde, cuál es el monto del derecho de pago ni qué documentos se adjuntan.

Por eso muchas solicitudes se observan o se devuelven por datos incompletos, por no adjuntar el recibo de pago o por requisitos mal entendidos. Cada observación significa volver a la universidad, rehacer el expediente y esperar más.

## Objetivo

Reunir en un solo lugar la información oficial de los trámites y acompañar al usuario desde que identifica su trámite hasta que presenta la solicitud completa. Así se busca reducir las observaciones en ventanilla y descargar al personal de la UTD de las consultas repetitivas.

## Funcionalidades

### Para el público (sin iniciar sesión)

| Módulo | Qué permite |
|--------|-------------|
| **Catálogo de trámites** | Buscar trámites por categoría o palabra clave y ver requisitos, derecho de pago, plazo, calificación y oficina de atención. |
| **Orientación guiada** | Llegar al trámite correcto respondiendo preguntas simples, aunque no se sepa cómo se llama. |
| **Hoja de ruta** | Ver los pasos del trámite, el pago previo y la norma que respalda cada requisito, y descargarla en **PDF**. |
| **Revisión de la solicitud** | Subir la solicitud (PDF, JPG o PNG, hasta 10 MB) y obtener una revisión preliminar: formato, tamaño, número de páginas, datos básicos (DNI, correo, firma, pago) y seguridad del archivo. Cada criterio se marca como *cumple*, *observado* o *no verificable*. |
| **Normativa** | Consultar las normas vigentes por palabra clave, trámite o tipo de documento, con sus artículos y fuente oficial. |
| **Ubicación** | Ver las oficinas sobre el **mapa real del campus** (OpenStreetMap, con vista satelital), la secuencia de visita de cada trámite y los canales digitales de cada dependencia (portales, correos, teléfonos). |
| **Expediente y derivación** | Entender qué es el cargo, el número de expediente y sus estados, y saber a qué oficina dirigir una consulta. |
| **Retroalimentación** | Reportar de forma anónima información desactualizada o inexacta. |

### Para el personal de la UTD (panel administrativo)

- Gestión de **categorías, trámites, pasos, requisitos y reglas de revisión**.
- Gestión de **normativas** y sus artículos, con vigencia y fuente.
- Edición del **árbol de preguntas guiadas**.
- Mantenimiento de **puntos de atención** (ubicados con un clic en el mapa), **canales digitales** y **reglas de derivación**.
- **Validación y publicación** de contenidos: cada cambio queda pendiente hasta que el personal de la UTD lo aprueba. Mientras tanto, el público sigue viendo la última versión aprobada.
- **Reportes** de consultas y observaciones por periodo, con exportación a **CSV**.
- **Gestión de usuarios y roles**, con **bitácora** de todos los cambios.
- **Eliminación automática** de los documentos subidos al cumplirse 24 horas.

## Requerimientos funcionales

La versión 2.0 cubre los 16 requerimientos funcionales finales del SRS (FD03 v1.1) y el RF-17 del SAD (FD04).

| RF | Requerimiento | Ruta | Componentes principales |
|----|---------------|------|-------------------------|
| RF-01 | Autenticar usuario administrativo | `/login` | `LoginServlet`, `ServicioAutenticacion`, `AuthFilter` |
| RF-02 | Gestionar usuarios y roles | `/admin/usuarios` | `AdminUsuarioServlet`, `ServicioUsuarios` |
| RF-03 | Buscar trámite en el catálogo | `/tramites` | `TramiteServlet`, `ServicioOrientacion` |
| RF-04 | Identificar trámite con preguntas guiadas | `/orientacion` | `OrientacionServlet` |
| RF-05 | Generar hoja de ruta en PDF | `/hoja-ruta`, `/hoja-ruta/pdf` | `HojaRutaServlet`, `GeneradorHojaRutaPdf` |
| RF-06 | Cargar documento asociado a un requisito | `/revision` | `RevisionServlet`, `ValidadorArchivo` |
| RF-07 | Revisión formal y reporte de conformidad | `/revision/reporte` | `ServicioRevisionDocumental`, `ExtractorTexto`, `EvaluadorReglas` |
| RF-08 | Consultar normativa | `/normativa` | `NormativaServlet`, `ServicioNormativa` |
| RF-09 | Mapa del campus y canales digitales | `/ubicacion` | `UbicacionServlet`, `ServicioUbicacion` |
| RF-10 | Cargo, expediente y derivación | `/derivacion` | `DerivacionServlet`, `ServicioDerivacion` |
| RF-11 | Retroalimentación anónima | `POST /retroalimentacion` | `DerivacionServlet`, `ServicioEstadistica` |
| RF-12 | Categorías, trámites, pasos y preguntas guiadas | `/admin/categorias`, `/admin/tramite`, `/admin/preguntas` | `ServicioContenidos` |
| RF-13 | Requisitos, reglas de revisión y normativas | `/admin/tramite`, `/admin/normativas` | `ServicioContenidos` |
| RF-14 | Puntos de atención, canales y reglas de derivación | `/admin/ubicaciones`, `/admin/derivacion` | `AdminUbicacionServlet`, `ServicioUbicacion` |
| RF-15 | Validar y publicar contenidos | `/admin/validacion` | `AdminValidacionServlet`, `ServicioValidacion` |
| RF-16 | Reportes por periodo y exportación CSV | `/admin/reportes` | `ReporteServlet`, `ServicioEstadistica` |
| RF-17 | Eliminación automática de documentos (24 h) | Tarea programada | `LimpiezaArchivosListener`, `ServicioLimpieza` |

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 |
| Web | Jakarta Servlet 6.0, JSP y JSTL 3.0 |
| Servidor | Apache Tomcat 10.1 o superior |
| Base de datos | PostgreSQL (pool de conexiones Tomcat JDBC) |
| Interfaz | Bootstrap 5.3, Bootstrap Icons |
| Mapas | Leaflet.js 1.9, OpenStreetMap, imágenes satelitales de Esri |
| Documentos | Apache PDFBox 3 (lectura y generación de PDF), Apache Tika (tipo real del archivo) |
| OCR (opcional) | Tess4J / Tesseract OCR |
| Antivirus (opcional) | ClamAV (`clamd`) |
| Seguridad | BCrypt para contraseñas, protección CSRF, control de acceso por rol |
| Construcción | Maven |

## Arquitectura

La aplicación está organizada en capas (MVC):

```
Vistas JSP  ──►  Controladores (Servlets)  ──►  Servicios  ──►  DAO  ──►  PostgreSQL
                       │                              │
                  Filtros: sesión,              Reglas de negocio,
                  rol, codificación             bitácora y transacciones
```

- **Vistas (JSP):** solo muestran información, sin lógica de negocio.
- **Controladores:** reciben la petición, validan los parámetros y delegan en los servicios.
- **Servicios:** aplican las reglas de negocio (validación de contenidos, revisión documental, derivación, estadísticas) y registran cada cambio en la bitácora dentro de la misma transacción.
- **DAO:** acceso a datos con JDBC y consultas parametrizadas.

Con esta separación se pueden actualizar trámites, requisitos y reglas de revisión desde el panel sin tocar la interfaz ni el código.

## Estructura del proyecto

```
Asistente-Virtual-UTD/
├── db/
│   ├── 01_esquema.sql          # Creación de tablas
│   ├── 02_datos.sql            # Datos iniciales: trámites del TUPA, normativa, usuarios
│   └── 03_mapa_real.sql        # Migración para bases creadas antes del mapa real
├── docs/
│   └── ejemplos/               # Solicitudes PDF de ejemplo para probar la revisión
├── src/main/java/asistente/
│   ├── config/                 # Configuración y conexión a la base de datos
│   ├── controlador/            # Servlets, filtros y listeners
│   ├── servicio/               # Lógica de negocio
│   ├── dao/                    # Acceso a datos
│   └── modelo/                 # Entidades
├── src/main/webapp/
│   ├── META-INF/context.xml    # Parámetros del entorno
│   ├── WEB-INF/vistas/         # Páginas JSP (públicas y del panel)
│   └── recursos/               # CSS, JavaScript y contorno del campus (GeoJSON)
└── pom.xml
```

## Instalación y ejecución

### Requisitos

- JDK 21 o superior
- Apache Maven 3.9 o superior
- Apache Tomcat 10.1 o superior
- PostgreSQL 14 o superior

### Pasos

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/Christian-sss/Asistente-Virtual-UTD.git
   cd Asistente-Virtual-UTD
   ```

2. **Crear la base de datos** y cargar los scripts en orden:

   ```bash
   createdb -U postgres -E UTF8 -T template0 asistente_utd
   psql -U postgres -d asistente_utd -f db/01_esquema.sql
   psql -U postgres -d asistente_utd -f db/02_datos.sql
   ```

   > `01_esquema.sql` vuelve a crear todas las tablas: haga un respaldo antes de ejecutarlo sobre una base con datos.
   > Si la base se creó con una versión anterior del proyecto (plano con coordenadas X/Y), ejecute además `db/03_mapa_real.sql`.

3. **Configurar la conexión** en `src/main/webapp/META-INF/context.xml` (URL, usuario y contraseña de PostgreSQL).

4. **Compilar**

   ```bash
   mvn package
   ```

5. **Desplegar** `target/asistente-virtual.war` copiándolo a la carpeta `webapps` de Tomcat e iniciar el servidor.

6. Abrir en el navegador: <http://localhost:8080/asistente-virtual/>

Ejemplos para probar la revisión de solicitudes en `docs/ejemplos/`: una solicitud completa y otra con omisiones.

## Configuración

Todos los parámetros están en `src/main/webapp/META-INF/context.xml`. También se pueden sobrescribir en Tomcat (`conf/Catalina/localhost/asistente-virtual.xml`) sin recompilar.

| Parámetro | Descripción | Valor por defecto |
|-----------|-------------|-------------------|
| `db.url`, `db.usuario`, `db.contrasena` | Conexión a PostgreSQL | `localhost:5432/asistente_utd` |
| `archivos.directorio` | Carpeta temporal de los documentos subidos (vacío = carpeta temporal del sistema) | vacío |
| `archivos.horasRetencion` | Horas que se conserva un documento antes de eliminarse | `24` |
| `limpieza.intervaloMinutos` | Cada cuánto se ejecuta la limpieza automática | `60` |
| `clamav.host`, `clamav.puerto`, `clamav.obligatorio` | Antivirus ClamAV. Si es obligatorio y no está disponible, se rechaza la carga | `127.0.0.1`, `3310`, `false` |
| `ocr.habilitado`, `ocr.tessdata`, `ocr.idioma` | OCR para imágenes y PDF escaneados (requiere Tesseract con el idioma español) | `false`, vacío, `spa` |

El mapa del campus se carga desde internet (OpenStreetMap y Esri), así que el navegador del usuario necesita conexión.

## Usuarios y roles

| Rol | Puede |
|-----|-------|
| `ADMIN_CONTENIDOS` | Registrar y editar trámites, requisitos, normativas, preguntas guiadas, ubicaciones y usuarios. Sus cambios quedan pendientes de validación. |
| `PERSONAL_UTD` | Validar y publicar los cambios (o devolverlos con observaciones), consultar reportes, trámites y la bitácora. |

Usuarios de prueba incluidos en `db/02_datos.sql`:

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `Admin2026` | ADMIN_CONTENIDOS |
| `utd` | `Utd2026` | PERSONAL_UTD |

> Estas credenciales son solo para el entorno de desarrollo. Cámbielas desde el panel antes de publicar la aplicación.

### Flujo de publicación

1. El administrador de contenidos registra o modifica un trámite: el cambio queda **pendiente de validación**.
2. El personal de la UTD lo revisa en `/admin/validacion`.
3. Si el trámite tiene al menos un paso y sus requisitos obligatorios se sustentan en una norma vigente, **se publica**. Si no, se **devuelve con observaciones**.

## Datos pendientes de completar

Hay información que debe confirmar la UTD:

- **Ubicación de las oficinas:** los puntos de atención están en posiciones aproximadas dentro de la Ciudad Universitaria. Cada oficina se ubica en su edificio desde *Ubicaciones y canales*: clic en el formulario del punto y luego clic en el mapa (la vista satelital ayuda a reconocer los edificios).
- **Canales digitales:** solo están cargados los conocidos (portal institucional, central telefónica, correos de Registros Académicos y el TUPA publicado en El Peruano). Falta registrar la mesa de partes virtual y las plataformas de cada dependencia.
- **Reglas de derivación:** revisar las palabras clave iniciales en *Derivación*.

## Fuente de la información

Los trámites, requisitos, montos y plazos provienen del **Texto Único de Procedimientos Administrativos (TUPA)** vigente de la UNJBG.
