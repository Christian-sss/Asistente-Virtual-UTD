package asistente.servicio;

import asistente.dao.BitacoraDAO;
import asistente.dao.UsuarioAdministrativoDAO;
import asistente.modelo.UsuarioAdministrativo;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Set;

/**
 * Gestión de usuarios administrativos y roles (RF-02). Los usuarios no se eliminan: se desactivan
 * para conservar la trazabilidad de la bitácora (RN-15). Cada cambio queda registrado.
 */
public class ServicioUsuarios extends ServicioBase {

    public static final Set<String> ROLES = Set.of("ADMIN_CONTENIDOS", "PERSONAL_UTD");

    private final UsuarioAdministrativoDAO dao = new UsuarioAdministrativoDAO();
    private final BitacoraDAO bitacora = new BitacoraDAO();

    public List<UsuarioAdministrativo> listar() {
        return consultar(dao::listar);
    }

    public UsuarioAdministrativo buscar(int id) {
        UsuarioAdministrativo u = consultar(cn -> dao.buscarPorId(cn, id));
        if (u != null) {
            u.setHashContrasena(null);
        }
        return u;
    }

    public int registrar(UsuarioAdministrativo actor, UsuarioAdministrativo nuevo, String contrasena) {
        validarDatos(nuevo);
        if (nuevo.getUsuario() == null || !nuevo.getUsuario().matches("[a-zA-Z0-9._-]{3,60}")) {
            throw new ReglaNegocioException("El usuario debe tener de 3 a 60 caracteres: letras, números, punto, guion o guion bajo.");
        }
        validarContrasena(contrasena);
        return enTransaccion(cn -> {
            if (dao.buscarPorUsuario(cn, nuevo.getUsuario()) != null) {
                throw new ReglaNegocioException("Ya existe un usuario con el nombre «" + nuevo.getUsuario() + "».");
            }
            int id = dao.insertar(cn, nuevo, BCrypt.hashpw(contrasena, BCrypt.gensalt(10)));
            bitacora.insertar(cn, actor.getId(), "usuario_administrativo", "REGISTRO", null,
                    nuevo.getUsuario() + " (" + nuevo.getRol() + ")");
            return id;
        });
    }

    public void actualizar(UsuarioAdministrativo actor, UsuarioAdministrativo datos) {
        validarDatos(datos);
        enTransaccion(cn -> {
            UsuarioAdministrativo antes = dao.buscarPorId(cn, datos.getId());
            if (antes == null) {
                throw new ReglaNegocioException("El usuario no existe.");
            }
            if (antes.getId() == actor.getId() && !antes.getRol().equals(datos.getRol())) {
                throw new ReglaNegocioException("No puede cambiar su propio rol.");
            }
            dao.actualizar(cn, datos);
            bitacora.insertar(cn, actor.getId(), "usuario_administrativo", "EDICION",
                    antes.getUsuario() + ": " + antes.getNombreCompleto() + " (" + antes.getRol() + ")",
                    antes.getUsuario() + ": " + datos.getNombreCompleto() + " (" + datos.getRol() + ")");
            return null;
        });
    }

    public void cambiarActivo(UsuarioAdministrativo actor, int id, boolean activo) {
        enTransaccion(cn -> {
            UsuarioAdministrativo u = dao.buscarPorId(cn, id);
            if (u == null) {
                throw new ReglaNegocioException("El usuario no existe.");
            }
            if (!activo && u.getId() == actor.getId()) {
                throw new ReglaNegocioException("No puede desactivar su propia cuenta.");
            }
            if (!activo && "ADMIN_CONTENIDOS".equals(u.getRol()) && u.isActivo() && dao.contarAdministradoresActivos(cn) <= 1) {
                throw new ReglaNegocioException("Debe quedar al menos un administrador de contenidos activo.");
            }
            dao.cambiarActivo(cn, id, activo);
            bitacora.insertar(cn, actor.getId(), "usuario_administrativo", activo ? "ACTIVACION" : "DESACTIVACION",
                    null, u.getUsuario());
            return null;
        });
    }

    /** El administrador asigna una contraseña temporal; el usuario la cambia luego desde su cuenta. */
    public void restablecerContrasena(UsuarioAdministrativo actor, int id, String contrasena) {
        validarContrasena(contrasena);
        enTransaccion(cn -> {
            UsuarioAdministrativo u = dao.buscarPorId(cn, id);
            if (u == null) {
                throw new ReglaNegocioException("El usuario no existe.");
            }
            dao.actualizarHash(cn, id, BCrypt.hashpw(contrasena, BCrypt.gensalt(10)));
            bitacora.insertar(cn, actor.getId(), "usuario_administrativo", "RESTABLECER_CONTRASENA", null, u.getUsuario());
            return null;
        });
    }

    /** Comprueba en cada solicitud del panel que la cuenta siga activa y con el mismo rol. */
    public UsuarioAdministrativo vigente(int id) {
        UsuarioAdministrativo u = consultar(cn -> dao.buscarPorId(cn, id));
        if (u == null || !u.isActivo()) {
            return null;
        }
        u.setHashContrasena(null);
        return u;
    }

    private static void validarDatos(UsuarioAdministrativo u) {
        if (u.getNombreCompleto() == null || u.getNombreCompleto().isBlank()) {
            throw new ReglaNegocioException("El nombre completo es obligatorio.");
        }
        if (!ROLES.contains(u.getRol())) {
            throw new ReglaNegocioException("Seleccione un rol válido: ADMIN_CONTENIDOS o PERSONAL_UTD.");
        }
    }

    static void validarContrasena(String c) {
        if (c == null || c.length() < 8 || !c.matches(".*\\d.*") || !c.matches(".*[A-Za-z].*")) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 8 caracteres, con letras y números.");
        }
    }
}
