package asistente.controlador;

import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioUsuarios;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gestión de usuarios administrativos y roles (RF-02): registro, edición, activación,
 * desactivación lógica (RN-15) y contraseña temporal. Solo ADMIN_CONTENIDOS.
 */
@WebServlet(name = "AdminUsuarioServlet", urlPatterns = "/admin/usuarios")
public class AdminUsuarioServlet extends ServletBase {

    private final ServicioUsuarios servicio = new ServicioUsuarios();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("usuarios", servicio.listar());
        Integer id = entero(req, "id");
        if (id != null) {
            req.setAttribute("editar", servicio.buscar(id));
        } else if (req.getParameter("nuevo") != null) {
            UsuarioAdministrativo n = new UsuarioAdministrativo();
            n.setRol("PERSONAL_UTD");
            req.setAttribute("editar", n);
        }
        vista(req, resp, "admin/usuarios");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioAdministrativo actor = usuario(req);
        String accion = req.getParameter("accion");
        Integer id = entero(req, "id");
        try {
            switch (accion == null ? "" : accion) {
                case "guardar" -> {
                    UsuarioAdministrativo u = new UsuarioAdministrativo();
                    u.setNombreCompleto(texto(req, "nombreCompleto"));
                    u.setRol(texto(req, "rol"));
                    if (id == null) {
                        u.setUsuario(texto(req, "usuario"));
                        id = servicio.registrar(actor, u, req.getParameter("contrasena"));
                        mensaje(req, "success", "Usuario registrado. Entréguele la contraseña temporal para que la cambie al ingresar.");
                    } else {
                        u.setId(id);
                        servicio.actualizar(actor, u);
                        mensaje(req, "success", "Datos del usuario actualizados.");
                    }
                }
                case "activar", "desactivar" -> {
                    servicio.cambiarActivo(actor, enteroObligatorio(req, "id"), "activar".equals(accion));
                    mensaje(req, "success", "activar".equals(accion) ? "Usuario activado." : "Usuario desactivado; ya no podrá ingresar al panel.");
                }
                case "contrasena" -> {
                    servicio.restablecerContrasena(actor, enteroObligatorio(req, "id"), req.getParameter("contrasena"));
                    mensaje(req, "success", "Contraseña temporal asignada.");
                }
                default -> mensaje(req, "warning", "Acción no reconocida.");
            }
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
            if (id == null) {
                redirigir(req, resp, "/admin/usuarios?nuevo=1");
                return;
            }
        }
        redirigir(req, resp, "/admin/usuarios" + (id == null ? "" : "?id=" + id));
    }
}
