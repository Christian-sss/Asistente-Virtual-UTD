package asistente.controlador;

import asistente.modelo.Normativa;
import asistente.servicio.ServicioNormativa;
import asistente.servicio.ServicioOrientacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Consulta de normativa: búsqueda por trámite o palabra clave y detalle con artículos (RF-08).
 */
@WebServlet(name = "NormativaServlet", urlPatterns = {"/normativa", "/normativa/detalle"})
public class NormativaServlet extends ServletBase {

    private final ServicioNormativa servicio = new ServicioNormativa();
    private final ServicioOrientacion orientacion = new ServicioOrientacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/normativa/detalle".equals(req.getServletPath())) {
            Integer id = entero(req, "id");
            Normativa n = id == null ? null : servicio.detalle(id);
            if (n == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "La normativa no existe.");
                return;
            }
            req.setAttribute("normativa", n);
            vista(req, resp, "normativa-detalle");
            return;
        }
        String q = texto(req, "q");
        Integer tramite = entero(req, "tramite");
        req.setAttribute("q", q);
        req.setAttribute("tramiteSel", tramite);
        req.setAttribute("tramites", orientacion.buscarTramites(null, null));
        req.setAttribute("normativas", servicio.buscar(q, tramite));
        vista(req, resp, "normativa");
    }
}
