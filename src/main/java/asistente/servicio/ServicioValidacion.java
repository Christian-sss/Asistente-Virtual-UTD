package asistente.servicio;

import asistente.dao.BitacoraDAO;
import asistente.dao.TramiteDAO;
import asistente.modelo.Tramite;
import asistente.modelo.UsuarioAdministrativo;

import java.util.ArrayList;
import java.util.List;

/**
 * Validación y publicación de contenidos por el personal de la UTD (RF-15). El trámite modificado
 * queda PENDIENTE; el personal lo aprueba (se publica) o lo devuelve con observaciones. Solo se
 * publica si cumple RN-10: al menos un paso y requisitos obligatorios con sustento en norma vigente.
 */
public class ServicioValidacion extends ServicioBase {

    private final TramiteDAO tramiteDAO = new TramiteDAO();
    private final BitacoraDAO bitacora = new BitacoraDAO();
    private final ServicioOrientacion orientacion = new ServicioOrientacion();

    public List<Tramite> pendientes() {
        return consultar(cn -> tramiteDAO.listarPorEstado(cn, "PENDIENTE"));
    }

    public List<Tramite> devueltos() {
        return consultar(cn -> tramiteDAO.listarPorEstado(cn, "DEVUELTO"));
    }

    /** Versión propuesta (con los cambios) y versión que hoy ve el público (o null si no está publicado). */
    public record Comparacion(Tramite propuesta, Tramite publicada, List<String> incumplimientos) {
    }

    public Comparacion comparar(int idTramite) {
        return consultar(cn -> {
            Tramite propuesta = tramiteDAO.buscarPorId(cn, idTramite);
            if (propuesta == null) {
                return null;
            }
            return new Comparacion(propuesta, orientacion.versionPublica(cn, idTramite), incumplimientos(cn, propuesta));
        });
    }

    public void aprobar(UsuarioAdministrativo u, int idTramite) {
        enTransaccion(cn -> {
            Tramite t = tramiteDAO.buscarPorId(cn, idTramite);
            if (t == null) {
                throw new ReglaNegocioException("El trámite no existe.");
            }
            if (!t.isPendiente()) {
                throw new ReglaNegocioException("El trámite no tiene cambios pendientes de validación.");
            }
            List<String> faltas = incumplimientos(cn, t);
            if (!faltas.isEmpty()) {
                throw new ReglaNegocioException("No se puede publicar: " + String.join(" ", faltas));
            }
            t.setPublicado(true);
            t.setEstadoContenido("PUBLICADO");
            tramiteDAO.aprobar(cn, idTramite, VersionPublicada.serializar(t));
            bitacora.insertar(cn, u.getId(), "tramite", "PUBLICACION", t.getCodigo() + ": pendiente",
                    t.getCodigo() + ": publicado (validado por " + u.getUsuario() + ")");
            return null;
        });
    }

    public void devolver(UsuarioAdministrativo u, int idTramite, String observacion) {
        if (observacion == null || observacion.isBlank()) {
            throw new ReglaNegocioException("Escriba las observaciones para el administrador de contenidos.");
        }
        String obs = observacion.strip();
        if (obs.length() > 600) {
            obs = obs.substring(0, 600);
        }
        String o = obs;
        enTransaccion(cn -> {
            Tramite t = tramiteDAO.buscarPorId(cn, idTramite);
            if (t == null || !t.isPendiente()) {
                throw new ReglaNegocioException("El trámite no tiene cambios pendientes de validación.");
            }
            tramiteDAO.devolver(cn, idTramite, o);
            bitacora.insertar(cn, u.getId(), "tramite", "DEVOLUCION", t.getCodigo() + ": pendiente",
                    t.getCodigo() + ": devuelto - " + o);
            return null;
        });
    }

    /** RN-10: condiciones mínimas para publicar. */
    private List<String> incumplimientos(java.sql.Connection cn, Tramite t) throws java.sql.SQLException {
        List<String> l = new ArrayList<>();
        if (t.getPasos().isEmpty()) {
            l.add("El trámite debe tener al menos un paso.");
        }
        int sinSustento = tramiteDAO.contarRequisitosSinSustento(cn, t.getId());
        if (sinSustento > 0) {
            l.add(sinSustento + " requisito(s) obligatorio(s) no están vinculados a un artículo de una norma vigente.");
        }
        return l;
    }
}
