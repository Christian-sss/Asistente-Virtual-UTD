package asistente.servicio;

import asistente.dao.EstadisticaDAO;
import asistente.dao.EstadisticaDAO.Reporte;
import asistente.dao.BitacoraDAO;
import asistente.modelo.Retroalimentacion;
import asistente.modelo.UsuarioAdministrativo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registro anónimo de consultas (RN-14), retroalimentación de los usuarios (RF-11) y reportes
 * estadísticos por periodo con exportación a CSV (RF-16).
 */
public class ServicioEstadistica extends ServicioBase {

    private static final Logger LOG = Logger.getLogger(ServicioEstadistica.class.getName());
    public static final Set<String> TIPOS_RETRO = Set.of("REQUISITO", "PAGO", "PLAZO", "UBICACION", "NORMATIVA", "OTRO");
    public static final Set<String> REPORTES = Set.of("consultas", "servicios", "observaciones", "retroalimentacion", "bitacora");

    private final EstadisticaDAO dao = new EstadisticaDAO();
    private final BitacoraDAO bitacora = new BitacoraDAO();

    /** Registra una consulta anónima. Nunca interrumpe la atención al usuario si falla. */
    public void registrar(String tipo, Integer idTramite, Integer idDependencia) {
        try {
            enTransaccion(cn -> {
                dao.registrarConsulta(cn, tipo, idTramite, idDependencia);
                return null;
            });
        } catch (RuntimeException e) {
            LOG.log(Level.FINE, "No se pudo registrar la consulta", e);
        }
    }

    // ------------------------------------------------------------------ retroalimentación (RF-11)
    public void enviarRetroalimentacion(Integer idTramite, String tipo, String comentario) {
        if (tipo == null || !TIPOS_RETRO.contains(tipo)) {
            throw new ReglaNegocioException("Seleccione qué información es inexacta.");
        }
        if (comentario == null || comentario.strip().length() < 10) {
            throw new ReglaNegocioException("Describa brevemente qué información es inexacta (al menos 10 caracteres).");
        }
        Retroalimentacion r = new Retroalimentacion();
        r.setIdTramite(idTramite);
        r.setTipo(tipo);
        r.setComentario(anonimizar(comentario.strip()));
        enTransaccion(cn -> {
            dao.registrarRetroalimentacion(cn, r);
            return null;
        });
    }

    /** No se solicitan datos personales; si el usuario escribe un DNI, correo o teléfono, se oculta. */
    static String anonimizar(String texto) {
        String t = texto.replaceAll("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+", "[correo omitido]")
                .replaceAll("\\b\\d{8}\\b", "[DNI omitido]")
                .replaceAll("\\b9\\d{8}\\b", "[teléfono omitido]");
        return t.length() > 500 ? t.substring(0, 500) : t;
    }

    public List<Retroalimentacion> retroalimentacion(LocalDate desde, LocalDate hasta) {
        return consultar(cn -> dao.listarRetroalimentacion(cn, desde, hasta));
    }

    public int retroalimentacionPendiente() {
        return consultar(dao::contarRetroalimentacionPendiente);
    }

    public void marcarAtendida(UsuarioAdministrativo u, int id, boolean atendida) {
        enTransaccion(cn -> {
            dao.marcarAtendida(cn, id, atendida);
            bitacora.insertar(cn, u.getId(), "retroalimentacion", atendida ? "ATENCION" : "REAPERTURA", null, "Reporte " + id);
            return null;
        });
    }

    // ------------------------------------------------------------------ reportes (RF-16)
    public Reporte reporte(String tipo, LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la final.");
        }
        return consultar(cn -> switch (tipo) {
            case "servicios" -> dao.consultasPorServicio(cn, desde, hasta);
            case "observaciones" -> dao.observacionesFrecuentes(cn, desde, hasta);
            case "retroalimentacion" -> dao.retroalimentacion(cn, desde, hasta);
            case "bitacora" -> dao.bitacora(cn, desde, hasta);
            default -> dao.tramitesMasConsultados(cn, desde, hasta);
        });
    }

    /** Registra en la bitácora que se exportó un reporte. */
    public void registrarExportacion(UsuarioAdministrativo u, String tipo, LocalDate desde, LocalDate hasta) {
        enTransaccion(cn -> {
            bitacora.insertar(cn, u.getId(), "reporte", "EXPORTACION", null, tipo + " " + desde + " a " + hasta);
            return null;
        });
    }

    /** CSV con separador punto y coma y BOM UTF-8, para abrirse directamente en Excel en español. */
    public static String aCsv(Reporte r) {
        StringBuilder sb = new StringBuilder("﻿");
        sb.append(linea(r.columnas()));
        for (List<String> fila : r.filas()) {
            sb.append(linea(fila));
        }
        return sb.toString();
    }

    private static String linea(List<String> valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            String v = valores.get(i) == null ? "" : valores.get(i);
            // Evita la inyección de fórmulas al abrir el CSV en una hoja de cálculo.
            if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0 && !v.matches("-?\\d+([.,]\\d+)?")) {
                v = "'" + v;
            }
            sb.append('"').append(v.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ")).append('"');
        }
        return sb.append("\r\n").toString();
    }
}
