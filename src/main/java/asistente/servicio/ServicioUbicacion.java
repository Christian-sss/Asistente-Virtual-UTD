package asistente.servicio;

import asistente.dao.BitacoraDAO;
import asistente.dao.UbicacionDAO;
import asistente.modelo.CanalDigital;
import asistente.modelo.Dependencia;
import asistente.modelo.PuntoAtencion;
import asistente.modelo.ReglaDerivacion;
import asistente.modelo.UsuarioAdministrativo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Consulta de puntos de atención y canales digitales (RF-09) y su mantenimiento junto con las
 * reglas de derivación (RF-14). Cada registro guarda su fecha de última verificación (RN-12).
 */
public class ServicioUbicacion extends ServicioBase {

    public static final Set<String> TIPOS_CANAL = Set.of("PORTAL", "MESA_PARTES_VIRTUAL", "CORREO", "PLATAFORMA", "TELEFONO", "RED_SOCIAL");

    private final UbicacionDAO dao = new UbicacionDAO();
    private final BitacoraDAO bitacora = new BitacoraDAO();

    public List<Dependencia> dependencias() {
        return consultar(dao::listarDependencias);
    }

    public Dependencia dependencia(int id) {
        return consultar(cn -> dao.buscarDependencia(cn, id));
    }

    public List<CanalDigital> canales(Integer idDependencia) {
        return consultar(cn -> dao.listarCanales(cn, idDependencia));
    }

    public List<ReglaDerivacion> reglas() {
        return consultar(cn -> dao.listarReglas(cn, false));
    }

    // ------------------------------------------------------------------ dependencias
    public int guardarDependencia(UsuarioAdministrativo u, Dependencia d) {
        obligatorio(d.getNombre(), "El nombre de la dependencia es obligatorio.");
        return enTransaccion(cn -> {
            int id = dao.guardarDependencia(cn, d);
            bitacora.insertar(cn, u.getId(), "dependencia", d.getId() == 0 ? "REGISTRO" : "EDICION", null, d.getNombre());
            return id;
        });
    }

    public void eliminarDependencia(UsuarioAdministrativo u, int id) {
        enTransaccion(cn -> {
            if (dao.contarPuntosDeDependencia(cn, id) > 0) {
                throw new ReglaNegocioException("No se puede eliminar: la dependencia tiene puntos de atención. Elimínelos o reasígnelos primero.");
            }
            dao.eliminarDependencia(cn, id);
            bitacora.insertar(cn, u.getId(), "dependencia", "ELIMINACION", "Dependencia " + id, null);
            return null;
        });
    }

    // ------------------------------------------------------------------ puntos de atención
    public void guardarPunto(UsuarioAdministrativo u, PuntoAtencion p) {
        obligatorio(p.getNombre(), "El nombre del punto de atención es obligatorio.");
        obligatorio(p.getSede(), "La sede es obligatoria.");
        obligatorio(p.getDireccion(), "La dirección es obligatoria.");
        obligatorio(p.getHorario(), "El horario de atención es obligatorio.");
        if (p.getIdDependencia() == null) {
            throw new ReglaNegocioException("Seleccione la dependencia del punto de atención.");
        }
        if ((p.getLatitud() == null) != (p.getLongitud() == null)) {
            throw new ReglaNegocioException("Indique la latitud y la longitud del punto, o deje ambas vacías.");
        }
        if (p.getLatitud() != null && !Mapa.dentroDeTacna(p.getLatitud(), p.getLongitud())) {
            throw new ReglaNegocioException("La ubicación debe estar en la ciudad de Tacna (latitud entre "
                    + Mapa.LAT_MIN + " y " + Mapa.LAT_MAX + ", longitud entre " + Mapa.LON_MIN + " y " + Mapa.LON_MAX
                    + "). Revise que no haya invertido la latitud y la longitud.");
        }
        enTransaccion(cn -> {
            dao.guardarPunto(cn, p);
            bitacora.insertar(cn, u.getId(), "punto_atencion", p.getId() == 0 ? "REGISTRO" : "EDICION", null,
                    p.getNombre() + (p.getLatitud() == null ? "" : " (" + p.getLatitud() + ", " + p.getLongitud() + ")"));
            return null;
        });
    }

