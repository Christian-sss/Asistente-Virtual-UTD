package asistente.dao;

import asistente.modelo.ArticuloNormativo;
import asistente.modelo.Normativa;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a normativas, artículos y requisitos que sustentan.
 */
public class NormativaDAO {

    /** Normativas que coinciden con el texto (en la norma, sus artículos o los trámites que sustenta). */
    public List<Normativa> buscar(Connection cn, String texto, Integer idTramite) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT n.* FROM normativa n "
                + "LEFT JOIN articulo_normativo a USING (id_normativa) "
                + "LEFT JOIN requisito_articulo ra USING (id_articulo) "
                + "LEFT JOIN requisito r USING (id_requisito) "
                + "LEFT JOIN tramite t ON t.id_tramite = r.id_tramite WHERE TRUE ");
        List<Object> params = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            sql.append("AND (n.titulo ILIKE ? OR n.numero ILIKE ? OR a.contenido ILIKE ? OR t.nombre ILIKE ?) ");
            String p = "%" + texto.trim() + "%";
            for (int i = 0; i < 4; i++) {
                params.add(p);
            }
        }
        if (idTramite != null) {
            sql.append("AND t.id_tramite = ? ");
            params.add(idTramite);
        }
        sql.append("ORDER BY n.id_normativa");
        List<Normativa> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    l.add(mapear(rs));
                }
            }
        }
        for (Normativa n : l) {
            n.setArticulos(listarArticulos(cn, n.getId(), idTramite));
        }
        return l;
    }

    public Normativa buscarPorId(Connection cn, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM normativa WHERE id_normativa = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Normativa n = mapear(rs);
                n.setArticulos(listarArticulos(cn, id, null));
                return n;
            }
        }
    }

    /** Artículos de una norma con los requisitos que sustentan (opcionalmente de un trámite). */
    public List<ArticuloNormativo> listarArticulos(Connection cn, int idNormativa, Integer idTramite) throws SQLException {
        List<ArticuloNormativo> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM articulo_normativo WHERE id_normativa = ? ORDER BY id_articulo")) {
            ps.setInt(1, idNormativa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ArticuloNormativo a = new ArticuloNormativo();
                    a.setId(rs.getInt("id_articulo"));
                    a.setIdNormativa(idNormativa);
                    a.setNumero(rs.getString("numero"));
                    a.setContenido(rs.getString("contenido"));
                    l.add(a);
                }
            }
        }
        String sql = "SELECT t.nombre || ': ' || r.descripcion AS req FROM requisito_articulo ra "
                + "JOIN requisito r USING (id_requisito) JOIN tramite t ON t.id_tramite = r.id_tramite "
                + "WHERE ra.id_articulo = ? AND t.estado_contenido = 'PUBLICADO' "
                + (idTramite != null ? "AND t.id_tramite = " + idTramite.intValue() + " " : "")
                + "ORDER BY t.codigo, r.orden";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (ArticuloNormativo a : l) {
                ps.setInt(1, a.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        a.getRequisitosSustentados().add(rs.getString("req"));
                    }
                }
            }
        }
        if (idTramite != null) {
            l.removeIf(a -> a.getRequisitosSustentados().isEmpty());
        }
        return l;
    }

    /** Todos los artículos, para vincularlos a requisitos desde el panel. */
    public List<ArticuloNormativo> listarTodosArticulos(Connection cn) throws SQLException {
        List<ArticuloNormativo> l = new ArrayList<>();
        String sql = "SELECT a.*, n.tipo || ' ' || n.numero AS normativa, n.vigente FROM articulo_normativo a "
                + "JOIN normativa n USING (id_normativa) ORDER BY n.id_normativa, a.id_articulo";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ArticuloNormativo a = new ArticuloNormativo();
                a.setId(rs.getInt("id_articulo"));
                a.setIdNormativa(rs.getInt("id_normativa"));
                a.setNumero(rs.getString("numero"));
                a.setContenido(rs.getString("contenido"));
                a.setNormativa(rs.getString("normativa"));
                a.setVigente(rs.getBoolean("vigente"));
                l.add(a);
            }
        }
        return l;
    }

    public int insertar(Connection cn, Normativa n) throws SQLException {
        String sql = "INSERT INTO normativa (tipo, numero, titulo, organo_emisor, fecha_aprobacion, vigente, fuente_oficial, "
                + "fecha_ultima_verificacion) VALUES (?,?,?,?,?,?,?, CURRENT_DATE)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, n);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Connection cn, Normativa n) throws SQLException {
        String sql = "UPDATE normativa SET tipo=?, numero=?, titulo=?, organo_emisor=?, fecha_aprobacion=?, vigente=?, "
                + "fuente_oficial=?, fecha_ultima_verificacion=CURRENT_DATE WHERE id_normativa=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, n);
            ps.setInt(8, n.getId());
            ps.executeUpdate();
        }
    }

    private void asignar(PreparedStatement ps, Normativa n) throws SQLException {
        ps.setString(1, n.getTipo());
        ps.setString(2, n.getNumero());
        ps.setString(3, n.getTitulo());
        ps.setString(4, n.getOrganoEmisor());
        ps.setDate(5, n.getFechaAprobacion() == null ? null : Date.valueOf(n.getFechaAprobacion()));
        ps.setBoolean(6, n.isVigente());
        ps.setString(7, n.getFuenteOficial());
    }

    public void insertarArticulo(Connection cn, int idNormativa, String numero, String contenido) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO articulo_normativo (id_normativa, numero, contenido) VALUES (?, ?, ?)")) {
            ps.setInt(1, idNormativa);
            ps.setString(2, numero);
            ps.setString(3, contenido);
            ps.executeUpdate();
        }
    }

    public void eliminarArticulo(Connection cn, int idArticulo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM articulo_normativo WHERE id_articulo = ?")) {
            ps.setInt(1, idArticulo);
            ps.executeUpdate();
        }
    }

    private Normativa mapear(ResultSet rs) throws SQLException {
        Normativa n = new Normativa();
        n.setId(rs.getInt("id_normativa"));
        n.setTipo(rs.getString("tipo"));
        n.setNumero(rs.getString("numero"));
        n.setTitulo(rs.getString("titulo"));
        n.setOrganoEmisor(rs.getString("organo_emisor"));
        Date f = rs.getDate("fecha_aprobacion");
        n.setFechaAprobacion(f == null ? null : f.toLocalDate());
        n.setVigente(rs.getBoolean("vigente"));
        n.setFuenteOficial(rs.getString("fuente_oficial"));
        Date v = rs.getDate("fecha_ultima_verificacion");
        n.setFechaUltimaVerificacion(v == null ? null : v.toLocalDate());
        return n;
    }
}
