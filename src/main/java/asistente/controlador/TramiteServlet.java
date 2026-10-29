package asistente.controlador;

import asistente.modelo.Tramite;
import asistente.servicio.ServicioOrientacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Página de inicio y catálogo de trámites publicados con búsqueda por categoría o palabra clave (RF-03).
 */
@WebServlet(name = "TramiteServlet", urlPatterns = {"", "/inicio", "/tramites", "/tramite"})
public class TramiteServlet extends ServletBase {

    private final ServicioOrientacion servicio = new ServicioOrientacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ruta = req.getServletPath();
        req.setAttribute("categorias", servicio.listarCategorias());
        switch (ruta) {
            case "/tramites" -> {
                String q = texto(req, "q");
                Integer cat = entero(req, "categoria");
                req.setAttribute("q", q);
                req.setAttribute("categoriaSel", cat);
                req.setAttribute("tramites", servicio.buscarTramites(q, cat));
                vista(req, resp, "tramites");
            }
            case "/tramite" -> {
                Integer id = entero(req, "id");
                Tramite t = id == null ? null : servicio.obtenerTramitePublicado(id);
                if (t == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El trámite no existe o no está publicado.");
                    return;
                }
                redirigir(req, resp, "/hoja-ruta?id=" + t.getId());
            }
            default -> {
                req.setAttribute("tramites", servicio.buscarTramites(null, null));
                vista(req, resp, "inicio");
            }
        }
    }
}
