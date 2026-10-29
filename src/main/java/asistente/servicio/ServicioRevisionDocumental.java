package asistente.servicio;

import asistente.config.Configuracion;
import asistente.dao.ReglaRevisionDAO;
import asistente.dao.RevisionDocumentoDAO;
import asistente.dao.TramiteDAO;
import asistente.modelo.ResultadoCriterio;
import asistente.modelo.RevisionDocumento;
import asistente.modelo.Tramite;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Revisión preliminar de documentos: carga (RF-06), revisión formal y reporte de conformidad (RF-07).
 */
public class ServicioRevisionDocumental extends ServicioBase {

    private final ValidadorArchivo validador = new ValidadorArchivo();
    private final AlmacenamientoTemporal almacenamiento = new AlmacenamientoTemporal();
    private final ExtractorTexto extractor = new ExtractorTexto();
    private final EvaluadorReglas evaluador = new EvaluadorReglas();
    private final ReglaRevisionDAO reglaDAO = new ReglaRevisionDAO();
    private final RevisionDocumentoDAO revisionDAO = new RevisionDocumentoDAO();
    private final TramiteDAO tramiteDAO = new TramiteDAO();

    /** Valida, guarda, revisa y registra el documento; devuelve el id de la revisión. */
    public int revisar(int idTramite, String nombreOriginal, byte[] contenido, String tokenSesion) {
        Tramite t = consultar(cn -> tramiteDAO.buscarPorId(cn, idTramite));
        if (t == null || !t.isPublicado()) {
            throw new ReglaNegocioException("Seleccione un trámite publicado.");
        }
        ValidadorArchivo.Validacion v = validador.validar(nombreOriginal, contenido);
        String uuid = almacenamiento.guardar(contenido);
        try {
            String texto = extractor.extraer(contenido, v.tipoMime());
            List<ResultadoCriterio> resultados = new ArrayList<>(v.criterios());
            boolean legible = texto.strip().length() >= EvaluadorReglas.TEXTO_MINIMO;
            resultados.add(ValidadorArchivo.basico("Texto legible", legible ? "CUMPLE" : "NO_VERIFICABLE",
                    legible ? "Se extrajo el texto del documento."
                            : "No hay texto extraíble (imagen o escaneado); el OCR no está habilitado en este avance."));
            resultados.addAll(evaluador.evaluar(consultar(cn -> reglaDAO.listarPorTramite(cn, idTramite, true)), texto));

            RevisionDocumento r = new RevisionDocumento();
            r.setIdTramite(idTramite);
            r.setTokenSesion(tokenSesion);
            r.setNombreOriginal(nombreOriginal.length() > 200 ? nombreOriginal.substring(0, 200) : nombreOriginal);
            r.setNombreArchivoUuid(uuid);
            r.setTipoMime(v.tipoMime());
            r.setTamanoBytes(contenido.length);
            r.setNumeroPaginas(v.paginas());
            r.setFechaCarga(LocalDateTime.now());
            r.setFechaExpiracion(r.getFechaCarga().plusHours(Configuracion.horasRetencion()));
            r.setEstado(EvaluadorReglas.estadoGeneral(resultados));
            return enTransaccion(cn -> {
                int id = revisionDAO.registrar(cn, r);
                revisionDAO.guardarResultados(cn, id, resultados);
                return id;
            });
        } catch (RuntimeException e) {
            almacenamiento.eliminar(uuid);
            throw e;
        }
    }

    /** Reporte de conformidad; solo para la sesión que cargó el documento. */
    public RevisionDocumento reporte(int idRevision, String tokenSesion) {
        return consultar(cn -> revisionDAO.buscar(cn, idRevision, tokenSesion));
    }

    /** Registra la confirmación asistida de firma, fotografías y anexos. */
    public void confirmar(int idRevision, String tokenSesion, Set<Integer> confirmados) {
        enTransaccion(cn -> {
            RevisionDocumento r = revisionDAO.buscar(cn, idRevision, tokenSesion);
            if (r == null) {
                throw new ReglaNegocioException("La revisión no existe o ya expiró.");
            }
            for (ResultadoCriterio c : r.getResultados()) {
                if (!"CONFIRMACION_ASISTIDA".equals(c.getTipo())) {
                    continue;
                }
                if (confirmados.contains(c.getId())) {
                    c.setResultado("CUMPLE");
                    c.setDetalle("Confirmado por el usuario.");
                } else {
                    c.setResultado("OBSERVADO");
                    c.setDetalle("No confirmado: " + mensajeRegla(cn, c));
                }
                revisionDAO.actualizarResultado(cn, c.getId(), idRevision, c.getResultado(), c.getDetalle());
            }
            revisionDAO.actualizarEstado(cn, idRevision, EvaluadorReglas.estadoGeneral(r.getResultados()));
            return null;
        });
    }

    private String mensajeRegla(java.sql.Connection cn, ResultadoCriterio c) throws java.sql.SQLException {
        String m = c.getIdRegla() == null ? null : reglaDAO.mensajeCorreccion(cn, c.getIdRegla());
        return m != null ? m : "verifique este elemento antes de presentar su expediente.";
    }
}
