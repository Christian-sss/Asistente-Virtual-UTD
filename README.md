# Asistente Virtual Web de Gestión y Orientación de Trámites Documentarios – UTD UNJBG

Avance del sistema con los **10 requerimientos funcionales de prioridad Alta** del SAD, implementado con
Java 21, Jakarta EE (Servlets 6.0, JSP 3.1, JSTL), Apache Tomcat 10.1 y PostgreSQL, en capas MVC
(vista JSP → Servlet → servicio → DAO).

Los trámites, requisitos, montos, plazos y unidades de atención provienen del **TUPA vigente de la UNJBG,
aprobado por R.R. N.° 11021-2023-UNJBG** (publicado en El Peruano el 31/03/2023).

## Requerimientos implementados

| RF | Requerimiento | Dónde se ve |
|----|---------------|-------------|
| RF-01 | Autenticar usuario administrativo (BCrypt, roles, cierre de sesión, cambio de contraseña, bitácora) | `/login`, menú del usuario |
| RF-03 | Buscar trámite en el catálogo por categoría, nombre o palabra clave | `/tramites` |
| RF-04 | Identificar trámite mediante preguntas guiadas | `/orientacion` |
| RF-05 | Generar hoja de ruta en pantalla y en PDF con aviso referencial | `/hoja-ruta?id=…`, botón «Descargar PDF» |
| RF-06 | Cargar documento (cláusula de datos, tipo MIME real, 10 MB, 20 páginas, antivirus, nombre UUID) | `/revision` |
| RF-07 | Revisión formal y reporte Cumple / Observado / No verificable, con confirmación asistida | `/revision/reporte?id=…` |
| RF-08 | Consultar normativa y sustento normativo | `/normativa` |
| RF-12 | Gestionar trámites, pasos, publicación y preguntas guiadas | Panel → Trámites / Preguntas guiadas |
| RF-13 | Gestionar requisitos (con artículo que los sustenta), reglas de revisión y normativas | Panel → Trámites (editar) / Normativas |
| RF-17 | Eliminar automáticamente cada hora los documentos vencidos (24 h) | Panel → Resumen («Ejecutar limpieza ahora») |

Quedan para el siguiente avance: RF-02, RF-09, RF-10, RF-11, RF-14, RF-15 y RF-16.

## Contenido cargado (TUPA UNJBG)

15 trámites en 4 categorías, con sus requisitos, derechos de pago y plazos oficiales:

- **Constancias y certificados:** Constancia de Ingreso (S/ 13.20), Certificado de Estudios (S/ 8.60 por semestre),
  Constancia de Egresado (S/ 22.00), Constancia por Mérito Académico (S/ 13.20).
- **Matrícula:** Reserva de Matrícula (S/ 28.00), Reanudación de Estudios (S/ 50 / 70 / 60), Rectificación de
  Matrícula (S/ 21.60), Convalidación de Asignaturas (S/ 50.00).
- **Grados y títulos:** Bachiller (S/ 328.80), Título Profesional (S/ 885.00), Duplicado de Diploma (S/ 205.50).
- **Otros trámites:** Carta de Presentación para prácticas (S/ 9.50), Copia de documentación de Registro Central
  (S/ 16.60), Acceso a la Información Pública (gratuito, solo costo de reproducción), Cambio de nombres (S/ 182.00).

Normativas: TUPA (R.R. N.° 11021-2023-UNJBG), Ley Universitaria N.° 30220, Estatuto UNJBG, Reglamento Académico
de Pregrado, Ley N.° 27806 (Transparencia) y Ley N.° 29733 (Protección de Datos Personales).

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `Admin2026` | ADMIN_CONTENIDOS (administra todo el contenido) |
| `utd` | `Utd2026` | PERSONAL_UTD (solo consulta trámites y bitácora) |

Cámbialas desde el menú del usuario → «Cambiar contraseña».

## Cómo ejecutarlo en esta computadora

Todo está en la carpeta `SISTEMA`:

