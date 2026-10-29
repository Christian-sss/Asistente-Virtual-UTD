package asistente.dao;

import asistente.modelo.ArticuloNormativo;
import asistente.modelo.Categoria;
import asistente.modelo.Paso;
import asistente.modelo.PuntoAtencion;
import asistente.modelo.Requisito;
import asistente.modelo.Tramite;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a trámites, categorías, pasos, requisitos y puntos de atención.
 */
public class TramiteDAO {

    private static final String SELECT_TRAMITE =
            "SELECT t.*, c.nombre AS categoria FROM tramite t JOIN categoria c USING (id_categoria) ";

    public List<Categoria> listarCategorias(Connection cn) throws SQLException {
        String sql = "SELECT c.*, (SELECT count(*) FROM tramite t WHERE t.id_categoria = c.id_categoria "
                + "AND t.estado_contenido = 'PUBLICADO') AS total FROM categoria c ORDER BY c.orden";
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                c.setIcono(rs.getString("icono"));
                c.setTotalTramites(rs.getInt("total"));
                lista.add(c);
            }
        }
        return lista;
    }

    /** Trámites publicados filtrados por texto (nombre o descripción) y categoría. */
    public List<Tramite> buscarPublicados(Connection cn, String texto, Integer idCategoria) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_TRAMITE + "WHERE t.estado_contenido = 'PUBLICADO' ");
        List<Object> params = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            sql.append("AND (t.nombre ILIKE ? OR t.descripcion ILIKE ? OR t.codigo_tupa ILIKE ?) ");
            String patron = "%" + texto.trim() + "%";
            params.add(patron);
            params.add(patron);
            params.add(patron);
        }
        if (idCategoria != null) {
            sql.append("AND t.id_categoria = ? ");
            params.add(idCategoria);
        }
        sql.append("ORDER BY c.orden, t.nombre");
        try (PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return mapearLista(ps);
        }
    }

    public List<Tramite> listarTodos(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_TRAMITE + "ORDER BY t.codigo")) {
            return mapearLista(ps);
        }
    }

    /** Trámite con su punto de atención, pasos y requisitos (con sustento normativo). */
    public Tramite buscarPorId(Connection cn, int id) throws SQLException {
        Tramite t;
        try (PreparedStatement ps = cn.prepareStatement(SELECT_TRAMITE + "WHERE t.id_tramite = ?")) {
            ps.setInt(1, id);
            List<Tramite> l = mapearLista(ps);
            if (l.isEmpty()) {
                return null;
            }
            t = l.get(0);
        }
        if (t.getIdPunto() != null) {
            t.setPunto(buscarPunto(cn, t.getIdPunto()));
        }
        t.setPasos(listarPasos(cn, id));
        t.setRequisitos(listarRequisitos(cn, id));
        return t;
    }

    public List<Paso> listarPasos(Connection cn, int idTramite) throws SQLException {
        List<Paso> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM paso WHERE id_tramite = ? ORDER BY orden, id_paso")) {
            ps.setInt(1, idTramite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Paso p = new Paso();
                    p.setId(rs.getInt("id_paso"));
                    p.setIdTramite(idTramite);
                    p.setOrden(rs.getInt("orden"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setPagoPrevio(rs.getBoolean("es_pago_previo"));
                    l.add(p);
                }
            }
        }
        return l;
    }

    public List<Requisito> listarRequisitos(Connection cn, int idTramite) throws SQLException {
        List<Requisito> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM requisito WHERE id_tramite = ? ORDER BY orden, id_requisito")) {
            ps.setInt(1, idTramite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Requisito r = new Requisito();
                    r.setId(rs.getInt("id_requisito"));
                    r.setIdTramite(idTramite);
                    r.setOrden(rs.getInt("orden"));
                    r.setDescripcion(rs.getString("descripcion"));
                    r.setObligatorio(rs.getBoolean("obligatorio"));
                    l.add(r);
                }
            }
        }
        String sql = "SELECT a.*, n.tipo || ' ' || n.numero || ' - ' || n.titulo AS normativa, n.vigente, n.fuente_oficial "
                + "FROM requisito_articulo ra JOIN articulo_normativo a USING (id_articulo) "
                + "JOIN normativa n USING (id_normativa) WHERE ra.id_requisito = ? ORDER BY n.id_normativa, a.numero";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Requisito r : l) {
                ps.setInt(1, r.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ArticuloNormativo a = new ArticuloNormativo();
                        a.setId(rs.getInt("id_articulo"));
                        a.setIdNormativa(rs.getInt("id_normativa"));
                        a.setNumero(rs.getString("numero"));
                        a.setContenido(rs.getString("contenido"));
                        a.setNormativa(rs.getString("normativa"));
                        a.setVigente(rs.getBoolean("vigente"));
                        a.setFuenteOficial(rs.getString("fuente_oficial"));
                        r.getArticulos().add(a);
                    }
                }
            }
        }
        return l;
    }

    public PuntoAtencion buscarPunto(Connection cn, int idPunto) throws SQLException {
        for (PuntoAtencion p : listarPuntos(cn)) {
            if (p.getId() == idPunto) {
                return p;
            }
        }
        return null;
    }

    public List<PuntoAtencion> listarPuntos(Connection cn) throws SQLException {
        List<PuntoAtencion> l = new ArrayList<>();
        String sql = "SELECT p.*, d.nombre AS dependencia FROM punto_atencion p JOIN dependencia d USING (id_dependencia) ORDER BY p.id_punto";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PuntoAtencion p = new PuntoAtencion();
                p.setId(rs.getInt("id_punto"));
                p.setDependencia(rs.getString("dependencia"));
                p.setNombre(rs.getString("nombre"));
                p.setSede(rs.getString("sede"));
                p.setDireccion(rs.getString("direccion"));
                p.setHorario(rs.getString("horario"));
                p.setTelefono(rs.getString("telefono"));
                p.setCorreo(rs.getString("correo"));
                l.add(p);
            }
        }
        return l;
    }

    // ------------------------------------------------------------------ escritura
    public int insertar(Connection cn, Tramite t) throws SQLException {
        String sql = "INSERT INTO tramite (id_categoria, id_punto, codigo, codigo_tupa, nombre, descripcion, dirigido_a, "
                + "derecho_pago, detalle_pago, plazo_referencial, calificacion, estado_contenido, fecha_ultima_verificacion) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?, 'BORRADOR', CURRENT_DATE)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, t);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Connection cn, Tramite t) throws SQLException {
        String sql = "UPDATE tramite SET id_categoria=?, id_punto=?, codigo=?, codigo_tupa=?, nombre=?, descripcion=?, dirigido_a=?, "
                + "derecho_pago=?, detalle_pago=?, plazo_referencial=?, calificacion=?, fecha_ultima_verificacion=CURRENT_DATE "
                + "WHERE id_tramite=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            asignarDatos(ps, t);
            ps.setInt(12, t.getId());
            ps.executeUpdate();
        }
    }

    private void asignarDatos(PreparedStatement ps, Tramite t) throws SQLException {
        ps.setInt(1, t.getIdCategoria());
        if (t.getIdPunto() == null) {
            ps.setNull(2, Types.INTEGER);
        } else {
            ps.setInt(2, t.getIdPunto());
        }
        ps.setString(3, t.getCodigo());
        ps.setString(4, t.getCodigoTupa());
        ps.setString(5, t.getNombre());
        ps.setString(6, t.getDescripcion());
        ps.setString(7, t.getDirigidoA());
        ps.setBigDecimal(8, t.getDerechoPago());
        ps.setString(9, t.getDetallePago());
        ps.setString(10, t.getPlazoReferencial());
        ps.setString(11, t.getCalificacion());
    }

    public void cambiarEstado(Connection cn, int idTramite, String estado) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE tramite SET estado_contenido=? WHERE id_tramite=?")) {
            ps.setString(1, estado);
            ps.setInt(2, idTramite);
            ps.executeUpdate();
        }
    }

    public void insertarPaso(Connection cn, Paso p) throws SQLException {
        String sql = "INSERT INTO paso (id_tramite, orden, descripcion, es_pago_previo) VALUES (?, "
                + "(SELECT coalesce(max(orden), 0) + 1 FROM paso WHERE id_tramite = ?), ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, p.getIdTramite());
            ps.setInt(2, p.getIdTramite());
            ps.setString(3, p.getDescripcion());
            ps.setBoolean(4, p.isPagoPrevio());
            ps.executeUpdate();
        }
    }

    public void eliminarPaso(Connection cn, int idPaso) throws SQLException {
        ejecutar(cn, "DELETE FROM paso WHERE id_paso = ?", idPaso);
    }

    public int insertarRequisito(Connection cn, Requisito r) throws SQLException {
        String sql = "INSERT INTO requisito (id_tramite, orden, descripcion, obligatorio) VALUES (?, "
                + "(SELECT coalesce(max(orden), 0) + 1 FROM requisito WHERE id_tramite = ?), ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getIdTramite());
            ps.setInt(2, r.getIdTramite());
            ps.setString(3, r.getDescripcion());
            ps.setBoolean(4, r.isObligatorio());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void eliminarRequisito(Connection cn, int idRequisito) throws SQLException {
        ejecutar(cn, "DELETE FROM requisito WHERE id_requisito = ?", idRequisito);
    }

    public void vincularArticulo(Connection cn, int idRequisito, int idArticulo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO requisito_articulo (id_requisito, id_articulo) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
            ps.setInt(1, idRequisito);
            ps.setInt(2, idArticulo);
            ps.executeUpdate();
        }
    }

    public void desvincularArticulo(Connection cn, int idRequisito, int idArticulo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM requisito_articulo WHERE id_requisito = ? AND id_articulo = ?")) {
            ps.setInt(1, idRequisito);
            ps.setInt(2, idArticulo);
            ps.executeUpdate();
        }
    }

    /** Requisitos obligatorios sin artículo de una norma vigente (impiden publicar). */
    public int contarRequisitosSinSustento(Connection cn, int idTramite) throws SQLException {
        String sql = "SELECT count(*) FROM requisito r WHERE r.id_tramite = ? AND r.obligatorio AND NOT EXISTS ("
                + "SELECT 1 FROM requisito_articulo ra JOIN articulo_normativo a USING (id_articulo) "
                + "JOIN normativa n USING (id_normativa) WHERE ra.id_requisito = r.id_requisito AND n.vigente)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTramite);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int contar(Connection cn, String condicion) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT count(*) FROM tramite " + condicion);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void ejecutar(Connection cn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private List<Tramite> mapearLista(PreparedStatement ps) throws SQLException {
        List<Tramite> l = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tramite t = new Tramite();
                t.setId(rs.getInt("id_tramite"));
                t.setIdCategoria(rs.getInt("id_categoria"));
                t.setCategoria(rs.getString("categoria"));
                int idPunto = rs.getInt("id_punto");
                t.setIdPunto(rs.wasNull() ? null : idPunto);
                t.setCodigo(rs.getString("codigo"));
                t.setCodigoTupa(rs.getString("codigo_tupa"));
                t.setNombre(rs.getString("nombre"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setDirigidoA(rs.getString("dirigido_a"));
                t.setDerechoPago(rs.getBigDecimal("derecho_pago"));
                t.setDetallePago(rs.getString("detalle_pago"));
                t.setPlazoReferencial(rs.getString("plazo_referencial"));
                t.setCalificacion(rs.getString("calificacion"));
                t.setEstadoContenido(rs.getString("estado_contenido"));
                Date f = rs.getDate("fecha_ultima_verificacion");
                t.setFechaUltimaVerificacion(f == null ? null : f.toLocalDate());
                l.add(t);
            }
        }
        return l;
    }
}
