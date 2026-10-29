package asistente.servicio;

import asistente.dao.BitacoraDAO;
import asistente.dao.UsuarioAdministrativoDAO;
import asistente.modelo.UsuarioAdministrativo;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Autenticación del usuario administrativo con BCrypt y registro del acceso (RF-01).
 */
public class ServicioAutenticacion extends ServicioBase {

    private final UsuarioAdministrativoDAO usuarioDAO = new UsuarioAdministrativoDAO();
    private final BitacoraDAO bitacoraDAO = new BitacoraDAO();

    /** Devuelve el usuario si las credenciales son válidas y está activo; si no, null. */
    public UsuarioAdministrativo autenticar(String usuario, String contrasena) {
        if (usuario == null || contrasena == null || usuario.isBlank()) {
            return null;
        }
        return enTransaccion(cn -> {
            UsuarioAdministrativo u = usuarioDAO.buscarPorUsuario(cn, usuario.trim());
            if (u == null || !u.isActivo() || !BCrypt.checkpw(contrasena, u.getHashContrasena())) {
                bitacoraDAO.insertar(cn, u == null ? null : u.getId(), "sesion", "ACCESO_FALLIDO", null, usuario.trim());
                return null;
            }
            bitacoraDAO.insertar(cn, u.getId(), "sesion", "ACCESO", null, u.getUsuario() + " (" + u.getRol() + ")");
            u.setHashContrasena(null);
            return u;
        });
    }

    public void cerrarSesion(UsuarioAdministrativo u) {
        enTransaccion(cn -> {
            bitacoraDAO.insertar(cn, u.getId(), "sesion", "CIERRE", null, u.getUsuario());
            return null;
        });
    }

    public void cambiarContrasena(UsuarioAdministrativo u, String actual, String nueva, String confirmacion) {
        if (nueva == null || nueva.length() < 8 || !nueva.matches(".*\\d.*") || !nueva.matches(".*[A-Za-z].*")) {
            throw new ReglaNegocioException("La nueva contraseña debe tener al menos 8 caracteres, con letras y números.");
        }
        if (!nueva.equals(confirmacion)) {
            throw new ReglaNegocioException("La confirmación no coincide con la nueva contraseña.");
        }
        enTransaccion(cn -> {
            UsuarioAdministrativo actualBD = usuarioDAO.buscarPorUsuario(cn, u.getUsuario());
            if (actualBD == null || !BCrypt.checkpw(actual == null ? "" : actual, actualBD.getHashContrasena())) {
                throw new ReglaNegocioException("La contraseña actual no es correcta.");
            }
            usuarioDAO.actualizarHash(cn, u.getId(), BCrypt.hashpw(nueva, BCrypt.gensalt(10)));
            bitacoraDAO.insertar(cn, u.getId(), "usuario_administrativo", "CAMBIO_CONTRASENA", null, u.getUsuario());
            return null;
        });
    }
}
