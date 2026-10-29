package asistente.servicio;

import asistente.modelo.ArticuloNormativo;
import asistente.modelo.Paso;
import asistente.modelo.PuntoAtencion;
import asistente.modelo.Requisito;
import asistente.modelo.Tramite;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera la hoja de ruta del trámite en PDF con Apache PDFBox (RF-05).
 */
public class GeneradorHojaRutaPdf {

    public static final String AVISO_REFERENCIAL = "Esta hoja de ruta es referencial y no constituye admisión, numeración ni "
            + "evaluación de fondo del trámite. La recepción, el número de expediente y el seguimiento oficial los realiza "
            + "la Unidad de Trámite Documentario de la UNJBG.";

    private static final float MARGEN = 50;
    private static final float ANCHO = PDRectangle.A4.getWidth() - 2 * MARGEN;
    private static final Color AZUL = new Color(0, 59, 113);

    private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private PDDocument doc;
    private PDPageContentStream cs;
    private float y;

    public byte[] generar(Tramite t) throws IOException {
        try (PDDocument d = new PDDocument()) {
            doc = d;
            nuevaPagina();
            // Encabezado
            cs.setNonStrokingColor(AZUL);
            cs.addRect(0, PDRectangle.A4.getHeight() - 70, PDRectangle.A4.getWidth(), 70);
            cs.fill();
            cs.setNonStrokingColor(Color.WHITE);
            texto("UNIVERSIDAD NACIONAL JORGE BASADRE GROHMANN", negrita, 12, MARGEN, PDRectangle.A4.getHeight() - 30);
            texto("Asistente Virtual de Trámites Documentarios - Hoja de ruta", normal, 10, MARGEN, PDRectangle.A4.getHeight() - 48);
            cs.setNonStrokingColor(Color.BLACK);
            y = PDRectangle.A4.getHeight() - 100;

            parrafo(t.getNombre(), negrita, 15, AZUL);
            parrafo(t.getDescripcion(), normal, 10, Color.DARK_GRAY);
            y -= 4;
            dato("Código del procedimiento (TUPA)", t.getCodigoTupa());
            dato("Solicitud dirigida a", t.getDirigidoA());
            dato("Derecho de trámite", t.getPagoTexto());
            dato("Plazo referencial", t.getPlazoReferencial());
            dato("Calificación", t.getCalificacion());

            titulo("Pasos a seguir");
            for (Paso p : t.getPasos()) {
                parrafo(p.getOrden() + ". " + p.getDescripcion() + (p.isPagoPrevio() ? "  [PAGO PREVIO]" : ""), normal, 10, Color.BLACK);
            }

            titulo("Requisitos");
            for (Requisito r : t.getRequisitos()) {
                parrafo("- " + r.getDescripcion() + (r.isObligatorio() ? "" : " (según corresponda)"), normal, 10, Color.BLACK);
                for (ArticuloNormativo a : r.getArticulos()) {
                    parrafo("    Sustento: " + a.getNormativa() + ", " + a.getNumero(), normal, 8, Color.GRAY);
                }
            }

            PuntoAtencion p = t.getPunto();
            if (p != null) {
                titulo("Dónde presentar");
                parrafo(p.getNombre(), negrita, 10, Color.BLACK);
                parrafo(p.getSede() + " - " + p.getDireccion(), normal, 10, Color.BLACK);
                parrafo("Horario: " + p.getHorario(), normal, 10, Color.BLACK);
                if (p.getTelefono() != null) {
                    parrafo("Teléfono: " + p.getTelefono(), normal, 9, Color.DARK_GRAY);
                }
            }

            titulo("Importante");
            parrafo(AVISO_REFERENCIAL, negrita, 9, new Color(150, 60, 0));
            y -= 6;
            parrafo("Generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + ". Fuente: TUPA UNJBG, R.R. N.° 11021-2023-UNJBG.", normal, 8, Color.GRAY);
            cs.close();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private void nuevaPagina() throws IOException {
        if (cs != null) {
            cs.close();
        }
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        cs = new PDPageContentStream(doc, page);
        y = PDRectangle.A4.getHeight() - MARGEN;
    }

    private void titulo(String s) throws IOException {
        y -= 8;
        espacio(30);
        texto(s.toUpperCase(), negrita, 11, MARGEN, y);
        cs.setStrokingColor(AZUL);
        cs.moveTo(MARGEN, y - 4);
        cs.lineTo(MARGEN + ANCHO, y - 4);
        cs.stroke();
        y -= 20;
    }

    private void dato(String etiqueta, String valor) throws IOException {
        if (valor == null || valor.isBlank()) {
            return;
        }
        parrafo(etiqueta + ": " + valor, normal, 10, Color.BLACK);
    }

    private void parrafo(String s, PDType1Font f, float tam, Color color) throws IOException {
        if (s == null) {
            return;
        }
        cs.setNonStrokingColor(color);
        for (String linea : partir(limpiar(s), f, tam)) {
            espacio(tam + 4);
            texto(linea, f, tam, MARGEN, y);
            y -= tam + 4;
        }
        cs.setNonStrokingColor(Color.BLACK);
        y -= 3;
    }

    private void espacio(float necesario) throws IOException {
        if (y - necesario < MARGEN) {
            Color c = Color.BLACK;
            nuevaPagina();
            cs.setNonStrokingColor(c);
        }
    }

    private void texto(String s, PDType1Font f, float tam, float x, float yy) throws IOException {
        cs.beginText();
        cs.setFont(f, tam);
        cs.newLineAtOffset(x, yy);
        cs.showText(limpiar(s));
        cs.endText();
    }

    private List<String> partir(String s, PDType1Font f, float tam) throws IOException {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : s.split(" ")) {
            String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (f.getStringWidth(prueba) / 1000 * tam > ANCHO && !actual.isEmpty()) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (!actual.isEmpty()) {
            lineas.add(actual.toString());
        }
        return lineas;
    }

    /** Reemplaza caracteres que la fuente estándar (WinAnsi) no puede representar. */
    private String limpiar(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').toCharArray()) {
            b.append(c <= 0xFF || "–—‘’“”•…€".indexOf(c) >= 0 ? c : '-');
        }
        return b.toString();
    }
}