    public void eliminarPunto(UsuarioAdministrativo u, int id) {
        enTransaccion(cn -> {
            dao.eliminarPunto(cn, id);
            bitacora.insertar(cn, u.getId(), "punto_atencion", "ELIMINACION", "Punto " + id, null);
            return null;
        });
    }

    // ------------------------------------------------------------------ canales digitales
    public void guardarCanal(UsuarioAdministrativo u, CanalDigital c) {
        obligatorio(c.getNombre(), "El nombre del canal es obligatorio.");
        obligatorio(c.getValor(), "Indique la dirección web, el correo o el número del canal.");
        if (!TIPOS_CANAL.contains(c.getTipo())) {
            throw new ReglaNegocioException("Seleccione el tipo de canal.");
        }
        String v = c.getValor().strip();
        boolean web = Set.of("PORTAL", "MESA_PARTES_VIRTUAL", "PLATAFORMA", "RED_SOCIAL").contains(c.getTipo());
        if (web && !v.startsWith("https://") && !v.startsWith("http://")) {
            throw new ReglaNegocioException("Para un portal, plataforma o mesa de partes virtual indique la dirección completa (https://...).");
        }
        if ("CORREO".equals(c.getTipo()) && !v.matches("[^\\s@,]+@[^\\s@,]+(\\s*,\\s*[^\\s@,]+@[^\\s@,]+)*")) {
            throw new ReglaNegocioException("Indique uno o más correos válidos separados por comas.");
        }
        c.setValor(v);
        enTransaccion(cn -> {
            dao.guardarCanal(cn, c);
            bitacora.insertar(cn, u.getId(), "canal_digital", c.getId() == 0 ? "REGISTRO" : "EDICION", null,
                    c.getTipo() + ": " + c.getNombre() + " - " + c.getValor());
            return null;
        });
    }

    public void eliminarCanal(UsuarioAdministrativo u, int id) {
        enTransaccion(cn -> {
            dao.eliminarCanal(cn, id);
            bitacora.insertar(cn, u.getId(), "canal_digital", "ELIMINACION", "Canal " + id, null);
            return null;
        });
    }

    // ------------------------------------------------------------------ reglas de derivación
    public void guardarRegla(UsuarioAdministrativo u, ReglaDerivacion r) {
        obligatorio(r.getTema(), "El tema de la regla es obligatorio.");
        obligatorio(r.getPalabrasClave(), "Escriba al menos una palabra clave.");
        if (r.getIdDependencia() == 0) {
            throw new ReglaNegocioException("Seleccione la dependencia competente.");
        }
        enTransaccion(cn -> {
            dao.guardarRegla(cn, r);
            bitacora.insertar(cn, u.getId(), "regla_derivacion", r.getId() == 0 ? "REGISTRO" : "EDICION", null,
                    r.getTema() + " -> dependencia " + r.getIdDependencia());
            return null;
        });
    }

    public void eliminarRegla(UsuarioAdministrativo u, int id) {
        enTransaccion(cn -> {
            dao.eliminarRegla(cn, id);
            bitacora.insertar(cn, u.getId(), "regla_derivacion", "ELIMINACION", "Regla " + id, null);
            return null;
        });
    }

    private static void obligatorio(String v, String mensaje) {
        if (v == null || v.isBlank()) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    /**
     * Mapa real del campus (OpenStreetMap, coordenadas WGS84). El centro es la Ciudad Universitaria
     * de la UNJBG (Sede Los Granados, junto al Óvalo Cusco). Los puntos se aceptan dentro de la
     * ciudad de Tacna para admitir también otras sedes.
     */
    public static final class Mapa {
        public static final double CENTRO_LAT = -18.02485;
        public static final double CENTRO_LON = -70.24913;
        public static final int ZOOM = 17;
        public static final double LAT_MIN = -18.15;
        public static final double LAT_MAX = -17.90;
        public static final double LON_MIN = -70.35;
        public static final double LON_MAX = -70.15;

        private Mapa() {
        }

        public static boolean dentroDeTacna(BigDecimal lat, BigDecimal lon) {
            double la = lat.doubleValue(), lo = lon.doubleValue();
            return la >= LAT_MIN && la <= LAT_MAX && lo >= LON_MIN && lo <= LON_MAX;
        }
    }
}
