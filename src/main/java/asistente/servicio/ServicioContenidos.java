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
 * Administración de contenidos: trámites, pasos y preguntas guiadas (RF-12); requisitos, reglas de
 * revisión y normativas (RF-13). Cada cambio se registra en la bitácora en la misma transacción.
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

    public int[] estadisticas() {
        return consultar(cn -> new int[]{
                tramiteDAO.contar(cn, ""),
                tramiteDAO.contar(cn, "WHERE estado_contenido = 'PUBLICADO'"),
                normativaDAO.buscar(cn, null, null).size(),
                preguntaDAO.listarTodas(cn).size()});
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
            Tramite antes = tramiteDAO.buscarPorId(cn, t.getId());
            tramiteDAO.actualizar(cn, t);
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

    /** Publica (con validación) o despublica un trámite. */
    public void cambiarEstado(UsuarioAdministrativo u, int idTramite, boolean publicar) {
        enTransaccion(cn -> {
            Tramite t = tramiteDAO.buscarPorId(cn, idTramite);
            if (t == null) {
                throw new ReglaNegocioException("El trámite no existe.");
            }
            if (publicar) {
                if (t.getPasos().isEmpty()) {
                    throw new ReglaNegocioException("No se puede publicar: el trámite debe tener al menos un paso.");
                }
                int sinSustento = tramiteDAO.contarRequisitosSinSustento(cn, idTramite);
                if (sinSustento > 0) {
                    throw new ReglaNegocioException("No se puede publicar: " + sinSustento
                            + " requisito(s) obligatorio(s) no están vinculados a un artículo de una norma vigente.");
                }
            }
            String nuevo = publicar ? "PUBLICADO" : "BORRADOR";
            tramiteDAO.cambiarEstado(cn, idTramite, nuevo);
            bitacora.insertar(cn, u.getId(), "tramite", publicar ? "PUBLICACION" : "DESPUBLICACION",
                    t.getCodigo() + ": " + t.getEstadoContenido(), t.getCodigo() + ": " + nuevo);
            return null;
        });
    }

    public void agregarPaso(UsuarioAdministrativo u, Paso p) {
        if (vacio(p.getDescripcion())) {
            throw new ReglaNegocioException("Escriba la descripción del paso.");
        }
        enTransaccion(cn -> {
            tramiteDAO.insertarPaso(cn, p);
            bitacora.insertar(cn, u.getId(), "paso", "REGISTRO", null, "Trámite " + p.getIdTramite() + ": " + p.getDescripcion());
            return null;
        });
    }

    public void eliminarPaso(UsuarioAdministrativo u, int idTramite, int idPaso) {
        enTransaccion(cn -> {
            tramiteDAO.eliminarPaso(cn, idPaso);
            bitacora.insertar(cn, u.getId(), "paso", "ELIMINACION", "Trámite " + idTramite + ", paso " + idPaso, null);
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
            int id = tramiteDAO.insertarRequisito(cn, r);
            if (idArticulo != null) {
                tramiteDAO.vincularArticulo(cn, id, idArticulo);
            }
            bitacora.insertar(cn, u.getId(), "requisito", "REGISTRO", null, "Trámite " + r.getIdTramite() + ": " + r.getDescripcion());
            return null;
        });
    }

    public void eliminarRequisito(UsuarioAdministrativo u, int idTramite, int idRequisito) {
        enTransaccion(cn -> {
            tramiteDAO.eliminarRequisito(cn, idRequisito);
            bitacora.insertar(cn, u.getId(), "requisito", "ELIMINACION", "Trámite " + idTramite + ", requisito " + idRequisito, null);
            return null;
        });
    }

    public void vincularArticulo(UsuarioAdministrativo u, int idRequisito, int idArticulo) {
        enTransaccion(cn -> {
            tramiteDAO.vincularArticulo(cn, idRequisito, idArticulo);
            bitacora.insertar(cn, u.getId(), "requisito_articulo", "VINCULACION", null,
                    "Requisito " + idRequisito + " - artículo " + idArticulo);
            return null;
        });
    }

    public void desvincularArticulo(UsuarioAdministrativo u, int idRequisito, int idArticulo) {
        enTransaccion(cn -> {
            tramiteDAO.desvincularArticulo(cn, idRequisito, idArticulo);
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

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static String resumen(Tramite t) {
        return t == null ? null : t.getCodigo() + " | " + t.getNombre() + " | " + t.getPagoTexto() + " | " + t.getPlazoReferencial();
    }
}
