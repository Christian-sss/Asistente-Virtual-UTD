package asistente.servicio;

import asistente.dao.BitacoraDAO;
import asistente.dao.NormativaDAO;
import asistente.dao.PreguntaGuiaDAO;
import asistente.dao.ReglaRevisionDAO;
import asistente.dao.TramiteDAO;
import asistente.modelo.ArticuloNormativo;
import asistente.modelo.BitacoraCambio;
import asistente.modelo.Categoria;
import asistente.modelo.Normativa;
import asistente.modelo.OpcionRespuesta;
import asistente.modelo.Paso;
import asistente.modelo.PreguntaGuia;
import asistente.modelo.PuntoAtencion;
import asistente.modelo.ReglaRevision;
import asistente.modelo.Requisito;
import asistente.modelo.Tramite;
import asistente.modelo.UsuarioAdministrativo;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Administración de contenidos: categorías, trámites, pasos y preguntas guiadas (RF-12); requisitos,
 * reglas de revisión y normativas (RF-13). Cada cambio se registra en la bitácora en la misma transacción.
 * Toda modificación de un trámite queda PENDIENTE de validación por el personal de la UTD (RF-15, RN-11):
 * mientras tanto el público sigue viendo la última versión publicada.
 */
public class ServicioContenidos extends ServicioBase {

    private final TramiteDAO tramiteDAO = new TramiteDAO();
    private final PreguntaGuiaDAO preguntaDAO = new PreguntaGuiaDAO();
    private final NormativaDAO normativaDAO = new NormativaDAO();
    private final ReglaRevisionDAO reglaDAO = new ReglaRevisionDAO();
    private final BitacoraDAO bitacora = new BitacoraDAO();

    // ------------------------------------------------------------------ consultas
    public List<Tramite> listarTramites() {
        return consultar(tramiteDAO::listarTodos);
    }

    public Tramite tramite(int id) {
        return consultar(cn -> tramiteDAO.buscarPorId(cn, id));
    }

    public List<Categoria> categorias() {
        return consultar(tramiteDAO::listarCategorias);
    }

    public List<PuntoAtencion> puntos() {
        return consultar(tramiteDAO::listarPuntos);
    }

    public List<ReglaRevision> reglas(int idTramite) {
        return consultar(cn -> reglaDAO.listarPorTramite(cn, idTramite, false));
    }

    public List<PreguntaGuia> preguntas() {
        return consultar(preguntaDAO::listarTodas);
    }

    public List<Normativa> normativas() {
        return consultar(cn -> normativaDAO.buscar(cn, null, null));
    }

    public Normativa normativa(int id) {
        return consultar(cn -> normativaDAO.buscarPorId(cn, id));
    }

    public List<ArticuloNormativo> articulos() {
        return consultar(normativaDAO::listarTodosArticulos);
    }

    public List<BitacoraCambio> bitacora(int limite) {
        return consultar(cn -> bitacora.listarRecientes(cn, limite));
    }

    /** Totales del panel: trámites, publicados, normativas, preguntas, pendientes de validación y devueltos. */
    public int[] estadisticas() {
        return consultar(cn -> new int[]{
                tramiteDAO.contar(cn, ""),
                tramiteDAO.contar(cn, "WHERE publicado"),
                normativaDAO.buscar(cn, null, null).size(),
                preguntaDAO.listarTodas(cn).size(),
                tramiteDAO.contar(cn, "WHERE estado_contenido = 'PENDIENTE'"),
                tramiteDAO.contar(cn, "WHERE estado_contenido = 'DEVUELTO'")});
    }

    // ------------------------------------------------------------------ categorías (RF-12)
    public int guardarCategoria(UsuarioAdministrativo u, Categoria c) {
        if (vacio(c.getNombre())) {
            throw new ReglaNegocioException("El nombre de la categoría es obligatorio.");
        }
        if (c.getIcono() != null && !c.getIcono().matches("bi-[a-z0-9-]{1,36}")) {
            throw new ReglaNegocioException("El ícono debe ser un nombre de Bootstrap Icons, por ejemplo bi-folder.");
        }
        return enTransaccion(cn -> {
            String antes = null;
            if (c.getId() != 0) {
                for (Categoria x : tramiteDAO.listarCategorias(cn)) {
                    if (x.getId() == c.getId()) {
                        antes = x.getNombre();
                    }
                }
            }
            int id = tramiteDAO.guardarCategoria(cn, c);
            bitacora.insertar(cn, u.getId(), "categoria", c.getId() == 0 ? "REGISTRO" : "EDICION", antes, c.getNombre());
            return id;
        });
    }

