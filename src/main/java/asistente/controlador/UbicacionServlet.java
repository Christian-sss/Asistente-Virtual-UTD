package asistente.controlador;

import asistente.config.Configuracion;
import asistente.modelo.CanalDigital;
import asistente.modelo.Dependencia;
import asistente.modelo.Paso;
import asistente.modelo.PuntoAtencion;
import asistente.modelo.Tramite;
import asistente.servicio.ServicioEstadistica;
import asistente.servicio.ServicioOrientacion;
import asistente.servicio.ServicioUbicacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ubicación de puntos de trámite y canales digitales (RF-09). La página dibuja con Leaflet.js el
 * plano del campus; /ubicacion/puntos entrega en JSON los puntos y la secuencia de visita del
 * trámite, y /ubicacion/canales los portales, correos y plataformas de una dependencia.
 */
@WebServlet(name = "UbicacionServlet", urlPatterns = {"/ubicacion", "/ubicacion/puntos", "/ubicacion/canales"})
public class UbicacionServlet extends ServletBase {

    private final ServicioUbicacion servicio = new ServicioUbicacion();
    private final ServicioOrientacion orientacion = new ServicioOrientacion();
    private final ServicioEstadistica estadistica = new ServicioEstadistica();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer idTramite = entero(req, "tramite");
        Tramite t = idTramite == null ? null : orientacion.obtenerTramitePublicado(idTramite);
        switch (req.getServletPath()) {
            case "/ubicacion/puntos" -> json(resp, Json.de(puntos(req, t)));
            case "/ubicacion/canales" -> {
                Integer dep = entero(req, "dependencia");
                List<Map<String, Object>> l = new ArrayList<>();
                for (CanalDigital c : servicio.canales(dep)) {
                    l.add(canal(c));
                }
                json(resp, Json.de(l));
            }
            default -> {
                Integer dep = entero(req, "dependencia");
                estadistica.registrar("UBICACION", t == null ? null : t.getId(), dep);
                req.setAttribute("dependencias", servicio.dependencias());
                req.setAttribute("tramite", t);
                req.setAttribute("dependenciaSel", dep);
                req.setAttribute("tramites", orientacion.buscarTramites(null, null));
                vista(req, resp, "ubicacion");
            }
        }
    }

    private Map<String, Object> puntos(HttpServletRequest req, Tramite t) {
        Map<String, Object> r = new LinkedHashMap<>();
        Map<String, Object> plano = new LinkedHashMap<>();
        String img = Configuracion.planoImagen();
        plano.put("imagen", img.startsWith("http") ? img : req.getContextPath() + img);
        plano.put("ancho", ServicioUbicacion.Plano.ANCHO);
        plano.put("alto", ServicioUbicacion.Plano.ALTO);
        r.put("plano", plano);
        List<Map<String, Object>> puntos = new ArrayList<>();
        for (Dependencia d : servicio.dependencias()) {
            for (PuntoAtencion p : d.getPuntos()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("nombre", p.getNombre());
                m.put("idDependencia", d.getId());
                m.put("dependencia", d.getNombre());
                m.put("sede", p.getSede());
                m.put("referencia", p.getReferencia());
                m.put("direccion", p.getDireccion());
                m.put("horario", p.getHorario());
                m.put("telefono", p.getTelefono());
                m.put("correo", p.getCorreo());
                m.put("x", p.getCoordX());
                m.put("y", p.getCoordY());
                m.put("verificado", p.getFechaUltimaVerificacion() == null ? null : p.getFechaUltimaVerificacion().toString());
                m.put("revisar", p.isRevisionPendiente());
                puntos.add(m);
            }
        }
        r.put("puntos", puntos);
        List<Map<String, Object>> secuencia = new ArrayList<>();
        if (t != null) {
            r.put("tramite", t.getNombre());
            int orden = 0;
            Integer anterior = null;
            for (Paso p : t.getPasos()) {
                if (p.getPunto() == null || p.getPunto().getId() == (anterior == null ? -1 : anterior)) {
                    continue;
                }
                anterior = p.getPunto().getId();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("orden", ++orden);
                m.put("idPunto", p.getPunto().getId());
                m.put("paso", p.getOrden());
                m.put("descripcion", p.getDescripcion());
                secuencia.add(m);
            }
            if (secuencia.isEmpty() && t.getPunto() != null) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("orden", 1);
                m.put("idPunto", t.getPunto().getId());
                m.put("paso", null);
                m.put("descripcion", "Presente su expediente en " + t.getPunto().getNombre());
                secuencia.add(m);
            }
        }
        r.put("secuencia", secuencia);
        return r;
    }

    private static Map<String, Object> canal(CanalDigital c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("dependencia", c.getDependencia());
        m.put("tipo", c.getTipoTexto());
        m.put("nombre", c.getNombre());
        m.put("valor", c.getValor());
        m.put("enlace", c.isEnlace());
        m.put("descripcion", c.getDescripcion());
        m.put("verificado", c.getFechaUltimaVerificacion() == null ? null : c.getFechaUltimaVerificacion().toString());
        return m;
    }
}
