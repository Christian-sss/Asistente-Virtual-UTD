package asistente.controlador;

import asistente.modelo.Categoria;
import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioContenidos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gestión de las categorías del catálogo de trámites (RF-12).
 */
@WebServlet(name = "AdminCategoriaServlet", urlPatterns = "/admin/categorias")
public class AdminCategoriaServlet extends ServletBase {

    private final ServicioContenidos servicio = new ServicioContenidos();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("categorias", servicio.categorias());
        req.setAttribute("editarId", entero(req, "id"));
        vista(req, resp, "admin/categorias");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioAdministrativo u = usuario(req);
        String accion = req.getParameter("accion");
        try {
            if ("eliminar".equals(accion)) {
                servicio.eliminarCategoria(u, enteroObligatorio(req, "id"));
                mensaje(req, "success", "Categoría eliminada.");
            } else {
                Categoria c = new Categoria();
                Integer id = entero(req, "id");
                c.setId(id == null ? 0 : id);
                c.setNombre(texto(req, "nombre"));
                c.setDescripcion(texto(req, "descripcion"));
                c.setIcono(texto(req, "icono"));
                Integer orden = entero(req, "orden");
                c.setOrden(orden == null ? 0 : orden);
                servicio.guardarCategoria(u, c);
                mensaje(req, "success", "Categoría guardada.");
            }
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
        } catch (IllegalStateException e) {
            mensaje(req, "danger", "No se pudo guardar: verifique que el nombre de la categoría no esté repetido.");
        }
        redirigir(req, resp, "/admin/categorias");
    }
}
