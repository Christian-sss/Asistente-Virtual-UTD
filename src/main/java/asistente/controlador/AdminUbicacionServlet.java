package asistente.controlador;

import asistente.modelo.CanalDigital;
import asistente.modelo.Dependencia;
import asistente.modelo.PuntoAtencion;
import asistente.modelo.ReglaDerivacion;
import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioUbicacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Mantenimiento de dependencias, puntos de atención con coordenadas, canales digitales (/admin/ubicaciones)
 * y reglas de derivación (/admin/derivacion), con fecha de última verificación (RF-14, RN-12).
 */
@WebServlet(name = "AdminUbicacionServlet", urlPatterns = {"/admin/ubicaciones", "/admin/derivacion"})
public class AdminUbicacionServlet extends ServletBase {

    private final ServicioUbicacion servicio = new ServicioUbicacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("dependencias", servicio.dependencias());
        if ("/admin/derivacion".equals(req.getServletPath())) {
            req.setAttribute("reglas", servicio.reglas());
            req.setAttribute("editarId", entero(req, "id"));
            vista(req, resp, "admin/derivacion");
            return;
        }
        req.setAttribute("dependenciaSel", entero(req, "dependencia"));
        req.setAttribute("puntoSel", entero(req, "punto"));
        req.setAttribute("canalSel", entero(req, "canal"));
        req.setAttribute("mapaLat", ServicioUbicacion.Mapa.CENTRO_LAT);
        req.setAttribute("mapaLon", ServicioUbicacion.Mapa.CENTRO_LON);
        req.setAttribute("mapaZoom", ServicioUbicacion.Mapa.ZOOM);
        vista(req, resp, "admin/ubicaciones");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioAdministrativo u = usuario(req);
        String accion = req.getParameter("accion");
        String volver = "/admin/derivacion".equals(req.getServletPath()) ? "/admin/derivacion" : "/admin/ubicaciones";
        Integer dep = entero(req, "idDependencia");
        try {
            switch (accion == null ? "" : accion) {
                case "dependencia" -> {
                    Dependencia d = new Dependencia();
                    Integer id = entero(req, "id");
                    d.setId(id == null ? 0 : id);
                    d.setNombre(texto(req, "nombre"));
                    d.setSigla(texto(req, "sigla"));
                    d.setFunciones(texto(req, "funciones"));
                    dep = servicio.guardarDependencia(u, d);
                    mensaje(req, "success", "Dependencia guardada.");
                }
                case "eliminarDependencia" -> {
                    servicio.eliminarDependencia(u, enteroObligatorio(req, "id"));
                    dep = null;
                    mensaje(req, "success", "Dependencia eliminada.");
                }
                case "punto" -> {
                    PuntoAtencion p = new PuntoAtencion();
                    Integer id = entero(req, "id");
                    p.setId(id == null ? 0 : id);
                    p.setIdDependencia(dep);
                    p.setNombre(texto(req, "nombre"));
                    p.setSede(texto(req, "sede"));
                    p.setDireccion(texto(req, "direccion"));
                    p.setHorario(texto(req, "horario"));
                    p.setTelefono(texto(req, "telefono"));
                    p.setCorreo(texto(req, "correo"));
                    p.setReferencia(texto(req, "referencia"));
                    p.setLatitud(decimal(req, "latitud"));
                    p.setLongitud(decimal(req, "longitud"));
                    servicio.guardarPunto(u, p);
                    mensaje(req, "success", "Punto de atención guardado; se actualizó su fecha de verificación.");
                }
                case "eliminarPunto" -> {
                    servicio.eliminarPunto(u, enteroObligatorio(req, "id"));
                    mensaje(req, "success", "Punto de atención eliminado. Los trámites que lo usaban quedaron sin punto asignado.");
                }
                case "canal" -> {
                    CanalDigital c = new CanalDigital();
                    Integer id = entero(req, "id");
                    c.setId(id == null ? 0 : id);
                    c.setIdDependencia(dep == null ? 0 : dep);
                    c.setTipo(texto(req, "tipo"));
                    c.setNombre(texto(req, "nombre"));
                    c.setValor(texto(req, "valor"));
                    c.setDescripcion(texto(req, "descripcion"));
                    servicio.guardarCanal(u, c);
                    mensaje(req, "success", "Canal digital guardado.");
                }
                case "eliminarCanal" -> {
                    servicio.eliminarCanal(u, enteroObligatorio(req, "id"));
                    mensaje(req, "success", "Canal digital eliminado.");
                }
                case "regla" -> {
                    ReglaDerivacion r = new ReglaDerivacion();
                    Integer id = entero(req, "id");
                    r.setId(id == null ? 0 : id);
                    r.setIdDependencia(dep == null ? 0 : dep);
                    r.setTema(texto(req, "tema"));
                    r.setPalabrasClave(texto(req, "palabrasClave"));
                    r.setIndicacion(texto(req, "indicacion"));
                    r.setActiva(req.getParameter("activa") != null);
                    servicio.guardarRegla(u, r);
                    mensaje(req, "success", "Regla de derivación guardada.");
                }
                case "eliminarRegla" -> {
                    servicio.eliminarRegla(u, enteroObligatorio(req, "id"));
                    mensaje(req, "success", "Regla de derivación eliminada.");
                }
                default -> mensaje(req, "warning", "Acción no reconocida.");
            }
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
        } catch (IllegalStateException e) {
            mensaje(req, "danger", "No se pudo guardar: verifique que el nombre no esté repetido.");
        }
        redirigir(req, resp, volver + ("/admin/ubicaciones".equals(volver) && dep != null ? "?dependencia=" + dep : ""));
    }
}