    public void eliminarCategoria(UsuarioAdministrativo u, int idCategoria) {
        enTransaccion(cn -> {
            if (tramiteDAO.contarTramitesDeCategoria(cn, idCategoria) > 0) {
                throw new ReglaNegocioException("No se puede eliminar: la categoría tiene trámites. Muévalos a otra categoría primero.");
            }
            tramiteDAO.eliminarCategoria(cn, idCategoria);
            bitacora.insertar(cn, u.getId(), "categoria", "ELIMINACION", "Categoría " + idCategoria, null);
            return null;
        });
    }

    // ------------------------------------------------------------------ ciclo de validación (RN-11)
    /**
     * Se invoca ANTES de modificar un trámite: si está publicado y aún no tiene copia de su versión
     * publicada, la guarda, para que el público la siga viendo mientras el cambio se valida.
     */
    private void conservarVersionPublicada(java.sql.Connection cn, int idTramite) throws java.sql.SQLException {
        Tramite actual = tramiteDAO.buscarPorId(cn, idTramite);
        if (actual == null) {
            throw new ReglaNegocioException("El trámite no existe.");
        }
        if (actual.isPublicado() && tramiteDAO.leerVersionPublicada(cn, idTramite) == null) {
            tramiteDAO.guardarVersionPublicada(cn, idTramite, VersionPublicada.serializar(actual));
        }
    }

    /** Se invoca DESPUÉS del cambio: queda pendiente de validación por el personal de la UTD. */
    private void quedaPendiente(java.sql.Connection cn, int idTramite) throws java.sql.SQLException {
        tramiteDAO.marcarPendiente(cn, idTramite);
    }

    // ------------------------------------------------------------------ trámites (RF-12)
    public int guardarTramite(UsuarioAdministrativo u, Tramite t) {
        validarTramite(t);
        return enTransaccion(cn -> {
            if (t.getId() == 0) {
                int id = tramiteDAO.insertar(cn, t);
                bitacora.insertar(cn, u.getId(), "tramite", "REGISTRO", null, t.getCodigo() + " - " + t.getNombre());
                return id;
            }
            conservarVersionPublicada(cn, t.getId());
            Tramite antes = tramiteDAO.buscarPorId(cn, t.getId());
            tramiteDAO.actualizar(cn, t);
            quedaPendiente(cn, t.getId());
            bitacora.insertar(cn, u.getId(), "tramite", "EDICION", resumen(antes), resumen(t));
            return t.getId();
        });
    }

    private void validarTramite(Tramite t) {
        if (vacio(t.getCodigo()) || vacio(t.getNombre())) {
            throw new ReglaNegocioException("El código y el nombre del trámite son obligatorios.");
        }
        if (t.getDerechoPago() != null && t.getDerechoPago().signum() < 0) {
            throw new ReglaNegocioException("El derecho de trámite no puede ser negativo.");
        }
    }

    /**
     * Retira el trámite de la vista pública (por ejemplo, si su información dejó de ser correcta).
     * Para volver a publicarlo se requiere una nueva validación del personal de la UTD (RF-15).
     */
    public void retirar(UsuarioAdministrativo u, int idTramite) {
        enTransaccion(cn -> {
            Tramite t = tramiteDAO.buscarPorId(cn, idTramite);
            if (t == null) {
                throw new ReglaNegocioException("El trámite no existe.");
            }
            tramiteDAO.retirar(cn, idTramite);
            bitacora.insertar(cn, u.getId(), "tramite", "DESPUBLICACION", t.getCodigo() + ": publicado",
                    t.getCodigo() + ": retirado, pendiente de validación");
            return null;
        });
    }

    public void agregarPaso(UsuarioAdministrativo u, Paso p) {
        if (vacio(p.getDescripcion())) {
            throw new ReglaNegocioException("Escriba la descripción del paso.");
        }
        enTransaccion(cn -> {
            conservarVersionPublicada(cn, p.getIdTramite());
            tramiteDAO.insertarPaso(cn, p);
            quedaPendiente(cn, p.getIdTramite());
            bitacora.insertar(cn, u.getId(), "paso", "REGISTRO", null, "Trámite " + p.getIdTramite() + ": " + p.getDescripcion());
            return null;
        });
    }

    public void eliminarPaso(UsuarioAdministrativo u, int idTramite, int idPaso) {
        enTransaccion(cn -> {
            conservarVersionPublicada(cn, idTramite);
            tramiteDAO.eliminarPaso(cn, idTramite, idPaso);
            quedaPendiente(cn, idTramite);
            bitacora.insertar(cn, u.getId(), "paso", "ELIMINACION", "Trámite " + idTramite + ", paso " + idPaso, null);
            return null;
        });
    }

