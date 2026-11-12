package asistente.controlador;

import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioValidacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Validación y publicación de contenidos (RF-15): el personal de la UTD revisa los trámites
 * pendientes y los aprueba para su publicación o los devuelve con observaciones.
 */
@WebServlet(name = "AdminValidacionServlet", urlPatterns = "/admin/validacion")
public class AdminValidacionServlet extends ServletBase {

    private final ServicioValidacion servicio = new ServicioValidacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("pendientes", servicio.pendientes());
        req.setAttribute("devueltos", servicio.devueltos());
        Integer id = entero(req, "id");
        if (id != null) {
            ServicioValidacion.Comparacion c = servicio.comparar(id);
            if (c == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El trámite no existe.");
                return;
            }
            req.setAttribute("comparacion", c);
        }
        vista(req, resp, "admin/validacion");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id;
        try {
            id = enteroObligatorio(req, "id");
            if ("aprobar".equals(req.getParameter("accion"))) {
                servicio.aprobar(usuario(req), id);
                mensaje(req, "success", "Trámite aprobado y publicado.");
            } else {
                servicio.devolver(usuario(req), id, req.getParameter("observacion"));
                mensaje(req, "success", "Trámite devuelto con observaciones al administrador de contenidos.");
            }
            redirigir(req, resp, "/admin/validacion");
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
            Integer i = entero(req, "id");
            redirigir(req, resp, "/admin/validacion" + (i == null ? "" : "?id=" + i));
        }
    }
}
