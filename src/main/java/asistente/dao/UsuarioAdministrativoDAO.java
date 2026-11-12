package asistente.dao;

import asistente.modelo.UsuarioAdministrativo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
                return mapear(rs);
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

    // ------------------------------------------------------------------ gestión de usuarios (RF-02)
    public List<UsuarioAdministrativo> listar(Connection cn) throws SQLException {
        String sql = "SELECT u.*, r.nombre AS rol FROM usuario_administrativo u JOIN rol r USING (id_rol) ORDER BY u.activo DESC, u.usuario";
        List<UsuarioAdministrativo> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UsuarioAdministrativo u = mapear(rs);
                u.setHashContrasena(null);
                l.add(u);
            }
        }
        return l;
    }

    public UsuarioAdministrativo buscarPorId(Connection cn, int id) throws SQLException {
        String sql = "SELECT u.*, r.nombre AS rol FROM usuario_administrativo u JOIN rol r USING (id_rol) WHERE u.id_usuario = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public int insertar(Connection cn, UsuarioAdministrativo u, String hash) throws SQLException {
        String sql = "INSERT INTO usuario_administrativo (id_rol, usuario, nombre_completo, hash_contrasena, activo) "
                + "VALUES ((SELECT id_rol FROM rol WHERE nombre = ?), ?, ?, ?, TRUE)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getRol());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getNombreCompleto());
            ps.setString(4, hash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Connection cn, UsuarioAdministrativo u) throws SQLException {
        String sql = "UPDATE usuario_administrativo SET nombre_completo = ?, id_rol = (SELECT id_rol FROM rol WHERE nombre = ?) "
                + "WHERE id_usuario = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombreCompleto());
            ps.setString(2, u.getRol());
            ps.setInt(3, u.getId());
            ps.executeUpdate();
        }
    }

    /** RN-15: los usuarios no se eliminan; se desactivan para conservar la trazabilidad. */
    public void cambiarActivo(Connection cn, int id, boolean activo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE usuario_administrativo SET activo = ? WHERE id_usuario = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public int contarAdministradoresActivos(Connection cn) throws SQLException {
        String sql = "SELECT count(*) FROM usuario_administrativo u JOIN rol r USING (id_rol) "
                + "WHERE u.activo AND r.nombre = 'ADMIN_CONTENIDOS'";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private UsuarioAdministrativo mapear(ResultSet rs) throws SQLException {
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