    /** Punto de atención donde se realiza el paso, para la secuencia de visita en el plano (RF-09). */
    public void asignarPuntoPaso(UsuarioAdministrativo u, int idTramite, int idPaso, Integer idPunto) {
        enTransaccion(cn -> {
            conservarVersionPublicada(cn, idTramite);
            tramiteDAO.asignarPuntoPaso(cn, idTramite, idPaso, idPunto);
            quedaPendiente(cn, idTramite);
            bitacora.insertar(cn, u.getId(), "paso", "UBICACION", "Trámite " + idTramite + ", paso " + idPaso,
                    idPunto == null ? "sin punto" : "punto " + idPunto);
            return null;
        });
    }

    // ------------------------------------------------------------------ preguntas guiadas (RF-12)
    public void guardarPregunta(UsuarioAdministrativo u, int id, String texto) {
        if (vacio(texto)) {
            throw new ReglaNegocioException("Escriba el texto de la pregunta.");
        }
        enTransaccion(cn -> {
            if (id == 0) {
                preguntaDAO.insertarPregunta(cn, texto.trim(), false);
                bitacora.insertar(cn, u.getId(), "pregunta_guia", "REGISTRO", null, texto.trim());
            } else {
                preguntaDAO.actualizarPregunta(cn, id, texto.trim());
                bitacora.insertar(cn, u.getId(), "pregunta_guia", "EDICION", "Pregunta " + id, texto.trim());
            }
            return null;
        });
    }

    public void eliminarPregunta(UsuarioAdministrativo u, int id) {
        enTransaccion(cn -> {
            preguntaDAO.eliminarPregunta(cn, id);
            bitacora.insertar(cn, u.getId(), "pregunta_guia", "ELIMINACION", "Pregunta " + id, null);
            return null;
        });
    }

    public void agregarOpcion(UsuarioAdministrativo u, OpcionRespuesta o) {
        if (vacio(o.getTexto())) {
            throw new ReglaNegocioException("Escriba el texto de la opción.");
        }
        if ((o.getIdTramite() == null) == (o.getIdPreguntaSiguiente() == null)) {
            throw new ReglaNegocioException("La opción debe conducir a una pregunta siguiente o a un trámite (solo uno).");
        }
        enTransaccion(cn -> {
            preguntaDAO.insertarOpcion(cn, o);
            bitacora.insertar(cn, u.getId(), "opcion_respuesta", "REGISTRO", null, "Pregunta " + o.getIdPregunta() + ": " + o.getTexto());
            return null;
        });
    }

    public void eliminarOpcion(UsuarioAdministrativo u, int idOpcion) {
        enTransaccion(cn -> {
            preguntaDAO.eliminarOpcion(cn, idOpcion);
            bitacora.insertar(cn, u.getId(), "opcion_respuesta", "ELIMINACION", "Opción " + idOpcion, null);
            return null;
        });
    }

    // ------------------------------------------------------------------ requisitos y reglas (RF-13)
    public void agregarRequisito(UsuarioAdministrativo u, Requisito r, Integer idArticulo) {
        if (vacio(r.getDescripcion())) {
            throw new ReglaNegocioException("Escriba la descripción del requisito.");
        }
        if (r.isObligatorio() && idArticulo == null) {
            throw new ReglaNegocioException("Todo requisito obligatorio debe vincularse al artículo normativo que lo sustenta.");
        }
        enTransaccion(cn -> {
            conservarVersionPublicada(cn, r.getIdTramite());
            int id = tramiteDAO.insertarRequisito(cn, r);
            if (idArticulo != null) {
                tramiteDAO.vincularArticulo(cn, id, idArticulo);
            }
            quedaPendiente(cn, r.getIdTramite());
            bitacora.insertar(cn, u.getId(), "requisito", "REGISTRO", null, "Trámite " + r.getIdTramite() + ": " + r.getDescripcion());
            return null;
        });
    }

    public void eliminarRequisito(UsuarioAdministrativo u, int idTramite, int idRequisito) {
        enTransaccion(cn -> {
            conservarVersionPublicada(cn, idTramite);
            tramiteDAO.eliminarRequisito(cn, idTramite, idRequisito);
            quedaPendiente(cn, idTramite);
            bitacora.insertar(cn, u.getId(), "requisito", "ELIMINACION", "Trámite " + idTramite + ", requisito " + idRequisito, null);
            return null;
        });
    }

    public void vincularArticulo(UsuarioAdministrativo u, int idRequisito, int idArticulo) {
        enTransaccion(cn -> {
            Integer idTramite = tramiteDeRequisito(cn, idRequisito);
            conservarVersionPublicada(cn, idTramite);
            tramiteDAO.vincularArticulo(cn, idRequisito, idArticulo);
            quedaPendiente(cn, idTramite);
            bitacora.insertar(cn, u.getId(), "requisito_articulo", "VINCULACION", null,
                    "Requisito " + idRequisito + " - artículo " + idArticulo);
            return null;
        });
    }

