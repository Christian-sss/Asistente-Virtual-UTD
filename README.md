# Asistente Virtual de Trámites Documentarios – UTD UNJBG

Esta aplicación web nace de una necesidad concreta de la Unidad de Trámite Documentario de la Universidad Nacional Jorge Basadre Grohmann: que las personas lleguen a la ventanilla sabiendo qué trámite les corresponde, cuánto deben pagar, qué requisitos presentar y en qué oficina hacerlo. En lugar de repartir esa información en avisos, carteles o consultas sueltas, el sistema la concentra y la convierte en una guía que se puede seguir de principio a fin.

## El problema

Cuando alguien necesita una constancia, un certificado o un trámite de matrícula, lo habitual es encontrarse con información dispersa y con la duda de si el documento está completo. Muchas solicitudes se devuelven por datos que faltan, pagos que no se adjuntan o requisitos que no quedaron claros. Cada observación significa volver, rehacer el papeleo y esperar más.

El asistente busca reducir eso: ordena la información oficial, ayuda a identificar el trámite correcto y permite revisar la solicitud antes de presentarla.

## Qué ofrece

Acompaña al usuario en cuatro momentos:

- **Encontrar el trámite.** Un catálogo con los procedimientos vigentes, sus requisitos, montos, plazos y oficina de atención, con búsqueda por categoría o palabra clave.
- **Identificarlo cuando no sabe cómo se llama.** A través de preguntas simples, el sistema conduce a la opción que corresponde.
- **Prepararlo.** Genera una hoja de ruta con los pasos, el pago previo y el sustento normativo de cada requisito, disponible en pantalla y para descargar en PDF.
- **Revisarlo antes de entregarlo.** Permite cargar la solicitud en PDF, JPG o PNG y hace una revisión preliminar de formato, tamaño, páginas, datos básicos y seguridad del archivo. El resultado se muestra criterio por criterio —cumple, observado o no verificable— junto con una confirmación asistida de firma y anexos.

También incluye un espacio para consultar la normativa que respalda cada requisito y un panel para que el personal mantenga actualizado el contenido (trámites, requisitos, reglas de revisión, normativa y preguntas guía), dejando registro de cada cambio. Los documentos temporales se eliminan solos cuando vence su plazo de conservación.

## De dónde sale la información

Los trámites, requisitos, montos y plazos provienen del TUPA vigente de la UNJBG, aprobado por la R.R. N.° 11021-2023-UNJBG.

## Cómo está pensado

Está construido como una aplicación web en capas: las vistas se apoyan en controladores que delegan la lógica en servicios, y estos acceden a una base de datos relacional. Esa separación permite mantener el contenido y las reglas de revisión sin tocar la interfaz, y deja preparado el sistema para crecer con nuevos trámites y requisitos.

## Requerimientos funcionales cubiertos (versión 2.0)

Esta versión implementa los 16 requerimientos funcionales finales del SRS (FD03 v1.1) y el RF-17 del SAD (FD04).

| RF | Requerimiento | Dónde está |
|----|---------------|------------|
| RF-01 | Autenticar usuario administrativo | `LoginServlet`, `ServicioAutenticacion`, `AuthFilter` |
| RF-02 | Gestionar usuarios y roles | `/admin/usuarios` · `AdminUsuarioServlet`, `ServicioUsuarios` |
| RF-03 | Buscar trámite en el catálogo | `/tramites` · `TramiteServlet`, `ServicioOrientacion` |
| RF-04 | Identificar trámite con preguntas guiadas | `/orientacion` · `OrientacionServlet` |
| RF-05 | Hoja de ruta en PDF | `/hoja-ruta`, `/hoja-ruta/pdf` · `GeneradorHojaRutaPdf` |
| RF-06 | Cargar documento (asociado a un requisito) | `/revision` · `RevisionServlet`, `ValidadorArchivo` |
| RF-07 | Revisión formal y reporte (OCR opcional con Tess4J) | `ServicioRevisionDocumental`, `ExtractorTexto`, `EvaluadorReglas` |
| RF-08 | Consultar normativa (palabra clave, trámite y tipo de documento) | `/normativa` · `NormativaServlet` |
| RF-09 | Plano del campus (Leaflet.js) y canales digitales | `/ubicacion`, `/ubicacion/puntos`, `/ubicacion/canales` · `UbicacionServlet` |
| RF-10 | Cargo, expediente y derivación | `/derivacion` · `DerivacionServlet`, `ServicioDerivacion` |
| RF-11 | Retroalimentación anónima | `POST /retroalimentacion` · `DerivacionServlet` |
| RF-12 | Categorías, trámites, pasos y preguntas guiadas | `/admin/categorias`, `/admin/tramite`, `/admin/preguntas` |
| RF-13 | Requisitos, reglas de revisión y normativas | `/admin/tramite`, `/admin/normativas` |
| RF-14 | Puntos de atención, canales y reglas de derivación | `/admin/ubicaciones`, `/admin/derivacion` · `AdminUbicacionServlet` |
| RF-15 | Validar y publicar contenidos | `/admin/validacion` · `AdminValidacionServlet`, `ServicioValidacion` |
| RF-16 | Reportes por periodo y exportación CSV | `/admin/reportes`, `/admin/reportes/csv` · `ReporteServlet`, `ServicioEstadistica` |
| RF-17 | Eliminación automática de documentos (24 h) | `LimpiezaArchivosListener`, `ServicioLimpieza` |

### Puesta en marcha

1. Crear la base de datos `asistente_utd` en PostgreSQL y ejecutar, en orden, `db/01_esquema.sql` y `db/02_datos.sql`
   (el esquema recrea todas las tablas; respalde los datos antes de ejecutarlo sobre una base existente).
2. Ajustar la conexión y los parámetros en `src/main/webapp/META-INF/context.xml`.
3. Compilar con `mvn package` y desplegar `target/asistente-virtual.war` en Apache Tomcat 10.1 (Java 21).

Usuarios de prueba: `admin` / `Admin2026` (ADMIN_CONTENIDOS) y `utd` / `Utd2026` (PERSONAL_UTD).

### Flujo de publicación (RF-15)

Toda modificación de un trámite queda **pendiente de validación**. Mientras tanto el público sigue viendo la última versión
aprobada. El personal de la UTD (rol PERSONAL_UTD) la aprueba en `/admin/validacion`, lo que la publica si cumple RN-10
(al menos un paso y requisitos obligatorios con sustento en una norma vigente), o la devuelve con observaciones.

### Datos que debe completar la UTD

- **Plano del campus:** `recursos/img/plano-campus.svg` es un plano referencial de ejemplo (1000 x 640 px). Reemplácelo por el
  plano oficial (o cambie `plano.imagen` en `context.xml`) y ajuste las coordenadas de cada punto en «Ubicaciones y canales»,
  haciendo clic sobre el plano.
- **Canales digitales:** se cargaron solo los datos ya conocidos (portal, central telefónica, correos de Registros Académicos y
  el TUPA en El Peruano). Registre la mesa de partes virtual y las demás plataformas de cada dependencia.
- **Reglas de derivación:** revise las palabras clave iniciales en «Derivación».
- **OCR (opcional):** instale Tesseract OCR con el idioma español en el servidor y active `ocr.habilitado` en `context.xml`.
