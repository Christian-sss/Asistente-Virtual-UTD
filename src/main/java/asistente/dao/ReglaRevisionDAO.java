package asistente.dao;

import asistente.modelo.ReglaRevision;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a las reglas de revisión formal parametrizadas por trámite.
 */
public class ReglaRevisionDAO {

    public List<ReglaRevision> listarPorTramite(Connection cn, int idTramite, boolean soloActivas) throws SQLException {
        String sql = "SELECT * FROM regla_revision WHERE id_tramite = ? " + (soloActivas ? "AND activa " : "")
                + "ORDER BY tipo, id_regla";
        List<ReglaRevision> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTramite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReglaRevision r = new ReglaRevision();
                    r.setId(rs.getInt("id_regla"));
                    r.setIdTramite(idTramite);
                    r.setCriterio(rs.getString("criterio"));
                    r.setTipo(rs.getString("tipo"));
                    r.setExpresion(rs.getString("expresion"));
                    r.setMensajeCorreccion(rs.getString("mensaje_correccion"));
                    r.setActiva(rs.getBoolean("activa"));
                    l.add(r);
                }
            }
        }
        return l;
    }

    public void insertar(Connection cn, ReglaRevision r) throws SQLException {
        String sql = "INSERT INTO regla_revision (id_tramite, criterio, tipo, expresion, mensaje_correccion) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdTramite());
            ps.setString(2, r.getCriterio());
            ps.setString(3, r.getTipo());
            ps.setString(4, r.getExpresion());
            ps.setString(5, r.getMensajeCorreccion());
            ps.executeUpdate();
        }
    }

    public void cambiarActiva(Connection cn, int idRegla, boolean activa) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE regla_revision SET activa = ? WHERE id_regla = ?")) {
            ps.setBoolean(1, activa);
            ps.setInt(2, idRegla);
            ps.executeUpdate();
        }
    }

    public String mensajeCorreccion(Connection cn, int idRegla) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT mensaje_correccion FROM regla_revision WHERE id_regla = ?")) {
            ps.setInt(1, idRegla);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public void eliminar(Connection cn, int idRegla) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM regla_revision WHERE id_regla = ?")) {
            ps.setInt(1, idRegla);
            ps.executeUpdate();
        }
    }
}
