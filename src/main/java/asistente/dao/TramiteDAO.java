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
import java.sql.Timestamp;
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
                + "AND t.publicado) AS total FROM categoria c ORDER BY c.orden, c.nombre";
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                c.setIcono(rs.getString("icono"));
                c.setTotalTramites(rs.getInt("total"));
                c.setOrden(rs.getInt("orden"));
                lista.add(c);
            }
        }
        return lista;
    }

    /** Trámites visibles al público (con una versión publicada), filtrados por texto y categoría. */
    public List<Tramite> buscarPublicados(Connection cn, String texto, Integer idCategoria) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_TRAMITE + "WHERE t.publicado ");
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
        java.util.Map<Integer, PuntoAtencion> puntos = new java.util.HashMap<>();
        for (PuntoAtencion p : listarPuntos(cn)) {
            puntos.put(p.getId(), p);
        }
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
                    int idPunto = rs.getInt("id_punto");
                    if (!rs.wasNull()) {
                        p.setIdPunto(idPunto);
                        p.setPunto(puntos.get(idPunto));
                    }
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
                p.setIdDependencia(rs.getInt("id_dependencia"));
                p.setReferencia(rs.getString("referencia"));
                p.setLatitud(rs.getBigDecimal("latitud"));
                p.setLongitud(rs.getBigDecimal("longitud"));
                Date fv = rs.getDate("fecha_ultima_verificacion");
                p.setFechaUltimaVerificacion(fv == null ? null : fv.toLocalDate());
                l.add(p);
            }
        }
        return l;
    }

    // ------------------------------------------------------------------ escritura
    public int insertar(Connection cn, Tramite t) throws SQLException {
        String sql = "INSERT INTO tramite (id_categoria, id_punto, codigo, codigo_tupa, nombre, descripcion, dirigido_a, "
                + "derecho_pago, detalle_pago, plazo_referencial, calificacion, estado_contenido, fecha_ultima_verificacion) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?, 'PENDIENTE', CURRENT_DATE)";
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

    // ------------------------------------------------------------------ ciclo de validación (RF-15)
    /** Versión publicada serializada, o null si el trámite nunca se validó con este mecanismo. */
    public byte[] leerVersionPublicada(Connection cn, int idTramite) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT version_publicada FROM tramite WHERE id_tramite = ?")) {
            ps.setInt(1, idTramite);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBytes(1) : null;
            }
        }
    }

    /** Guarda la versión que ve el público (antes de un cambio o al aprobarse). */
    public void guardarVersionPublicada(Connection cn, int idTramite, byte[] version) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE tramite SET version_publicada = ? WHERE id_tramite = ?")) {
            ps.setBytes(1, version);
            ps.setInt(2, idTramite);
            ps.executeUpdate();
        }
    }

    /** RN-11: el cambio queda pendiente de validación; la versión publicada sigue visible. */
    public void marcarPendiente(Connection cn, int idTramite) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE tramite SET estado_contenido = 'PENDIENTE' WHERE id_tramite = ?")) {
            ps.setInt(1, idTramite);
            ps.executeUpdate();
        }
    }

    public void aprobar(Connection cn, int idTramite, byte[] version) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE tramite SET estado_contenido = 'PUBLICADO', publicado = TRUE, "
                + "version_publicada = ?, fecha_publicacion = now(), observacion_validacion = NULL WHERE id_tramite = ?")) {
            ps.setBytes(1, version);
            ps.setInt(2, idTramite);
            ps.executeUpdate();
        }
    }

    public void devolver(Connection cn, int idTramite, String observacion) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE tramite SET estado_contenido = 'DEVUELTO', observacion_validacion = ? WHERE id_tramite = ?")) {
            ps.setString(1, observacion);
            ps.setInt(2, idTramite);
            ps.executeUpdate();
        }
    }

    /** Retira el trámite de la vista pública (queda pendiente de una nueva validación). */
    public void retirar(Connection cn, int idTramite) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE tramite SET publicado = FALSE, version_publicada = NULL, "
                + "estado_contenido = 'PENDIENTE' WHERE id_tramite = ?")) {
            ps.setInt(1, idTramite);
            ps.executeUpdate();
        }
    }

    public List<Tramite> listarPorEstado(Connection cn, String estado) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_TRAMITE + "WHERE t.estado_contenido = ? ORDER BY t.codigo")) {
            ps.setString(1, estado);
            return mapearLista(ps);
        }
    }

    // ------------------------------------------------------------------ categorías (RF-12)
    public int guardarCategoria(Connection cn, Categoria c) throws SQLException {
        if (c.getId() == 0) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO categoria (nombre, descripcion, icono, orden) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                datosCategoria(ps, c);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE categoria SET nombre=?, descripcion=?, icono=?, orden=? WHERE id_categoria=?")) {
            datosCategoria(ps, c);
            ps.setInt(5, c.getId());
            ps.executeUpdate();
            return c.getId();
        }
    }

    private void datosCategoria(PreparedStatement ps, Categoria c) throws SQLException {
        ps.setString(1, c.getNombre());
        ps.setString(2, c.getDescripcion());
        ps.setString(3, c.getIcono() == null ? "bi-folder" : c.getIcono());
        ps.setInt(4, c.getOrden());
    }

    public int contarTramitesDeCategoria(Connection cn, int idCategoria) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT count(*) FROM tramite WHERE id_categoria = ?")) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void eliminarCategoria(Connection cn, int idCategoria) throws SQLException {
        ejecutar(cn, "DELETE FROM categoria WHERE id_categoria = ?", idCategoria);
    }

    public void insertarPaso(Connection cn, Paso p) throws SQLException {
        String sql = "INSERT INTO paso (id_tramite, orden, descripcion, es_pago_previo, id_punto) VALUES (?, "
                + "(SELECT coalesce(max(orden), 0) + 1 FROM paso WHERE id_tramite = ?), ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, p.getIdTramite());
            ps.setInt(2, p.getIdTramite());
            ps.setString(3, p.getDescripcion());
            ps.setBoolean(4, p.isPagoPrevio());
            if (p.getIdPunto() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, p.getIdPunto());
            }
            ps.executeUpdate();
        }
    }

    public void eliminarPaso(Connection cn, int idTramite, int idPaso) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM paso WHERE id_paso = ? AND id_tramite = ?")) {
            ps.setInt(1, idPaso);
            ps.setInt(2, idTramite);
            ps.executeUpdate();
        }
    }

    /** Asigna (o quita) el punto de atención de un paso para la secuencia de visita (RF-09). */
    public void asignarPuntoPaso(Connection cn, int idTramite, int idPaso, Integer idPunto) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE paso SET id_punto = ? WHERE id_paso = ? AND id_tramite = ?")) {
            if (idPunto == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, idPunto);
            }
            ps.setInt(2, idPaso);
            ps.setInt(3, idTramite);
            ps.executeUpdate();
        }
    }

    /** Trámite al que pertenece un requisito (para marcarlo pendiente al cambiar su sustento). */
    public Integer tramiteDeRequisito(Connection cn, int idRequisito) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT id_tramite FROM requisito WHERE id_requisito = ?")) {
            ps.setInt(1, idRequisito);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
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

    public void eliminarRequisito(Connection cn, int idTramite, int idRequisito) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM requisito WHERE id_requisito = ? AND id_tramite = ?")) {
            ps.setInt(1, idRequisito);
            ps.setInt(2, idTramite);
            ps.executeUpdate();
        }
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
                t.setPublicado(rs.getBoolean("publicado"));
                Timestamp fp = rs.getTimestamp("fecha_publicacion");
                t.setFechaPublicacion(fp == null ? null : fp.toLocalDateTime());
                t.setObservacionValidacion(rs.getString("observacion_validacion"));
                Date f = rs.getDate("fecha_ultima_verificacion");
                t.setFechaUltimaVerificacion(f == null ? null : f.toLocalDate());
                l.add(t);
            }
        }
        return l;
    }
}
