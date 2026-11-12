package asistente.dao;

import asistente.modelo.CanalDigital;
import asistente.modelo.Dependencia;
import asistente.modelo.PuntoAtencion;
import asistente.modelo.ReglaDerivacion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a dependencias, puntos de atención, canales digitales y reglas de derivación (RF-09, RF-10, RF-14).
 */
public class UbicacionDAO {

    private final TramiteDAO tramiteDAO = new TramiteDAO();

    // ------------------------------------------------------------------ dependencias
    /** Dependencias con sus puntos de atención y canales digitales. */
    public List<Dependencia> listarDependencias(Connection cn) throws SQLException {
        Map<Integer, Dependencia> mapa = new LinkedHashMap<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM dependencia ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Dependencia d = new Dependencia();
                d.setId(rs.getInt("id_dependencia"));
                d.setNombre(rs.getString("nombre"));
                d.setSigla(rs.getString("sigla"));
                d.setFunciones(rs.getString("funciones"));
                mapa.put(d.getId(), d);
            }
        }
        for (PuntoAtencion p : tramiteDAO.listarPuntos(cn)) {
            Dependencia d = mapa.get(p.getIdDependencia());
            if (d != null) {
                d.getPuntos().add(p);
            }
        }
        for (CanalDigital c : listarCanales(cn, null)) {
            Dependencia d = mapa.get(c.getIdDependencia());
            if (d != null) {
                d.getCanales().add(c);
            }
        }
        return new ArrayList<>(mapa.values());
    }

    public Dependencia buscarDependencia(Connection cn, int id) throws SQLException {
        for (Dependencia d : listarDependencias(cn)) {
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }

    public int guardarDependencia(Connection cn, Dependencia d) throws SQLException {
        if (d.getId() == 0) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO dependencia (nombre, sigla, funciones) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, d.getNombre());
                ps.setString(2, d.getSigla());
                ps.setString(3, d.getFunciones());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE dependencia SET nombre=?, sigla=?, funciones=? WHERE id_dependencia=?")) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getSigla());
            ps.setString(3, d.getFunciones());
            ps.setInt(4, d.getId());
            ps.executeUpdate();
            return d.getId();
        }
    }

    public int contarPuntosDeDependencia(Connection cn, int idDependencia) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT count(*) FROM punto_atencion WHERE id_dependencia = ?")) {
            ps.setInt(1, idDependencia);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void eliminarDependencia(Connection cn, int id) throws SQLException {
        ejecutar(cn, "DELETE FROM dependencia WHERE id_dependencia = ?", id);
    }

    // ------------------------------------------------------------------ puntos de atención
    public int guardarPunto(Connection cn, PuntoAtencion p) throws SQLException {
        String sql = p.getId() == 0
                ? "INSERT INTO punto_atencion (id_dependencia, nombre, sede, direccion, horario, telefono, correo, referencia, "
                  + "coord_x, coord_y, fecha_ultima_verificacion) VALUES (?,?,?,?,?,?,?,?,?,?, CURRENT_DATE)"
                : "UPDATE punto_atencion SET id_dependencia=?, nombre=?, sede=?, direccion=?, horario=?, telefono=?, correo=?, "
                  + "referencia=?, coord_x=?, coord_y=?, fecha_ultima_verificacion=CURRENT_DATE WHERE id_punto=?";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdDependencia());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getSede());
            ps.setString(4, p.getDireccion());
            ps.setString(5, p.getHorario());
            ps.setString(6, p.getTelefono());
            ps.setString(7, p.getCorreo());
            ps.setString(8, p.getReferencia());
            ps.setBigDecimal(9, p.getCoordX());
            ps.setBigDecimal(10, p.getCoordY());
            if (p.getId() != 0) {
                ps.setInt(11, p.getId());
            }
            ps.executeUpdate();
            if (p.getId() != 0) {
                return p.getId();
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void eliminarPunto(Connection cn, int id) throws SQLException {
        ejecutar(cn, "DELETE FROM punto_atencion WHERE id_punto = ?", id);
    }

    // ------------------------------------------------------------------ canales digitales
    public List<CanalDigital> listarCanales(Connection cn, Integer idDependencia) throws SQLException {
        String sql = "SELECT c.*, d.nombre AS dependencia FROM canal_digital c JOIN dependencia d USING (id_dependencia) "
                + (idDependencia == null ? "" : "WHERE c.id_dependencia = ? ") + "ORDER BY d.nombre, c.tipo, c.nombre";
        List<CanalDigital> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idDependencia != null) {
                ps.setInt(1, idDependencia);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CanalDigital c = new CanalDigital();
                    c.setId(rs.getInt("id_canal"));
                    c.setIdDependencia(rs.getInt("id_dependencia"));
                    c.setDependencia(rs.getString("dependencia"));
                    c.setTipo(rs.getString("tipo"));
                    c.setNombre(rs.getString("nombre"));
                    c.setValor(rs.getString("valor"));
                    c.setDescripcion(rs.getString("descripcion"));
                    c.setFechaUltimaVerificacion(fecha(rs, "fecha_ultima_verificacion"));
                    l.add(c);
                }
            }
        }
        return l;
    }

    public void guardarCanal(Connection cn, CanalDigital c) throws SQLException {
        String sql = c.getId() == 0
                ? "INSERT INTO canal_digital (id_dependencia, tipo, nombre, valor, descripcion, fecha_ultima_verificacion) "
                  + "VALUES (?,?,?,?,?, CURRENT_DATE)"
                : "UPDATE canal_digital SET id_dependencia=?, tipo=?, nombre=?, valor=?, descripcion=?, "
                  + "fecha_ultima_verificacion=CURRENT_DATE WHERE id_canal=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, c.getIdDependencia());
            ps.setString(2, c.getTipo());
            ps.setString(3, c.getNombre());
            ps.setString(4, c.getValor());
            ps.setString(5, c.getDescripcion());
            if (c.getId() != 0) {
                ps.setInt(6, c.getId());
            }
            ps.executeUpdate();
        }
    }

    public void eliminarCanal(Connection cn, int id) throws SQLException {
        ejecutar(cn, "DELETE FROM canal_digital WHERE id_canal = ?", id);
    }

    // ------------------------------------------------------------------ reglas de derivación
    public List<ReglaDerivacion> listarReglas(Connection cn, boolean soloActivas) throws SQLException {
        String sql = "SELECT r.*, d.nombre AS dependencia FROM regla_derivacion r JOIN dependencia d USING (id_dependencia) "
                + (soloActivas ? "WHERE r.activa " : "") + "ORDER BY d.nombre, r.tema";
        List<ReglaDerivacion> l = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ReglaDerivacion r = new ReglaDerivacion();
                r.setId(rs.getInt("id_regla_derivacion"));
                r.setIdDependencia(rs.getInt("id_dependencia"));
                r.setDependencia(rs.getString("dependencia"));
                r.setTema(rs.getString("tema"));
                r.setPalabrasClave(rs.getString("palabras_clave"));
                r.setIndicacion(rs.getString("indicacion"));
                r.setActiva(rs.getBoolean("activa"));
                r.setFechaUltimaVerificacion(fecha(rs, "fecha_ultima_verificacion"));
                l.add(r);
            }
        }
        return l;
    }

    public void guardarRegla(Connection cn, ReglaDerivacion r) throws SQLException {
        String sql = r.getId() == 0
                ? "INSERT INTO regla_derivacion (id_dependencia, tema, palabras_clave, indicacion, activa, fecha_ultima_verificacion) "
                  + "VALUES (?,?,?,?,?, CURRENT_DATE)"
                : "UPDATE regla_derivacion SET id_dependencia=?, tema=?, palabras_clave=?, indicacion=?, activa=?, "
                  + "fecha_ultima_verificacion=CURRENT_DATE WHERE id_regla_derivacion=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdDependencia());
            ps.setString(2, r.getTema());
            ps.setString(3, r.getPalabrasClave());
            ps.setString(4, r.getIndicacion());
            ps.setBoolean(5, r.isActiva());
            if (r.getId() != 0) {
                ps.setInt(6, r.getId());
            }
            ps.executeUpdate();
        }
    }

    public void eliminarRegla(Connection cn, int id) throws SQLException {
        ejecutar(cn, "DELETE FROM regla_derivacion WHERE id_regla_derivacion = ?", id);
    }

    private static java.time.LocalDate fecha(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d == null ? null : d.toLocalDate();
    }

    private void ejecutar(Connection cn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
