package asistente.controlador;

import asistente.modelo.UsuarioAdministrativo;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

/**
 * Protege /admin/**: exige sesión y rol. PERSONAL_UTD solo consulta; ADMIN_CONTENIDOS administra.
 * Además valida el token CSRF de todo envío POST del panel.
 */
public class AuthFilter implements Filter {

    /** Rutas que el rol PERSONAL_UTD puede usar (consulta y cambio de su contraseña). */
    private static final Set<String> RUTAS_PERSONAL = Set.of("/admin", "/admin/", "/admin/tramites", "/admin/bitacora",
            "/admin/contrasena");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession s = req.getSession(false);
        UsuarioAdministrativo u = s == null ? null : (UsuarioAdministrativo) s.getAttribute(ServletBase.USUARIO);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String ruta = req.getRequestURI().substring(req.getContextPath().length());
        boolean post = "POST".equalsIgnoreCase(req.getMethod());
        if ("PERSONAL_UTD".equals(u.getRol()) && (!RUTAS_PERSONAL.contains(ruta) || post && !ruta.equals("/admin/contrasena"))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Su rol no tiene permiso para esta operación.");
            return;
        }
        if (post && !ServletBase.csrfValido(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida (token de seguridad).");
            return;
        }
        chain.doFilter(req, resp);
    }
}
