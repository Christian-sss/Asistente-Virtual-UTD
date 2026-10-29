package asistente.controlador;

import asistente.modelo.RevisionDocumento;
import asistente.servicio.GeneradorHojaRutaPdf;
import asistente.servicio.ReglaNegocioException;
import asistente.servicio.ServicioOrientacion;
import asistente.servicio.ServicioRevisionDocumental;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Carga del documento para revisión preliminar (RF-06), reporte de conformidad y confirmación
 * asistida de firma, fotografías y anexos (RF-07).
 */
@WebServlet(name = "RevisionServlet", urlPatterns = {"/revision", "/revision/reporte", "/revision/confirmacion"})
@MultipartConfig(maxFileSize = 10L * 1024 * 1024, maxRequestSize = 11L * 1024 * 1024, fileSizeThreshold = 1024 * 1024)
public class RevisionServlet extends ServletBase {

    private static final String TOKEN = "tokenRevision";
    private static final String CLAUSULA = "clausulaAceptada";

    private final ServicioOrientacion orientacion = new ServicioOrientacion();
    private final ServicioRevisionDocumental servicio = new ServicioRevisionDocumental();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/revision/reporte".equals(req.getServletPath())) {
            Integer id = entero(req, "id");
            RevisionDocumento r = id == null ? null : servicio.reporte(id, token(req.getSession()));
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "El reporte no existe, ya expiró o pertenece a otra sesión.");
                return;
            }
            req.setAttribute("revision", r);
            req.setAttribute("aviso", GeneradorHojaRutaPdf.AVISO_REFERENCIAL);
            vista(req, resp, "reporte");
            return;
        }
        req.setAttribute("tramites", orientacion.buscarTramites(null, null));
        req.setAttribute("tramiteSel", entero(req, "tramite"));
        req.setAttribute("clausulaAceptada", Boolean.TRUE.equals(req.getSession().getAttribute(CLAUSULA)));
        vista(req, resp, "revision");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession();
        Part archivo;
        try {
            if (!csrfValido(req)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida (token de seguridad).");
                return;
            }
            if ("/revision/confirmacion".equals(req.getServletPath())) {
                int id = enteroObligatorio(req, "revision");
                Set<Integer> confirmados = new HashSet<>();
                String[] marcados = req.getParameterValues("confirmado");
                if (marcados != null) {
                    for (String m : marcados) {
                        confirmados.add(Integer.valueOf(m));
                    }
                }
                servicio.confirmar(id, token(s), confirmados);
                mensaje(req, "success", "Se registró su confirmación de los elementos visuales.");
                redirigir(req, resp, "/revision/reporte?id=" + id);
                return;
            }
            archivo = req.getPart("archivo");
        } catch (IllegalStateException e) {
            mensaje(req, "danger", "El archivo supera el tamaño máximo de 10 MB.");
            redirigir(req, resp, "/revision");
            return;
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", e.getMessage());
            redirigir(req, resp, "/revision");
            return;
        }

        Integer idTramite = entero(req, "tramite");
        if (req.getParameter("acepto") != null) {
            s.setAttribute(CLAUSULA, Boolean.TRUE);
        }
        if (!Boolean.TRUE.equals(s.getAttribute(CLAUSULA))) {
            mensaje(req, "warning", "Para cargar un documento debe aceptar la cláusula de tratamiento de datos personales.");
            redirigir(req, resp, "/revision" + (idTramite == null ? "" : "?tramite=" + idTramite));
            return;
        }
        if (idTramite == null || archivo == null || archivo.getSize() == 0) {
            mensaje(req, "warning", "Seleccione el trámite y el archivo que desea revisar.");
            redirigir(req, resp, "/revision" + (idTramite == null ? "" : "?tramite=" + idTramite));
            return;
        }
        String nombre = Path.of(archivo.getSubmittedFileName() == null ? "documento" : archivo.getSubmittedFileName())
                .getFileName().toString();
        byte[] contenido;
        try (InputStream in = archivo.getInputStream()) {
            contenido = in.readAllBytes();
        } finally {
            archivo.delete();
        }
        try {
            int id = servicio.revisar(idTramite, nombre, contenido, token(s));
            redirigir(req, resp, "/revision/reporte?id=" + id);
        } catch (ReglaNegocioException e) {
            mensaje(req, "danger", "Archivo rechazado: " + e.getMessage());
            redirigir(req, resp, "/revision?tramite=" + idTramite);
        }
    }

    /** Identificador aleatorio de la sesión al que se liga cada reporte. */
    private String token(HttpSession s) {
        String t = (String) s.getAttribute(TOKEN);
        if (t == null) {
            t = tokenAleatorio();
            s.setAttribute(TOKEN, t);
        }
        return t;
    }
}
