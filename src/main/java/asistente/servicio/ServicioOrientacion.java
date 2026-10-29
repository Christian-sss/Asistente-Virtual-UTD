package asistente.servicio;

import asistente.dao.PreguntaGuiaDAO;
import asistente.dao.TramiteDAO;
import asistente.modelo.Categoria;
import asistente.modelo.OpcionRespuesta;
import asistente.modelo.PreguntaGuia;
import asistente.modelo.Tramite;

import java.util.List;

/**
 * Catálogo de trámites (RF-03), orientación con preguntas guiadas (RF-04) y hoja de ruta (RF-05).
 */
public class ServicioOrientacion extends ServicioBase {

    private final TramiteDAO tramiteDAO = new TramiteDAO();
    private final PreguntaGuiaDAO preguntaDAO = new PreguntaGuiaDAO();

    public List<Categoria> listarCategorias() {
        return consultar(tramiteDAO::listarCategorias);
    }

    public List<Tramite> buscarTramites(String texto, Integer idCategoria) {
        return consultar(cn -> tramiteDAO.buscarPublicados(cn, texto, idCategoria));
    }

    /** Trámite publicado con pasos, requisitos y sustento normativo; null si no existe o no está publicado. */
    public Tramite obtenerTramitePublicado(int id) {
        Tramite t = consultar(cn -> tramiteDAO.buscarPorId(cn, id));
        return t != null && t.isPublicado() ? t : null;
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
