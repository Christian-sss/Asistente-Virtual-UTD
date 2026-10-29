package asistente.controlador;

import asistente.modelo.Normativa;
import asistente.modelo.UsuarioAdministrativo;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioContenidos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Gestión de normativas y sus artículos, con vigencia y fuente oficial (RF-13).
 */
@WebServlet(name = "AdminNormativaServlet", urlPatterns = "/admin/normativas")
public class AdminNormativaServlet extends ServletBase {

    private final ServicioContenidos servicio = new ServicioContenidos();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = entero(req, "id");
        req.setAttribute("normativas", servicio.normativas());
        if (id != null) {
            req.setAttribute("editar", servicio.normativa(id));
        } else if (req.getParameter("nueva") != null) {
            req.setAttribute("editar", new Normativa());
        }
        vista(req, resp, "admin/normativas");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioAdministrativo u = usuario(req);
        Integer id = entero(req, "id");
        String accion = req.getParameter("accion");
        try {
            switch (accion == null ? "" : accion) {
                case "guardar" -> {
                    Normativa n = new Normativa();
                    n.setId(id == null ? 0 : id);
                    n.setTipo(texto(req, "tipo"));
                    n.setNumero(texto(req, "numero"));
                    n.setTitulo(texto(req, "titulo"));
                    n.setOrganoEmisor(texto(req, "organoEmisor"));
                    String f = texto(req, "fechaAprobacion");
                    n.setFechaAprobacion(f == null ? null : LocalDate.parse(f));
                    n.setVigente(req.getParameter("vigente") != null);
                    n.setFuenteOficial(texto(req, "fuenteOficial"));
                    id = servicio.guardarNormativa(u, n);
                    mensaje(req, "success", "Normativa guardada; se actualizó su fecha de verificación.");
                }
                case "articulo" -> {
                    servicio.agregarArticulo(u, id, texto(req, "numeroArticulo"), texto(req, "contenido"));
                    mensaje(req, "success", "Artículo agregado.");
                }
                case "eliminarArticulo" -> {
                    servicio.eliminarArticulo(u, enteroObligatorio(req, "idArticulo"));
                    mensaje(req, "success", "Artículo eliminado.");
                }
                default -> mensaje(req, "warning", "Acción no reconocida.");
            }
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
        } catch (DateTimeParseException e) {
            mensaje(req, "danger", "La fecha de aprobación no es válida.");
        }
        redirigir(req, resp, "/admin/normativas" + (id == null ? "" : "?id=" + id));
    }
}
