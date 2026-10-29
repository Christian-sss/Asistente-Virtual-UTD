package asistente.dao;

import asistente.modelo.OpcionRespuesta;
import asistente.modelo.PreguntaGuia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso al árbol de decisión de preguntas guiadas.
 */
public class PreguntaGuiaDAO {

    public PreguntaGuia buscarInicial(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM pregunta_guia WHERE es_inicial ORDER BY id_pregunta LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? completar(cn, mapear(rs)) : null;
        }
    }

    public PreguntaGuia buscarPorId(Connection cn, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM pregunta_guia WHERE id_pregunta = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? completar(cn, mapear(rs)) : null;
            }
        }
    }

    public List<PreguntaGuia> listarTodas(Connection cn) throws SQLException {
        List<PreguntaGuia> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM pregunta_guia ORDER BY es_inicial DESC, id_pregunta");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                l.add(mapear(rs));
            }
        }
        for (PreguntaGuia p : l) {
            completar(cn, p);
        }
        return l;
    }

    public OpcionRespuesta buscarOpcion(Connection cn, int idOpcion) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_OPCION + "WHERE o.id_opcion = ?")) {
            ps.setInt(1, idOpcion);
            List<OpcionRespuesta> l = mapearOpciones(ps);
            return l.isEmpty() ? null : l.get(0);
        }
    }

    public int insertarPregunta(Connection cn, String texto, boolean inicial) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO pregunta_guia (texto, es_inicial) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, texto);
            ps.setBoolean(2, inicial);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizarPregunta(Connection cn, int id, String texto) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE pregunta_guia SET texto = ? WHERE id_pregunta = ?")) {
            ps.setString(1, texto);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void eliminarPregunta(Connection cn, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM pregunta_guia WHERE id_pregunta = ? AND NOT es_inicial")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void insertarOpcion(Connection cn, OpcionRespuesta o) throws SQLException {
        String sql = "INSERT INTO opcion_respuesta (id_pregunta, texto, id_pregunta_siguiente, id_tramite, orden) "
                + "VALUES (?, ?, ?, ?, (SELECT coalesce(max(orden), 0) + 1 FROM opcion_respuesta WHERE id_pregunta = ?))";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, o.getIdPregunta());
            ps.setString(2, o.getTexto());
            if (o.getIdPreguntaSiguiente() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, o.getIdPreguntaSiguiente());
            }
            if (o.getIdTramite() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, o.getIdTramite());
            }
            ps.setInt(5, o.getIdPregunta());
            ps.executeUpdate();
        }
    }

    public void eliminarOpcion(Connection cn, int idOpcion) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM opcion_respuesta WHERE id_opcion = ?")) {
            ps.setInt(1, idOpcion);
            ps.executeUpdate();
        }
    }

    private static final String SELECT_OPCION = "SELECT o.*, coalesce('Trámite: ' || t.nombre, 'Pregunta: ' || p.texto, '(sin destino)') AS destino "
            + "FROM opcion_respuesta o LEFT JOIN tramite t ON t.id_tramite = o.id_tramite "
            + "LEFT JOIN pregunta_guia p ON p.id_pregunta = o.id_pregunta_siguiente ";

    private PreguntaGuia completar(Connection cn, PreguntaGuia p) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_OPCION + "WHERE o.id_pregunta = ? ORDER BY o.orden, o.id_opcion")) {
            ps.setInt(1, p.getId());
            p.setOpciones(mapearOpciones(ps));
        }
        return p;
    }

    private List<OpcionRespuesta> mapearOpciones(PreparedStatement ps) throws SQLException {
        List<OpcionRespuesta> l = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OpcionRespuesta o = new OpcionRespuesta();
                o.setId(rs.getInt("id_opcion"));
                o.setIdPregunta(rs.getInt("id_pregunta"));
                o.setTexto(rs.getString("texto"));
                int sig = rs.getInt("id_pregunta_siguiente");
                o.setIdPreguntaSiguiente(rs.wasNull() ? null : sig);
                int tr = rs.getInt("id_tramite");
                o.setIdTramite(rs.wasNull() ? null : tr);
                o.setOrden(rs.getInt("orden"));
                o.setDestino(rs.getString("destino"));
                l.add(o);
            }
        }
        return l;
    }

    private PreguntaGuia mapear(ResultSet rs) throws SQLException {
        PreguntaGuia p = new PreguntaGuia();
        p.setId(rs.getInt("id_pregunta"));
        p.setTexto(rs.getString("texto"));
        p.setInicial(rs.getBoolean("es_inicial"));
        return p;
    }
}