    public void desvincularArticulo(UsuarioAdministrativo u, int idRequisito, int idArticulo) {
        enTransaccion(cn -> {
            Integer idTramite = tramiteDeRequisito(cn, idRequisito);
            conservarVersionPublicada(cn, idTramite);
            tramiteDAO.desvincularArticulo(cn, idRequisito, idArticulo);
            quedaPendiente(cn, idTramite);
            bitacora.insertar(cn, u.getId(), "requisito_articulo", "DESVINCULACION",
                    "Requisito " + idRequisito + " - artículo " + idArticulo, null);
            return null;
        });
    }

    public void agregarRegla(UsuarioAdministrativo u, ReglaRevision r) {
        if (vacio(r.getCriterio()) || vacio(r.getMensajeCorreccion())) {
            throw new ReglaNegocioException("El criterio y el mensaje de corrección son obligatorios.");
        }
        if ("DATO_OBLIGATORIO".equals(r.getTipo())) {
            if (vacio(r.getExpresion())) {
                throw new ReglaNegocioException("Una regla de dato obligatorio necesita una expresión regular.");
            }
            try {
                Pattern.compile(r.getExpresion());
            } catch (PatternSyntaxException e) {
                throw new ReglaNegocioException("La expresión regular no es válida: " + e.getDescription());
            }
        } else {
            r.setTipo("CONFIRMACION_ASISTIDA");
            r.setExpresion(null);
        }
        enTransaccion(cn -> {
            reglaDAO.insertar(cn, r);
            bitacora.insertar(cn, u.getId(), "regla_revision", "REGISTRO", null, "Trámite " + r.getIdTramite() + ": " + r.getCriterio());
            return null;
        });
    }

    public void cambiarRegla(UsuarioAdministrativo u, int idRegla, boolean activa) {
        enTransaccion(cn -> {
            reglaDAO.cambiarActiva(cn, idRegla, activa);
            bitacora.insertar(cn, u.getId(), "regla_revision", activa ? "ACTIVACION" : "DESACTIVACION", null, "Regla " + idRegla);
            return null;
        });
    }

    public void eliminarRegla(UsuarioAdministrativo u, int idRegla) {
        enTransaccion(cn -> {
            reglaDAO.eliminar(cn, idRegla);
            bitacora.insertar(cn, u.getId(), "regla_revision", "ELIMINACION", "Regla " + idRegla, null);
            return null;
        });
    }

    // ------------------------------------------------------------------ normativas (RF-13)
    public int guardarNormativa(UsuarioAdministrativo u, Normativa n) {
        if (vacio(n.getTipo()) || vacio(n.getNumero()) || vacio(n.getTitulo()) || vacio(n.getOrganoEmisor())) {
            throw new ReglaNegocioException("Tipo, número, título y órgano emisor son obligatorios.");
        }
        return enTransaccion(cn -> {
            if (n.getId() == 0) {
                int id = normativaDAO.insertar(cn, n);
                bitacora.insertar(cn, u.getId(), "normativa", "REGISTRO", null, n.getNombreCompleto());
                return id;
            }
            Normativa antes = normativaDAO.buscarPorId(cn, n.getId());
            normativaDAO.actualizar(cn, n);
            bitacora.insertar(cn, u.getId(), "normativa", "EDICION",
                    antes.getNombreCompleto() + (antes.isVigente() ? " (vigente)" : " (no vigente)"),
                    n.getNombreCompleto() + (n.isVigente() ? " (vigente)" : " (no vigente)"));
            return n.getId();
        });
    }

    public void agregarArticulo(UsuarioAdministrativo u, int idNormativa, String numero, String contenido) {
        if (vacio(numero) || vacio(contenido)) {
            throw new ReglaNegocioException("El número y el contenido del artículo son obligatorios.");
        }
        enTransaccion(cn -> {
            normativaDAO.insertarArticulo(cn, idNormativa, numero.trim(), contenido.trim());
            bitacora.insertar(cn, u.getId(), "articulo_normativo", "REGISTRO", null, "Normativa " + idNormativa + ": " + numero);
            return null;
        });
    }

    public void eliminarArticulo(UsuarioAdministrativo u, int idArticulo) {
        enTransaccion(cn -> {
            normativaDAO.eliminarArticulo(cn, idArticulo);
            bitacora.insertar(cn, u.getId(), "articulo_normativo", "ELIMINACION", "Artículo " + idArticulo, null);
            return null;
        });
    }

    private Integer tramiteDeRequisito(java.sql.Connection cn, int idRequisito) throws java.sql.SQLException {
        Integer id = tramiteDAO.tramiteDeRequisito(cn, idRequisito);
        if (id == null) {
            throw new ReglaNegocioException("El requisito no existe.");
        }
        return id;
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static String resumen(Tramite t) {
        return t == null ? null : t.getCodigo() + " | " + t.getNombre() + " | " + t.getPagoTexto() + " | " + t.getPlazoReferencial();
    }
}
