package asistente.controlador;

import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ServicioAutenticacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Inicio y cierre de sesión del usuario administrativo (RF-01).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/logout"})
public class LoginServlet extends ServletBase {

    private final ServicioAutenticacion servicio = new ServicioAutenticacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (usuario(req) != null) {
            redirigir(req, resp, "/admin");
            return;
        }
        vista(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!csrfValido(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida (token de seguridad).");
            return;
        }
        if ("/logout".equals(req.getServletPath())) {
            UsuarioAdministrativo u = usuario(req);
            if (u != null) {
                servicio.cerrarSesion(u);
            }
            req.getSession().invalidate();
            redirigir(req, resp, "/login?salida=1");
            return;
        }
        UsuarioAdministrativo u = servicio.autenticar(texto(req, "usuario"), req.getParameter("contrasena"));
        if (u == null) {
            req.setAttribute("error", "Usuario o contraseña incorrectos, o usuario desactivado.");
            req.setAttribute("usuarioIngresado", texto(req, "usuario"));
            vista(req, resp, "login");
            return;
        }
        // Nueva sesión para evitar la fijación de sesión.
        req.getSession().invalidate();
        HttpSession s = req.getSession(true);
        s.setAttribute(USUARIO, u);
        s.setMaxInactiveInterval(30 * 60);
        tokenCsrf(s);
        redirigir(req, resp, "/admin");
    }
}
