package asistente.controlador;

import asistente.modelo.PreguntaGuia;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioOrientacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Identificación del trámite mediante preguntas guiadas sobre el árbol de decisión (RF-04).
 */
@WebServlet(name = "OrientacionServlet", urlPatterns = "/orientacion")
public class OrientacionServlet extends ServletBase {

    private static final String RUTA = "rutaOrientacion";
    private final ServicioOrientacion servicio = new ServicioOrientacion();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = entero(req, "pregunta");
        PreguntaGuia p = id == null ? servicio.preguntaInicial() : servicio.pregunta(id);
        if (p == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "La pregunta no existe.");
            return;
        }
        if (p.isInicial()) {
            req.getSession().setAttribute(RUTA, new ArrayList<String>());
        }
        req.setAttribute("pregunta", p);
        vista(req, resp, "orientacion");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer idOpcion = entero(req, "opcion");
        if (idOpcion == null) {
            mensaje(req, "warning", "Seleccione una opción para continuar.");
            Integer p = entero(req, "pregunta");
            redirigir(req, resp, "/orientacion" + (p == null ? "" : "?pregunta=" + p));
            return;
        }
        HttpSession s = req.getSession();
        List<String> ruta = (List<String>) s.getAttribute(RUTA);
        if (ruta == null) {
            ruta = new ArrayList<>();
            s.setAttribute(RUTA, ruta);
        }
        try {
            ServicioOrientacion.Siguiente sig = servicio.resolverSiguiente(idOpcion);
            ruta.add(sig.opcionElegida());
            if (sig.tramite() != null) {
                s.setAttribute("tramiteIdentificado", sig.tramite().getId());
                redirigir(req, resp, "/hoja-ruta?id=" + sig.tramite().getId() + "&identificado=1");
            } else {
                redirigir(req, resp, "/orientacion?pregunta=" + sig.pregunta().getId());
            }
        } catch (ReglaNegocioException e) {
            mensaje(req, "warning", e.getMessage());
            redirigir(req, resp, "/orientacion");
        }
    }
}
