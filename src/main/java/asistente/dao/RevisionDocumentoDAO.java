package asistente.dao;

import asistente.modelo.ResultadoCriterio;
import asistente.modelo.RevisionDocumento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a las revisiones preliminares temporales y sus resultados por criterio.
 */
public class RevisionDocumentoDAO {

    public int registrar(Connection cn, RevisionDocumento r) throws SQLException {
        String sql = "INSERT INTO revision_documento (id_tramite, token_sesion, nombre_original, nombre_archivo_uuid, tipo_mime, "
                + "tamano_bytes, numero_paginas, fecha_carga, fecha_expiracion, estado) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getIdTramite());
            ps.setString(2, r.getTokenSesion());
            ps.setString(3, r.getNombreOriginal());
            ps.setString(4, r.getNombreArchivoUuid());
            ps.setString(5, r.getTipoMime());
            ps.setLong(6, r.getTamanoBytes());
            if (r.getNumeroPaginas() == null) {
                ps.setNull(7, Types.SMALLINT);
            } else {
                ps.setInt(7, r.getNumeroPaginas());
            }
            ps.setTimestamp(8, Timestamp.valueOf(r.getFechaCarga()));
            ps.setTimestamp(9, Timestamp.valueOf(r.getFechaExpiracion()));
            ps.setString(10, r.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void guardarResultados(Connection cn, int idRevision, List<ResultadoCriterio> resultados) throws SQLException {
        String sql = "INSERT INTO resultado_criterio (id_revision, id_regla, criterio, tipo, resultado, detalle) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (ResultadoCriterio r : resultados) {
                ps.setInt(1, idRevision);
                if (r.getIdRegla() == null) {
                    ps.setNull(2, Types.INTEGER);
                } else {
                    ps.setInt(2, r.getIdRegla());
                }
                ps.setString(3, r.getCriterio());
                ps.setString(4, r.getTipo());
                ps.setString(5, r.getResultado());
                ps.setString(6, r.getDetalle());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** La revisión solo se entrega a la sesión que cargó el documento. */
    public RevisionDocumento buscar(Connection cn, int idRevision, String token) throws SQLException {
        String sql = "SELECT r.*, t.nombre AS tramite FROM revision_documento r JOIN tramite t USING (id_tramite) "
                + "WHERE r.id_revision = ? AND r.token_sesion = ? AND r.fecha_expiracion > now()";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRevision);
            ps.setString(2, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                RevisionDocumento r = mapear(rs);
                r.setResultados(listarResultados(cn, idRevision));
                return r;
            }
        }
    }

    public List<ResultadoCriterio> listarResultados(Connection cn, int idRevision) throws SQLException {
        List<ResultadoCriterio> l = new ArrayList<>();
        String sql = "SELECT * FROM resultado_criterio WHERE id_revision = ? ORDER BY "
                + "CASE tipo WHEN 'BASICO' THEN 0 WHEN 'DATO_OBLIGATORIO' THEN 1 ELSE 2 END, id_resultado";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRevision);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ResultadoCriterio c = new ResultadoCriterio();
                    c.setId(rs.getInt("id_resultado"));
                    c.setIdRevision(idRevision);
                    int idRegla = rs.getInt("id_regla");
                    c.setIdRegla(rs.wasNull() ? null : idRegla);
                    c.setCriterio(rs.getString("criterio"));
                    c.setTipo(rs.getString("tipo"));
                    c.setResultado(rs.getString("resultado"));
                    c.setDetalle(rs.getString("detalle"));
                    l.add(c);
                }
            }
        }
        return l;
    }

    public void actualizarResultado(Connection cn, int idResultado, int idRevision, String resultado, String detalle) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE resultado_criterio SET resultado = ?, detalle = ? WHERE id_resultado = ? AND id_revision = ?")) {
            ps.setString(1, resultado);
            ps.setString(2, detalle);
            ps.setInt(3, idResultado);
            ps.setInt(4, idRevision);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(Connection cn, int idRevision, String estado) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE revision_documento SET estado = ? WHERE id_revision = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idRevision);
            ps.executeUpdate();
        }
    }

    public List<RevisionDocumento> buscarVencidas(Connection cn, LocalDateTime ahora) throws SQLException {
        List<RevisionDocumento> l = new ArrayList<>();
        String sql = "SELECT r.*, '' AS tramite FROM revision_documento r WHERE r.fecha_expiracion <= ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(ahora));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    l.add(mapear(rs));
                }
            }
        }
        return l;
    }

    public void eliminar(Connection cn, int idRevision) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM revision_documento WHERE id_revision = ?")) {
            ps.setInt(1, idRevision);
            ps.executeUpdate();
        }
    }

    public int contarActivas(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT count(*) FROM revision_documento");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public void registrarLimpieza(Connection cn, int eliminados, String origen) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO ejecucion_limpieza (archivos_eliminados, origen) VALUES (?, ?)")) {
            ps.setInt(1, eliminados);
            ps.setString(2, origen);
            ps.executeUpdate();
        }
    }

    /** Fecha y cantidad de la última ejecución de la limpieza, o null. */
    public String ultimaLimpieza(Connection cn) throws SQLException {
        String sql = "SELECT to_char(fecha, 'DD/MM/YYYY HH24:MI') || ' (' || archivos_eliminados || ' archivo(s), ' || lower(origen) || ')' "
                + "FROM ejecucion_limpieza ORDER BY fecha DESC LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private RevisionDocumento mapear(ResultSet rs) throws SQLException {
        RevisionDocumento r = new RevisionDocumento();
        r.setId(rs.getInt("id_revision"));
        r.setIdTramite(rs.getInt("id_tramite"));
        r.setTramite(rs.getString("tramite"));
        r.setTokenSesion(rs.getString("token_sesion"));
        r.setNombreOriginal(rs.getString("nombre_original"));
        r.setNombreArchivoUuid(rs.getString("nombre_archivo_uuid"));
        r.setTipoMime(rs.getString("tipo_mime"));
        r.setTamanoBytes(rs.getLong("tamano_bytes"));
        int pag = rs.getInt("numero_paginas");
        r.setNumeroPaginas(rs.wasNull() ? null : pag);
        r.setFechaCarga(rs.getTimestamp("fecha_carga").toLocalDateTime());
        r.setFechaExpiracion(rs.getTimestamp("fecha_expiracion").toLocalDateTime());
        r.setEstado(rs.getString("estado"));
        return r;
    }
}
