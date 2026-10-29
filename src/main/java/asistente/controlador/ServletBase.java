package asistente.controlador;

import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ReglaNegocioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Utilidades comunes de los controladores: vistas, parámetros, mensajes y protección CSRF.
 */
public abstract class ServletBase extends HttpServlet {

    static final String USUARIO = "usuarioAdministrativo";
    static final String CSRF = "csrfToken";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    protected void vista(HttpServletRequest req, HttpServletResponse resp, String nombre)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/vistas/" + nombre + ".jsp").forward(req, resp);
    }

    protected void redirigir(HttpServletRequest req, HttpServletResponse resp, String ruta) throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    protected Integer entero(HttpServletRequest req, String nombre) {
        String v = req.getParameter(nombre);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected int enteroObligatorio(HttpServletRequest req, String nombre) {
        Integer v = entero(req, nombre);
        if (v == null) {
            throw new ReglaNegocioException("Falta el dato «" + nombre + "».");
        }
        return v;
    }

    protected BigDecimal decimal(HttpServletRequest req, String nombre) {
        String v = req.getParameter(nombre);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(v.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El monto «" + v + "» no es válido.");
        }
    }

    protected String texto(HttpServletRequest req, String nombre) {
        String v = req.getParameter(nombre);
        return v == null || v.isBlank() ? null : v.trim();
    }

    /** Mensaje que se muestra una sola vez en la siguiente vista (tipo: success, danger, warning, info). */
    protected void mensaje(HttpServletRequest req, String tipo, String texto) {
        req.getSession().setAttribute("flashTipo", tipo);
        req.getSession().setAttribute("flashTexto", texto);
    }

    protected UsuarioAdministrativo usuario(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (UsuarioAdministrativo) s.getAttribute(USUARIO);
    }

    /** Token CSRF de la sesión (se crea si no existe). */
    static String tokenCsrf(HttpSession s) {
        String t = (String) s.getAttribute(CSRF);
        if (t == null) {
            byte[] b = new byte[24];
            ALEATORIO.nextBytes(b);
            t = HexFormat.of().formatHex(b);
            s.setAttribute(CSRF, t);
        }
        return t;
    }

    static boolean csrfValido(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        String esperado = s == null ? null : (String) s.getAttribute(CSRF);
        return esperado != null && esperado.equals(req.getParameter("_csrf"));
    }

    static String tokenAleatorio() {
        byte[] b = new byte[32];
        ALEATORIO.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }
}
