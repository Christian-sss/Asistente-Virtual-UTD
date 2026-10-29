package asistente.servicio;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

/**
 * Extrae el texto del documento con Apache PDFBox. Las imágenes (JPG/PNG) y los PDF escaneados no
 * tienen texto extraíble; el OCR con Tess4J es opcional y no está habilitado en este avance, por lo
 * que sus criterios de texto se clasifican como «No verificable».
 */
public class ExtractorTexto {

    public String extraer(byte[] contenido, String tipoMime) {
        if (!"application/pdf".equals(tipoMime)) {
            return "";
        }
        try (PDDocument d = Loader.loadPDF(contenido)) {
            return new PDFTextStripper().getText(d);
        } catch (IOException e) {
            return "";
        }
    }
}
