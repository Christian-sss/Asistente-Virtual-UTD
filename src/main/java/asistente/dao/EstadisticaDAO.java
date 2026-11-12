package asistente.dao;

import asistente.modelo.ResultadoCriterio;
import asistente.modelo.Retroalimentacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro anónimo de consultas y observaciones, retroalimentación y reportes por periodo
 * (RF-11, RF-16, RN-14). No se guarda ningún dato personal ni el texto de los documentos.
 */
public class EstadisticaDAO {

    /** Resultado tabular de un reporte: se muestra en pantalla y se exporta a CSV. */
    public record Reporte(String titulo, List<String> columnas, List<List<String>> filas) {
    }

    // ------------------------------------------------------------------ registro
    public void registrarConsulta(Connection cn, String tipo, Integer idTramite, Integer idDependencia) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO registro_consulta (tipo, id_tramite, id_dependencia) VALUES (?,?,?)")) {
            ps.setString(1, tipo);
            entero(ps, 2, idTramite);
            entero(ps, 3, idDependencia);
            ps.executeUpdate();
        }
    }

    public void registrarObservaciones(Connection cn, int idRevision, int idTramite, List<ResultadoCriterio> resultados)
            throws SQLException {
        String sql = "INSERT INTO registro_observacion (id_revision, id_tramite, criterio, tipo, resultado) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (ResultadoCriterio r : resultados) {
                ps.setInt(1, idRevision);
                ps.setInt(2, idTramite);
                ps.setString(3, r.getCriterio());
                ps.setString(4, r.getTipo());
                ps.setString(5, r.getResultado());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Actualiza el resultado registrado de un criterio tras la confirmación asistida. */
    public void actualizarObservacion(Connection cn, int idRevision, String criterio, String resultado) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE registro_observacion SET resultado = ? WHERE id_revision = ? AND criterio = ?")) {
            ps.setString(1, resultado);
            ps.setInt(2, idRevision);
            ps.setString(3, criterio);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ retroalimentación (RF-11)
    public void registrarRetroalimentacion(Connection cn, Retroalimentacion r) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO retroalimentacion (id_tramite, tipo, comentario) VALUES (?,?,?)")) {
            entero(ps, 1, r.getIdTramite());
            ps.setString(2, r.getTipo());
            ps.setString(3, r.getComentario());
            ps.executeUpdate();
        }
    }

    public List<Retroalimentacion> listarRetroalimentacion(Connection cn, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT r.*, t.nombre AS tramite FROM retroalimentacion r LEFT JOIN tramite t USING (id_tramite) "
                + "WHERE r.fecha >= ? AND r.fecha < ? ORDER BY r.atendida, r.fecha DESC";
        List<Retroalimentacion> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            periodo(ps, 1, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Retroalimentacion r = new Retroalimentacion();
                    r.setId(rs.getInt("id_retroalimentacion"));
                    int idT = rs.getInt("id_tramite");
                    r.setIdTramite(rs.wasNull() ? null : idT);
                    r.setTramite(rs.getString("tramite"));
                    r.setTipo(rs.getString("tipo"));
                    r.setComentario(rs.getString("comentario"));
                    r.setAtendida(rs.getBoolean("atendida"));
                    r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    l.add(r);
                }
            }
        }
        return l;
    }

    public void marcarAtendida(Connection cn, int id, boolean atendida) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE retroalimentacion SET atendida = ? WHERE id_retroalimentacion = ?")) {
            ps.setBoolean(1, atendida);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public int contarRetroalimentacionPendiente(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT count(*) FROM retroalimentacion WHERE NOT atendida");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ------------------------------------------------------------------ reportes (RF-16)
    public Reporte tramitesMasConsultados(Connection cn, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT t.codigo AS \"Código\", t.nombre AS \"Trámite\", "
                + "count(*) FILTER (WHERE rc.tipo = 'HOJA_RUTA') AS \"Hojas de ruta\", "
                + "count(*) FILTER (WHERE rc.tipo = 'PDF') AS \"PDF descargados\", "
                + "count(*) FILTER (WHERE rc.tipo = 'ORIENTACION') AS \"Identificados por orientación\", "
                + "count(*) FILTER (WHERE rc.tipo = 'REVISION') AS \"Revisiones de documentos\", "
                + "count(*) AS \"Total\" "
                + "FROM registro_consulta rc JOIN tramite t USING (id_tramite) "
                + "WHERE rc.fecha >= ? AND rc.fecha < ? GROUP BY t.codigo, t.nombre ORDER BY \"Total\" DESC, t.codigo";
        return consulta(cn, "Trámites más consultados", sql, desde, hasta);
    }

    public Reporte consultasPorServicio(Connection cn, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT CASE tipo WHEN 'HOJA_RUTA' THEN 'Hoja de ruta' WHEN 'PDF' THEN 'Descarga de PDF' "
                + "WHEN 'ORIENTACION' THEN 'Orientación guiada' WHEN 'NORMATIVA' THEN 'Consulta normativa' "
                + "WHEN 'UBICACION' THEN 'Ubicación y canales' WHEN 'DERIVACION' THEN 'Cargo, expediente y derivación' "
                + "WHEN 'REVISION' THEN 'Revisión preliminar' ELSE tipo END AS \"Servicio\", count(*) AS \"Consultas\" "
                + "FROM registro_consulta WHERE fecha >= ? AND fecha < ? GROUP BY tipo ORDER BY 2 DESC";
        return consulta(cn, "Consultas por servicio", sql, desde, hasta);
    }

    public Reporte observacionesFrecuentes(Connection cn, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT ro.criterio AS \"Criterio\", coalesce(t.nombre, '(trámite eliminado)') AS \"Trámite\", "
                + "count(*) FILTER (WHERE ro.resultado = 'OBSERVADO') AS \"Observado\", "
                + "count(*) FILTER (WHERE ro.resultado = 'NO_VERIFICABLE') AS \"No verificable\", "
                + "count(*) AS \"Evaluaciones\", "
                + "round(100.0 * count(*) FILTER (WHERE ro.resultado = 'OBSERVADO') / count(*), 1) AS \"% observado\" "
                + "FROM registro_observacion ro LEFT JOIN tramite t USING (id_tramite) "
                + "WHERE ro.fecha >= ? AND ro.fecha < ? GROUP BY ro.criterio, t.nombre "
                + "HAVING count(*) FILTER (WHERE ro.resultado <> 'CUMPLE') > 0 "
                + "ORDER BY \"Observado\" DESC, \"No verificable\" DESC, ro.criterio";
        return consulta(cn, "Observaciones formales más frecuentes", sql, desde, hasta);
    }

    public Reporte retroalimentacion(Connection cn, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT to_char(r.fecha, 'DD/MM/YYYY HH24:MI') AS \"Fecha\", coalesce(t.nombre, '(General)') AS \"Trámite\", "
                + "r.tipo AS \"Tipo\", r.comentario AS \"Comentario\", CASE WHEN r.atendida THEN 'Sí' ELSE 'No' END AS \"Atendida\" "
                + "FROM retroalimentacion r LEFT JOIN tramite t USING (id_tramite) "
                + "WHERE r.fecha >= ? AND r.fecha < ? ORDER BY r.fecha DESC";
        return consulta(cn, "Retroalimentación recibida", sql, desde, hasta);
    }

    public Reporte bitacora(Connection cn, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT to_char(b.fecha, 'DD/MM/YYYY HH24:MI:SS') AS \"Fecha\", coalesce(u.usuario, 'sistema') AS \"Usuario\", "
                + "b.entidad AS \"Entidad\", b.accion AS \"Acción\", b.valor_anterior AS \"Valor anterior\", b.valor_nuevo AS \"Valor nuevo\" "
                + "FROM bitacora_cambios b LEFT JOIN usuario_administrativo u USING (id_usuario) "
                + "WHERE b.fecha >= ? AND b.fecha < ? ORDER BY b.fecha DESC, b.id_bitacora DESC";
        return consulta(cn, "Bitácora de cambios", sql, desde, hasta);
    }

    private Reporte consulta(Connection cn, String titulo, String sql, LocalDate desde, LocalDate hasta) throws SQLException {
        List<String> columnas = new ArrayList<>();
        List<List<String>> filas = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            periodo(ps, 1, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    columnas.add(md.getColumnLabel(i));
                }
                while (rs.next()) {
                    List<String> fila = new ArrayList<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        String v = rs.getString(i);
                        fila.add(v == null ? "" : v);
                    }
                    filas.add(fila);
                }
            }
        }
        return new Reporte(titulo, columnas, filas);
    }

    /** El periodo incluye ambos días completos: [desde 00:00, hasta + 1 día 00:00). */
    private static void periodo(PreparedStatement ps, int i, LocalDate desde, LocalDate hasta) throws SQLException {
        ps.setTimestamp(i, Timestamp.valueOf(desde.atStartOfDay()));
        ps.setTimestamp(i + 1, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
    }

    private static void entero(PreparedStatement ps, int i, Integer v) throws SQLException {
        if (v == null) {
            ps.setNull(i, Types.INTEGER);
        } else {
            ps.setInt(i, v);
        }
    }
}
