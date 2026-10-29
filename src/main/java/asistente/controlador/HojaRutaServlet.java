package asistente.controlador;

import asistente.modelo.Tramite;
import asistente.servicio.GeneradorHojaRutaPdf;
import asistente.servicio.ServicioOrientacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Hoja de ruta del trámite en pantalla y en PDF, con el aviso de carácter referencial (RF-05).
 */
@WebServlet(name = "HojaRutaServlet", urlPatterns = {"/hoja-ruta", "/hoja-ruta/pdf"})
public class HojaRutaServlet extends ServletBase {

    private final ServicioOrientacion servicio = new ServicioOrientacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = entero(req, "id");
        Tramite t = id == null ? null : servicio.obtenerTramitePublicado(id);
        if (t == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El trámite no existe o no está publicado.");
            return;
        }
        if ("/hoja-ruta/pdf".equals(req.getServletPath())) {
            byte[] pdf = new GeneradorHojaRutaPdf().generar(t);
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"hoja-de-ruta-" + t.getCodigo() + ".pdf\"");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
            return;
        }
        req.setAttribute("tramite", t);
        req.setAttribute("aviso", GeneradorHojaRutaPdf.AVISO_REFERENCIAL);
        req.setAttribute("identificado", req.getParameter("identificado") != null);
        vista(req, resp, "hoja-ruta");
    }
}
