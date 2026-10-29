package asistente.servicio;

import asistente.config.Configuracion;
import asistente.modelo.ResultadoCriterio;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Valida extensión, tipo MIME real (Apache Tika), tamaño, páginas (PDFBox) y antivirus (ClamAV).
 * Rechaza el archivo en el primer criterio que falle.
 */
public class ValidadorArchivo {

    public static final long TAMANO_MAXIMO = 10L * 1024 * 1024;
    public static final int PAGINAS_MAXIMAS = 20;
    private static final Set<String> EXTENSIONES = Set.of("pdf", "jpg", "jpeg", "png");
    private static final Set<String> MIMES = Set.of("application/pdf", "image/jpeg", "image/png");

    private final Tika tika = new Tika();
    private final ClamAvCliente antivirus = new ClamAvCliente();

    /** Resultado de la validación: tipo MIME, páginas y criterios básicos aprobados. */
    public record Validacion(String tipoMime, Integer paginas, List<ResultadoCriterio> criterios) {
    }

    public Validacion validar(String nombre, byte[] contenido) {
        List<ResultadoCriterio> criterios = new ArrayList<>();
        String ext = nombre.contains(".") ? nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!EXTENSIONES.contains(ext)) {
            throw new ReglaNegocioException("Formato no permitido (." + ext + "). Solo se aceptan archivos PDF, JPG o PNG.");
        }
        if (contenido.length == 0) {
            throw new ReglaNegocioException("El archivo está vacío.");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new ReglaNegocioException("El archivo supera el tamaño máximo de 10 MB.");
        }
        String mime = tika.detect(contenido, nombre);
        if (!MIMES.contains(mime)) {
            throw new ReglaNegocioException("El contenido real del archivo (" + mime + ") no corresponde a un PDF, JPG o PNG.");
        }
        boolean pdf = "application/pdf".equals(mime);
        if (pdf != "pdf".equals(ext)) {
            throw new ReglaNegocioException("La extensión del archivo no coincide con su contenido real.");
        }
        criterios.add(basico("Formato permitido (PDF, JPG o PNG)", "CUMPLE", "Tipo real detectado: " + mime + "."));
        criterios.add(basico("Tamaño máximo de 10 MB", "CUMPLE", contenido.length >= 1048576
                ? String.format(Locale.US, "Tamaño: %.2f MB.", contenido.length / 1048576.0)
                : String.format(Locale.US, "Tamaño: %.0f KB.", Math.ceil(contenido.length / 1024.0))));

        Integer paginas = null;
        if (pdf) {
            try (PDDocument d = Loader.loadPDF(contenido)) {
                if (d.isEncrypted()) {
                    throw new ReglaNegocioException("El PDF está protegido con contraseña; no puede revisarse.");
                }
                paginas = d.getNumberOfPages();
            } catch (IOException e) {
                throw new ReglaNegocioException("El PDF está dañado o no se puede leer.");
            }
            if (paginas > PAGINAS_MAXIMAS) {
                throw new ReglaNegocioException("El PDF tiene " + paginas + " páginas; el máximo permitido es 20.");
            }
            criterios.add(basico("Máximo 20 páginas", "CUMPLE", "Páginas: " + paginas + "."));
        }

        ClamAvCliente.Resultado av = antivirus.analizar(contenido);
        if (av == ClamAvCliente.Resultado.INFECTADO) {
            throw new ReglaNegocioException("El antivirus detectó contenido malicioso. El archivo fue rechazado y eliminado.");
        }
        if (av == ClamAvCliente.Resultado.NO_DISPONIBLE) {
            if (Configuracion.clamavObligatorio()) {
                throw new ReglaNegocioException("El servicio antivirus no está disponible. Intente más tarde.");
            }
            criterios.add(basico("Análisis antivirus (ClamAV)", "NO_VERIFICABLE",
                    "El servicio antivirus no está disponible en este entorno de demostración."));
        } else {
            criterios.add(basico("Análisis antivirus (ClamAV)", "CUMPLE", "Sin amenazas detectadas."));
        }
        return new Validacion(mime, paginas, criterios);
    }

    static ResultadoCriterio basico(String criterio, String resultado, String detalle) {
        ResultadoCriterio r = new ResultadoCriterio();
        r.setCriterio(criterio);
        r.setTipo("BASICO");
        r.setResultado(resultado);
        r.setDetalle(detalle);
        return r;
    }
}
