package asistente.controlador;

import asistente.dao.EstadisticaDAO.Reporte;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioEstadistica;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Reportes estadísticos y de bitácora por periodo, con exportación a CSV (RF-16), y atención de la
 * retroalimentación recibida (RF-11). Disponible para PERSONAL_UTD y ADMIN_CONTENIDOS.
 */
@WebServlet(name = "ReporteServlet", urlPatterns = {"/admin/reportes", "/admin/reportes/csv"})
public class ReporteServlet extends ServletBase {

    private final ServicioEstadistica servicio = new ServicioEstadistica();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate hasta = fecha(req, "hasta");
        LocalDate desde = fecha(req, "desde");
        if (hasta == null) {
            hasta = LocalDate.now();
        }
        if (desde == null) {
            desde = hasta.minusDays(29);
        }
        String tipo = texto(req, "tipo");
        if (tipo == null || !ServicioEstadistica.REPORTES.contains(tipo)) {
            tipo = "consultas";
        }
        Reporte r;
        try {
            r = servicio.reporte(tipo, desde, hasta);
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
            redirigir(req, resp, "/admin/reportes");
            return;
        }
        if ("/admin/reportes/csv".equals(req.getServletPath())) {
            servicio.registrarExportacion(usuario(req), tipo, desde, hasta);
            byte[] csv = ServicioEstadistica.aCsv(r).getBytes(StandardCharsets.UTF_8);
            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"reporte-" + tipo + "-" + desde + "-a-" + hasta + ".csv\"");
            resp.setContentLength(csv.length);
            resp.getOutputStream().write(csv);
            return;
        }
        req.setAttribute("tipo", tipo);
        req.setAttribute("desde", desde);
        req.setAttribute("hasta", hasta);
        req.setAttribute("reporte", r);
        if ("retroalimentacion".equals(tipo)) {
            req.setAttribute("retroalimentacion", servicio.retroalimentacion(desde, hasta));
        }
        vista(req, resp, "admin/reportes");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            servicio.marcarAtendida(usuario(req), enteroObligatorio(req, "id"), req.getParameter("atendida") != null);
            mensaje(req, "success", "Estado de la retroalimentación actualizado.");
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
        }
        LocalDate desde = fecha(req, "desde");
        LocalDate hasta = fecha(req, "hasta");
        redirigir(req, resp, "/admin/reportes?tipo=retroalimentacion"
                + (desde == null ? "" : "&desde=" + desde) + (hasta == null ? "" : "&hasta=" + hasta));
    }
}
