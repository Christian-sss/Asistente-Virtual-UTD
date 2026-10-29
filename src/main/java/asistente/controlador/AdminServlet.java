package asistente.controlador;

import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioAutenticacion;
import asistente.servicio.ServicioContenidos;
import asistente.servicio.ServicioLimpieza;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Panel de administración: resumen, bitácora, cambio de contraseña y limpieza manual.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin", "/admin/bitacora", "/admin/contrasena", "/admin/limpieza"})
public class AdminServlet extends ServletBase {

    private final ServicioContenidos contenidos = new ServicioContenidos();
    private final ServicioLimpieza limpieza = new ServicioLimpieza();
    private final ServicioAutenticacion autenticacion = new ServicioAutenticacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/admin/bitacora" -> {
                req.setAttribute("bitacora", contenidos.bitacora(200));
                vista(req, resp, "admin/bitacora");
            }
            case "/admin/contrasena" -> vista(req, resp, "admin/contrasena");
            case "/admin/limpieza" -> redirigir(req, resp, "/admin");
            default -> {
                req.setAttribute("estadisticas", contenidos.estadisticas());
                req.setAttribute("temporales", limpieza.documentosTemporales());
                req.setAttribute("ultimaLimpieza", limpieza.ultimaEjecucion());
                req.setAttribute("bitacora", contenidos.bitacora(8));
                vista(req, resp, "admin/panel");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/admin/contrasena".equals(req.getServletPath())) {
                autenticacion.cambiarContrasena(usuario(req), req.getParameter("actual"), req.getParameter("nueva"),
                        req.getParameter("confirmacion"));
                mensaje(req, "success", "Su contraseña se actualizó correctamente.");
                redirigir(req, resp, "/admin");
                return;
            }
            if ("/admin/limpieza".equals(req.getServletPath())) {
                int n = limpieza.eliminarVencidos("MANUAL");
                mensaje(req, "success", "Limpieza ejecutada: " + n + " documento(s) vencido(s) eliminado(s).");
            }
            redirigir(req, resp, "/admin");
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
            redirigir(req, resp, req.getServletPath());
        }
    }
}
