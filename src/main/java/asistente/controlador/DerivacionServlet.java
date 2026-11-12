package asistente.controlador;

import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioDerivacion;
import asistente.servicio.ServicioEstadistica;
import asistente.servicio.ServicioOrientacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Cargo, expediente y seguimiento, y derivación a la dependencia competente (RF-10, RN-13), y
 * registro de retroalimentación anónima sobre información inexacta (RF-11). Solo orienta: no
 * consulta el sistema institucional ni asigna números de expediente (RN-16).
 */
@WebServlet(name = "DerivacionServlet", urlPatterns = {"/derivacion", "/retroalimentacion"})
public class DerivacionServlet extends ServletBase {

    /** Máximo de reportes por sesión, para evitar envíos masivos. */
    private static final int MAX_REPORTES_SESION = 5;

    private final ServicioDerivacion servicio = new ServicioDerivacion();
    private final ServicioEstadistica estadistica = new ServicioEstadistica();
    private final ServicioOrientacion orientacion = new ServicioOrientacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/retroalimentacion".equals(req.getServletPath())) {
            redirigir(req, resp, "/derivacion#retroalimentacion");
            return;
        }
        String consulta = texto(req, "consulta");
        if (consulta != null && consulta.length() > 300) {
            consulta = consulta.substring(0, 300);
        }
        if (consulta != null) {
            List<ServicioDerivacion.Derivacion> resultados = servicio.derivar(consulta);
            req.setAttribute("resultados", resultados);
            estadistica.registrar("DERIVACION", null, resultados.isEmpty() ? null : resultados.get(0).dependencia().getId());
        }
        String expediente = texto(req, "expediente");
        if (expediente != null) {
            req.setAttribute("expediente", expediente);
            req.setAttribute("expedienteValido", expediente.matches("\\d{7}"));
        }
        req.setAttribute("consulta", consulta);
        req.setAttribute("tramites", orientacion.buscarTramites(null, null));
        vista(req, resp, "derivacion");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!csrfValido(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida (token de seguridad).");
            return;
        }
        Integer idTramite = entero(req, "tramite");
        String volver = idTramite != null && "hoja".equals(req.getParameter("origen"))
                ? "/hoja-ruta?id=" + idTramite + "#retroalimentacion" : "/derivacion#retroalimentacion";
        HttpSession s = req.getSession();
        Integer enviados = (Integer) s.getAttribute("reportesEnviados");
        if (enviados != null && enviados >= MAX_REPORTES_SESION) {
            mensaje(req, "warning", "Ya registramos varios reportes suyos en esta sesión. Gracias por su ayuda.");
            redirigir(req, resp, volver);
            return;
        }
        try {
            if (idTramite != null && orientacion.obtenerTramitePublicado(idTramite) == null) {
                idTramite = null;
            }
            estadistica.enviarRetroalimentacion(idTramite, texto(req, "tipo"), req.getParameter("comentario"));
            s.setAttribute("reportesEnviados", enviados == null ? 1 : enviados + 1);
            mensaje(req, "success", "Gracias. Su reporte fue enviado al personal de la UTD para su revisión.");
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
        }
        redirigir(req, resp, volver);
    }
}
