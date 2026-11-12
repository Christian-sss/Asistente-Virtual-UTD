package asistente.controlador;

import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ServicioUsuarios;
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
 * Protege /admin/** (RN-02): exige sesión, cuenta activa y rol.
 * <ul>
 *   <li>PERSONAL_UTD valida y publica contenidos (RF-15), consulta reportes (RF-16), trámites y bitácora.</li>
 *   <li>ADMIN_CONTENIDOS administra los contenidos y usuarios; no publica (solo la UTD valida).</li>
 * </ul>
 * Además valida el token CSRF de todo envío POST del panel.
 */
public class AuthFilter implements Filter {

    /** Rutas que el rol PERSONAL_UTD puede consultar (GET). */
    private static final Set<String> CONSULTA_PERSONAL = Set.of("/admin", "/admin/", "/admin/tramites", "/admin/bitacora",
            "/admin/contrasena", "/admin/validacion", "/admin/reportes", "/admin/reportes/csv");
    /** Rutas en las que el rol PERSONAL_UTD puede enviar cambios (POST). */
    private static final Set<String> ESCRITURA_PERSONAL = Set.of("/admin/contrasena", "/admin/validacion", "/admin/reportes");

    private final ServicioUsuarios usuarios = new ServicioUsuarios();

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
        // Si la cuenta fue desactivada o cambió de rol mientras la sesión estaba abierta (RF-02)
        UsuarioAdministrativo vigente = usuarios.vigente(u.getId());
        if (vigente == null) {
            s.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login?desactivado=1");
            return;
        }
        if (!vigente.getRol().equals(u.getRol()) || !vigente.getNombreCompleto().equals(u.getNombreCompleto())) {
            s.setAttribute(ServletBase.USUARIO, vigente);
            u = vigente;
        }
        String ruta = req.getRequestURI().substring(req.getContextPath().length());
        boolean post = "POST".equalsIgnoreCase(req.getMethod());
        boolean permitido = switch (u.getRol()) {
            case "PERSONAL_UTD" -> post ? ESCRITURA_PERSONAL.contains(ruta) : CONSULTA_PERSONAL.contains(ruta);
            case "ADMIN_CONTENIDOS" -> !(post && ruta.equals("/admin/validacion"));
            default -> false;
        };
        if (!permitido) {
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
