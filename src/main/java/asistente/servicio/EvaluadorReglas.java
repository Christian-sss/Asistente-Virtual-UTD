package asistente.servicio;

import asistente.modelo.ReglaRevision;
import asistente.modelo.ResultadoCriterio;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Evalúa las reglas de revisión del trámite leídas en tiempo de ejecución (sin recompilar).
 * Ningún criterio se marca «Cumple» sin evidencia; lo que no puede evaluarse queda «No verificable».
 */
public class EvaluadorReglas {

    /** Texto mínimo para considerar que el documento tiene texto legible. */
    public static final int TEXTO_MINIMO = 40;

    public List<ResultadoCriterio> evaluar(List<ReglaRevision> reglas, String texto) {
        boolean legible = texto != null && texto.strip().length() >= TEXTO_MINIMO;
        List<ResultadoCriterio> resultados = new ArrayList<>();
        for (ReglaRevision regla : reglas) {
            ResultadoCriterio r = new ResultadoCriterio();
            r.setIdRegla(regla.getId());
            r.setCriterio(regla.getCriterio());
            r.setTipo(regla.getTipo());
            if ("CONFIRMACION_ASISTIDA".equals(regla.getTipo())) {
                r.setResultado("NO_VERIFICABLE");
                r.setDetalle("Pendiente de su confirmación: el sistema no verifica firmas, sellos ni anexos.");
            } else if (!legible) {
                r.setResultado("NO_VERIFICABLE");
                r.setDetalle("No se pudo leer el texto (imagen o documento escaneado). Verifíquelo manualmente.");
            } else {
                evaluarExpresion(regla, texto, r);
            }
            resultados.add(r);
        }
        return resultados;
    }

    private void evaluarExpresion(ReglaRevision regla, String texto, ResultadoCriterio r) {
        try {
            // UNICODE_CASE para que (?i) también reconozca tildes en mayúsculas (ACADÉMICOS).
            Matcher m = Pattern.compile(regla.getExpresion(), Pattern.UNICODE_CASE).matcher(texto);
            if (m.find()) {
                r.setResultado("CUMPLE");
                r.setDetalle("Se encontró: «" + enmascarar(m.group()) + "».");
            } else {
                r.setResultado("OBSERVADO");
                r.setDetalle(regla.getMensajeCorreccion());
            }
        } catch (PatternSyntaxException | NullPointerException e) {
            r.setResultado("NO_VERIFICABLE");
            r.setDetalle("La regla no tiene una expresión válida; informe al administrador de contenidos.");
        }
    }

    /** Oculta parte de los datos personales encontrados (DNI, correo) en el reporte. */
    static String enmascarar(String valor) {
        String v = valor.strip().replaceAll("\\s+", " ");
        if (v.length() > 60) {
            v = v.substring(0, 60) + "…";
        }
        if (v.matches("\\d{8}")) {
            return v.substring(0, 2) + "****" + v.substring(6);
        }
        int arroba = v.indexOf('@');
        if (arroba > 2) {
            return v.substring(0, 2) + "***" + v.substring(arroba);
        }
        return v;
    }

    /** Estado general: Observado si algún criterio lo está; No verificable si alguno no pudo evaluarse. */
    public static String estadoGeneral(List<ResultadoCriterio> resultados) {
        boolean noVerificable = false;
        for (ResultadoCriterio r : resultados) {
            if ("OBSERVADO".equals(r.getResultado())) {
                return "OBSERVADO";
            }
            noVerificable |= "NO_VERIFICABLE".equals(r.getResultado());
        }
        return noVerificable ? "NO_VERIFICABLE" : "CUMPLE";
    }
}