1. **`iniciar-sistema.bat`**: inicia PostgreSQL y Tomcat 10.1 y abre `http://localhost:8080/asistente-virtual/`.
2. **`detener-sistema.bat`**: detiene ambos servicios.
3. **`compilar-y-desplegar.bat`**: compila con Maven y vuelve a desplegar el WAR (después de cambiar código).
4. **`restablecer-base-de-datos.bat`**: vuelve a cargar los datos iniciales del TUPA (borra los cambios de prueba).

Los programas están en `SISTEMA/herramientas` (Tomcat 10.1.34 y PostgreSQL 16.6 portables; usuario de base de
datos `postgres` / `postgres`, base `asistente_utd`, puerto 5432).

## Abrirlo en NetBeans

1. *File → Open Project* y seleccionar la carpeta `asistente-virtual` (es un proyecto Maven).
2. *Tools → Servers → Add Server → Apache Tomcat or TomEE* y elegir la carpeta
   `SISTEMA/herramientas/apache-tomcat-10.1.34` (crear un usuario de manager si NetBeans lo pide).
3. Clic derecho en el proyecto → *Properties → Run* → servidor Apache Tomcat 10.1.
4. Iniciar PostgreSQL con `iniciar-sistema.bat` (o solo la línea de `pg_ctl`) y luego *Run Project*.

## Instalación en otra computadora

1. Instalar JDK 21 o superior, Apache Tomcat 10.1 y PostgreSQL 14 o superior.
2. Crear la base de datos y cargar los scripts:
   ```
   createdb -U postgres asistente_utd
   psql -U postgres -d asistente_utd -f db/01_esquema.sql
   psql -U postgres -d asistente_utd -f db/02_datos.sql
   ```
3. Ajustar la conexión en `src/main/webapp/META-INF/context.xml` (o en la configuración de Tomcat).
4. `mvn package` y copiar `target/asistente-virtual.war` a la carpeta `webapps` de Tomcat.

## Guion sugerido para la presentación

1. **Inicio y catálogo (RF-03):** buscar «bachiller» y filtrar por «Matrícula».
2. **Orientación guiada (RF-04):** «Obtener una constancia o un certificado» → «Un documento con mis notas por
   semestre» → el sistema identifica el *Certificado de Estudios*.
3. **Hoja de ruta (RF-05):** mostrar pasos, pago previo, requisitos con su sustento normativo y la oficina; descargar el PDF.
4. **Revisión preliminar (RF-06 y RF-07):** subir `docs/ejemplos/solicitud_con_omisiones.pdf` (sale *Observado*:
   faltan DNI, correo, fecha, código y recibo) y luego `docs/ejemplos/solicitud_completa.pdf` (todos los datos
   *Cumple*); marcar la firma y el recibo en la confirmación asistida.
5. **Normativa (RF-08):** buscar «bachiller» y abrir la Ley Universitaria, art. 45.1.
6. **Panel (RF-01, RF-12, RF-13):** ingresar con `admin`, intentar publicar un trámite sin pasos (el sistema lo
   impide), agregar un requisito con su artículo normativo y una regla de revisión, y mostrar la bitácora.
7. **Eliminación automática (RF-17):** en el Resumen, mostrar los documentos temporales y «Ejecutar limpieza ahora».
8. **Roles:** ingresar con `utd` y mostrar que no puede editar (error 403).

## Notas del entorno de demostración

- **Antivirus:** el sistema usa ClamAV (servicio `clamd`, puerto 3310). En esta computadora no está instalado, por
  eso el criterio «Análisis antivirus» aparece como *No verificable* y el estado general del reporte no llega a
  *Cumple*. Con ClamAV activo el criterio pasa a *Cumple*; con `clamav.obligatorio=true` en `context.xml` se
  rechaza toda carga si el antivirus no responde.
- **OCR:** Tess4J es opcional y no está habilitado en este avance; en imágenes JPG/PNG y PDF escaneados los
  criterios de texto quedan *No verificable*.
- La interfaz usa Bootstrap e íconos desde CDN, por lo que la presentación necesita conexión a Internet para verse
  con estilos.
