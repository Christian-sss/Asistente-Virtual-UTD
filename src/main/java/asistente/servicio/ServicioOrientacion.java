package asistente.servicio;

import asistente.dao.NormativaDAO;
import asistente.dao.PreguntaGuiaDAO;
import asistente.dao.TramiteDAO;
import asistente.modelo.ArticuloNormativo;
import asistente.modelo.Categoria;
import asistente.modelo.OpcionRespuesta;
import asistente.modelo.Paso;
import asistente.modelo.PreguntaGuia;
import asistente.modelo.PuntoAtencion;
import asistente.modelo.Requisito;
import asistente.modelo.Tramite;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catálogo de trámites (RF-03), orientación con preguntas guiadas (RF-04) y hoja de ruta (RF-05).
 */
public class ServicioOrientacion extends ServicioBase {

    private final TramiteDAO tramiteDAO = new TramiteDAO();
    private final PreguntaGuiaDAO preguntaDAO = new PreguntaGuiaDAO();
    private final NormativaDAO normativaDAO = new NormativaDAO();

    /** Categorías con el número de trámites visibles al público. */
    public List<Categoria> listarCategorias() {
        return consultar(cn -> {
            List<Categoria> categorias = tramiteDAO.listarCategorias(cn);
            Map<Integer, Integer> totales = new HashMap<>();
            for (Tramite t : visibles(cn)) {
                totales.merge(t.getIdCategoria(), 1, Integer::sum);
            }
            for (Categoria c : categorias) {
                c.setTotalTramites(totales.getOrDefault(c.getId(), 0));
            }
            return categorias;
        });
    }

    /** Catálogo público: versión publicada de cada trámite, filtrada por palabra clave y categoría (RF-03). */
    public List<Tramite> buscarTramites(String texto, Integer idCategoria) {
        String q = normalizar(texto);
        return consultar(cn -> {
            List<Tramite> l = new ArrayList<>();
            for (Tramite t : visibles(cn)) {
                if (idCategoria != null && t.getIdCategoria() != idCategoria) {
                    continue;
                }
                if (!q.isEmpty() && !(normalizar(t.getNombre()) + " " + normalizar(t.getDescripcion()) + " "
                        + normalizar(t.getCodigoTupa()) + " " + normalizar(t.getCategoria())).contains(q)) {
                    continue;
                }
                l.add(t);
            }
            return l;
        });
    }

    /** Trámite publicado con pasos, requisitos y sustento normativo; null si no existe o no está publicado. */
    public Tramite obtenerTramitePublicado(int id) {
        return consultar(cn -> versionPublica(cn, id));
    }

    /**
     * Versión que ve el público: la última aprobada por la UTD (RN-11). Los datos de ubicación y la
     * vigencia de las normas se actualizan con los valores vigentes, que se mantienen aparte (RF-14).
     */
    Tramite versionPublica(Connection cn, int id) throws SQLException {
        Tramite fila = tramiteDAO.buscarPorId(cn, id);
        if (fila == null || !fila.isPublicado()) {
            return null;
        }
        Tramite v = VersionPublicada.leer(tramiteDAO.leerVersionPublicada(cn, id));
        if (v == null) {
            return fila;
        }
        v.setPublicado(true);
        v.setEstadoContenido("PUBLICADO");
        v.setCategoria(fila.getCategoria());
        refrescar(cn, v);
        return v;
    }

    private List<Tramite> visibles(Connection cn) throws SQLException {
        Map<Integer, String> categorias = new HashMap<>();
        Map<Integer, Integer> orden = new HashMap<>();
        for (Categoria c : tramiteDAO.listarCategorias(cn)) {
            categorias.put(c.getId(), c.getNombre());
            orden.put(c.getId(), c.getOrden());
        }
        List<Tramite> l = new ArrayList<>();
        for (Tramite fila : tramiteDAO.buscarPublicados(cn, null, null)) {
            Tramite v = VersionPublicada.leer(tramiteDAO.leerVersionPublicada(cn, fila.getId()));
            if (v == null) {
                v = fila;
            }
            v.setCategoria(categorias.getOrDefault(v.getIdCategoria(), fila.getCategoria()));
            l.add(v);
        }
        l.sort(Comparator.comparing((Tramite t) -> orden.getOrDefault(t.getIdCategoria(), 999))
                .thenComparing(Tramite::getNombre, String.CASE_INSENSITIVE_ORDER));
        return l;
    }

    private void refrescar(Connection cn, Tramite t) throws SQLException {
        Map<Integer, PuntoAtencion> puntos = new HashMap<>();
        for (PuntoAtencion p : tramiteDAO.listarPuntos(cn)) {
            puntos.put(p.getId(), p);
        }
        t.setPunto(t.getIdPunto() == null ? null : puntos.get(t.getIdPunto()));
        for (Paso p : t.getPasos()) {
            p.setPunto(p.getIdPunto() == null ? null : puntos.get(p.getIdPunto()));
        }
        Map<Integer, Boolean> vigencia = new HashMap<>();
        for (ArticuloNormativo a : normativaDAO.listarTodosArticulos(cn)) {
            vigencia.put(a.getId(), a.isVigente());
        }
        for (Requisito r : t.getRequisitos()) {
            r.getArticulos().removeIf(a -> !vigencia.containsKey(a.getId()));
            for (ArticuloNormativo a : r.getArticulos()) {
                a.setVigente(vigencia.get(a.getId()));
            }
        }
    }

    /** Minúsculas y sin tildes, para búsquedas tolerantes. */
    static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    public PreguntaGuia preguntaInicial() {
        return consultar(preguntaDAO::buscarInicial);
    }

    public PreguntaGuia pregunta(int id) {
        return consultar(cn -> preguntaDAO.buscarPorId(cn, id));
    }

    /** Resultado de responder una opción: la siguiente pregunta o el trámite identificado. */
    public record Siguiente(PreguntaGuia pregunta, Tramite tramite, String opcionElegida) {
    }

    public Siguiente resolverSiguiente(int idOpcion) {
        return consultar(cn -> {
            OpcionRespuesta o = preguntaDAO.buscarOpcion(cn, idOpcion);
            if (o == null) {
                throw new ReglaNegocioException("La opción seleccionada no existe.");
            }
            if (o.getIdTramite() != null) {
                Tramite t = tramiteDAO.buscarPorId(cn, o.getIdTramite());
                if (t == null || !t.isPublicado()) {
                    throw new ReglaNegocioException("El trámite de esta opción aún no está publicado. Consulte en la UTD.");
                }
                return new Siguiente(null, t, o.getTexto());
            }
            if (o.getIdPreguntaSiguiente() != null) {
                return new Siguiente(preguntaDAO.buscarPorId(cn, o.getIdPreguntaSiguiente()), null, o.getTexto());
            }
            throw new ReglaNegocioException("Esta opción todavía no tiene un destino configurado.");
        });
    }
}
