package asistente.controlador;

import asistente.modelo.OpcionRespuesta;
import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioContenidos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gestión del árbol de preguntas guiadas y sus opciones (RF-12).
 */
@WebServlet(name = "AdminPreguntaServlet", urlPatterns = "/admin/preguntas")
public class AdminPreguntaServlet extends ServletBase {

    private final ServicioContenidos servicio = new ServicioContenidos();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("preguntas", servicio.preguntas());
        req.setAttribute("tramites", servicio.listarTramites());
        vista(req, resp, "admin/preguntas");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioAdministrativo u = usuario(req);
        String accion = req.getParameter("accion");
        Integer idPregunta = entero(req, "idPregunta");
        try {
            switch (accion == null ? "" : accion) {
                case "pregunta" -> {
                    servicio.guardarPregunta(u, idPregunta == null ? 0 : idPregunta, texto(req, "texto"));
                    mensaje(req, "success", "Pregunta guardada.");
                }
                case "eliminarPregunta" -> {
                    servicio.eliminarPregunta(u, idPregunta);
                    mensaje(req, "success", "Pregunta eliminada.");
                }
                case "opcion" -> {
                    OpcionRespuesta o = new OpcionRespuesta();
                    o.setIdPregunta(idPregunta);
                    o.setTexto(texto(req, "texto"));
                    String destino = req.getParameter("destino");
                    if (destino != null && destino.startsWith("P")) {
                        o.setIdPreguntaSiguiente(Integer.valueOf(destino.substring(1)));
                    } else if (destino != null && destino.startsWith("T")) {
                        o.setIdTramite(Integer.valueOf(destino.substring(1)));
                    }
                    servicio.agregarOpcion(u, o);
                    mensaje(req, "success", "Opción agregada.");
                }
                case "eliminarOpcion" -> {
                    servicio.eliminarOpcion(u, enteroObligatorio(req, "idOpcion"));
                    mensaje(req, "success", "Opción eliminada.");
                }
                default -> mensaje(req, "warning", "Acción no reconocida.");
            }
        } catch (ReglaNegocioException | NumberFormatException e) {
            mensaje(req, "danger", e.getMessage());
        }
        redirigir(req, resp, "/admin/preguntas" + (idPregunta == null ? "" : "#p" + idPregunta));
    }
}
