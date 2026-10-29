package asistente.dao;

import asistente.modelo.UsuarioAdministrativo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a los usuarios del panel de administración.
 */
public class UsuarioAdministrativoDAO {

    public UsuarioAdministrativo buscarPorUsuario(Connection cn, String usuario) throws SQLException {
        String sql = "SELECT u.*, r.nombre AS rol FROM usuario_administrativo u JOIN rol r USING (id_rol) WHERE u.usuario = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                UsuarioAdministrativo u = new UsuarioAdministrativo();
                u.setId(rs.getInt("id_usuario"));
                u.setUsuario(rs.getString("usuario"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setHashContrasena(rs.getString("hash_contrasena"));
                u.setActivo(rs.getBoolean("activo"));
                u.setRol(rs.getString("rol"));
                return u;
            }
        }
    }

    public void actualizarHash(Connection cn, int idUsuario, String hash) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE usuario_administrativo SET hash_contrasena = ? WHERE id_usuario = ?")) {
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }
}
