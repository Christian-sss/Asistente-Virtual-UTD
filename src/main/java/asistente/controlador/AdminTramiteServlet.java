package asistente.controlador;

import asistente.modelo.Paso;
import asistente.modelo.ReglaRevision;
import asistente.modelo.Requisito;
import asistente.modelo.Tramite;
import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioContenidos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gestión de trámites y pasos (RF-12), requisitos con sustento normativo y reglas de revisión (RF-13).
 */
@WebServlet(name = "AdminTramiteServlet", urlPatterns = {"/admin/tramites", "/admin/tramite"})
public class AdminTramiteServlet extends ServletBase {

    private final ServicioContenidos servicio = new ServicioContenidos();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/admin/tramites".equals(req.getServletPath())) {
            req.setAttribute("tramites", servicio.listarTramites());
            vista(req, resp, "admin/tramites");
            return;
        }
        Integer id = entero(req, "id");
        Tramite t = id == null ? new Tramite() : servicio.tramite(id);
        if (t == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El trámite no existe.");
            return;
        }
        req.setAttribute("tramite", t);
        req.setAttribute("categorias", servicio.categorias());
        req.setAttribute("puntos", servicio.puntos());
        if (id != null) {
            req.setAttribute("reglas", servicio.reglas(id));
            req.setAttribute("articulos", servicio.articulos());
        }
        vista(req, resp, "admin/tramite");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioAdministrativo u = usuario(req);
        Integer id = entero(req, "id");
        String accion = req.getParameter("accion");
        String ancla = "";
        try {
            switch (accion == null ? "" : accion) {
                case "guardar" -> {
                    Tramite t = new Tramite();
                    t.setId(id == null ? 0 : id);
                    t.setIdCategoria(enteroObligatorio(req, "idCategoria"));
                    t.setIdPunto(entero(req, "idPunto"));
                    t.setCodigo(texto(req, "codigo"));
                    t.setCodigoTupa(texto(req, "codigoTupa"));
                    t.setNombre(texto(req, "nombre"));
                    t.setDescripcion(texto(req, "descripcion"));
                    t.setDirigidoA(texto(req, "dirigidoA"));
                    t.setDerechoPago(decimal(req, "derechoPago"));
                    t.setDetallePago(texto(req, "detallePago"));
                    t.setPlazoReferencial(texto(req, "plazoReferencial"));
                    t.setCalificacion(texto(req, "calificacion"));
                    id = servicio.guardarTramite(u, t);
                    mensaje(req, "success", "Datos del trámite guardados.");
                }
                case "publicar", "despublicar" -> {
                    servicio.cambiarEstado(u, id, "publicar".equals(accion));
                    mensaje(req, "success", "publicar".equals(accion) ? "Trámite publicado." : "Trámite retirado de la publicación.");
                }
                case "paso" -> {
                    Paso p = new Paso();
                    p.setIdTramite(id);
                    p.setDescripcion(texto(req, "descripcion"));
                    p.setPagoPrevio(req.getParameter("pagoPrevio") != null);
                    servicio.agregarPaso(u, p);
                    mensaje(req, "success", "Paso agregado.");
                    ancla = "#pasos";
                }
                case "eliminarPaso" -> {
                    servicio.eliminarPaso(u, id, enteroObligatorio(req, "idPaso"));
                    mensaje(req, "success", "Paso eliminado.");
                    ancla = "#pasos";
                }
                case "requisito" -> {
                    Requisito r = new Requisito();
                    r.setIdTramite(id);
                    r.setDescripcion(texto(req, "descripcion"));
                    r.setObligatorio(req.getParameter("obligatorio") != null);
                    servicio.agregarRequisito(u, r, entero(req, "idArticulo"));
                    mensaje(req, "success", "Requisito agregado con su sustento normativo.");
                    ancla = "#requisitos";
                }
                case "eliminarRequisito" -> {
                    servicio.eliminarRequisito(u, id, enteroObligatorio(req, "idRequisito"));
                    mensaje(req, "success", "Requisito eliminado.");
                    ancla = "#requisitos";
                }
                case "vincular" -> {
                    servicio.vincularArticulo(u, enteroObligatorio(req, "idRequisito"), enteroObligatorio(req, "idArticulo"));
                    mensaje(req, "success", "Artículo normativo vinculado.");
                    ancla = "#requisitos";
                }
                case "desvincular" -> {
                    servicio.desvincularArticulo(u, enteroObligatorio(req, "idRequisito"), enteroObligatorio(req, "idArticulo"));
                    mensaje(req, "success", "Vínculo normativo retirado.");
                    ancla = "#requisitos";
                }
                case "regla" -> {
                    ReglaRevision r = new ReglaRevision();
                    r.setIdTramite(id);
                    r.setCriterio(texto(req, "criterio"));
                    r.setTipo(texto(req, "tipo"));
                    r.setExpresion(texto(req, "expresion"));
                    r.setMensajeCorreccion(texto(req, "mensajeCorreccion"));
                    servicio.agregarRegla(u, r);
                    mensaje(req, "success", "Regla de revisión agregada. Se aplicará en la siguiente revisión, sin recompilar.");
                    ancla = "#reglas";
                }
                case "activarRegla", "desactivarRegla" -> {
                    servicio.cambiarRegla(u, enteroObligatorio(req, "idRegla"), "activarRegla".equals(accion));
                    mensaje(req, "success", "Estado de la regla actualizado.");
                    ancla = "#reglas";
                }
                case "eliminarRegla" -> {
                    servicio.eliminarRegla(u, enteroObligatorio(req, "idRegla"));
                    mensaje(req, "success", "Regla eliminada.");
                    ancla = "#reglas";
                }
                default -> mensaje(req, "warning", "Acción no reconocida.");
            }
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
        }
        redirigir(req, resp, id == null ? "/admin/tramite" : "/admin/tramite?id=" + id + ancla);
    }
}
