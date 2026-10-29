package asistente.dao;

import asistente.modelo.BitacoraCambio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la bitácora de cambios administrativos.
 */
public class BitacoraDAO {

    /** Se invoca con la misma conexión (transacción) que realiza el cambio. */
    public void insertar(Connection cn, Integer idUsuario, String entidad, String accion,
                         String valorAnterior, String valorNuevo) throws SQLException {
        String sql = "INSERT INTO bitacora_cambios (id_usuario, entidad, accion, valor_anterior, valor_nuevo) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idUsuario == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, idUsuario);
            }
            ps.setString(2, entidad);
            ps.setString(3, accion);
            ps.setString(4, valorAnterior);
            ps.setString(5, valorNuevo);
            ps.executeUpdate();
        }
    }

    public List<BitacoraCambio> listarRecientes(Connection cn, int limite) throws SQLException {
        String sql = "SELECT b.*, coalesce(u.usuario, 'sistema') AS usuario FROM bitacora_cambios b "
                + "LEFT JOIN usuario_administrativo u USING (id_usuario) ORDER BY b.fecha DESC, b.id_bitacora DESC LIMIT ?";
        List<BitacoraCambio> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BitacoraCambio b = new BitacoraCambio();
                    b.setId(rs.getLong("id_bitacora"));
                    b.setUsuario(rs.getString("usuario"));
                    b.setEntidad(rs.getString("entidad"));
                    b.setAccion(rs.getString("accion"));
                    b.setValorAnterior(rs.getString("valor_anterior"));
                    b.setValorNuevo(rs.getString("valor_nuevo"));
                    b.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    l.add(b);
                }
            }
        }
        return l;
    }
}
