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
    private final asistente.servicio.ServicioEstadistica estadistica = new asistente.servicio.ServicioEstadistica();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = entero(req, "id");
        Tramite t = id == null ? null : servicio.obtenerTramitePublicado(id);
        if (t == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El trámite no existe o no está publicado.");
            return;
        }
        boolean pdf = "/hoja-ruta/pdf".equals(req.getServletPath());
        estadistica.registrar(pdf ? "PDF" : "HOJA_RUTA", t.getId(), null);
        if (pdf) {
            byte[] archivo = new GeneradorHojaRutaPdf().generar(t);
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"hoja-de-ruta-" + t.getCodigo() + ".pdf\"");
            resp.setContentLength(archivo.length);
            resp.getOutputStream().write(archivo);
            return;
        }
        req.setAttribute("tramite", t);
        req.setAttribute("aviso", GeneradorHojaRutaPdf.AVISO_REFERENCIAL);
        req.setAttribute("identificado", req.getParameter("identificado") != null);
        req.setAttribute("secuencia", t.getSecuenciaVisita());
        vista(req, resp, "hoja-ruta");
    }
}
