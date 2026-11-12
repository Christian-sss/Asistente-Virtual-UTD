package asistente.servicio;

import asistente.config.Configuracion;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extrae el texto del documento con Apache PDFBox. Las imágenes (JPG/PNG) y los PDF escaneados no
 * tienen texto extraíble: si el OCR opcional con Tess4J está habilitado (ocr.habilitado=true) se
 * reconoce su texto; si no lo está o Tesseract no está instalado, los criterios de texto quedan
 * «No verificable» (RN-07).
 */
public class ExtractorTexto {

    private static final Logger LOG = Logger.getLogger(ExtractorTexto.class.getName());
    private static final int DPI_OCR = 200;

    /** Texto extraído y si se obtuvo mediante OCR. */
    public record Extraccion(String texto, boolean ocr) {
    }

    public String extraer(byte[] contenido, String tipoMime) {
        return extraerConDetalle(contenido, tipoMime).texto();
    }

    public Extraccion extraerConDetalle(byte[] contenido, String tipoMime) {
        String texto = "";
        if ("application/pdf".equals(tipoMime)) {
            try (PDDocument d = Loader.loadPDF(contenido)) {
                texto = new PDFTextStripper().getText(d);
                if (texto.strip().length() < EvaluadorReglas.TEXTO_MINIMO && Configuracion.ocrHabilitado()) {
                    String ocr = ocrPdf(d);
                    if (ocr != null) {
                        return new Extraccion(ocr, true);
                    }
                }
            } catch (IOException e) {
                return new Extraccion("", false);
            }
            return new Extraccion(texto, false);
        }
        if (Configuracion.ocrHabilitado()) {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(contenido));
                if (img != null) {
                    String ocr = ocr(img);
                    if (ocr != null) {
                        return new Extraccion(ocr, true);
                    }
                }
            } catch (IOException e) {
                LOG.log(Level.FINE, "No se pudo leer la imagen para OCR", e);
            }
        }
        return new Extraccion("", false);
    }

    private String ocrPdf(PDDocument d) throws IOException {
        PDFRenderer r = new PDFRenderer(d);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.getNumberOfPages(); i++) {
            String t = ocr(r.renderImageWithDPI(i, DPI_OCR, ImageType.GRAY));
            if (t == null) {
                return null;
            }
            sb.append(t).append('\n');
        }
        return sb.toString();
    }

    /** OCR de una imagen; null si Tesseract no está disponible en el servidor. */
    private String ocr(BufferedImage img) {
        try {
            Tesseract t = new Tesseract();
            if (!Configuracion.ocrDatos().isBlank()) {
                t.setDatapath(Configuracion.ocrDatos());
            }
            t.setLanguage(Configuracion.ocrIdioma());
            return t.doOCR(img);
        } catch (Exception | LinkageError e) {
            LOG.log(Level.WARNING, "OCR no disponible: " + e.getMessage());
            return null;
        }
    }
}
