package asistente.controlador;

import java.util.Collection;
import java.util.Map;

/**
 * Serialización JSON mínima para las respuestas que consume Leaflet.js (RF-09). Admite mapas,
 * colecciones, cadenas, números, booleanos y null.
 */
final class Json {

    private Json() {
    }

    static String de(Object v) {
        StringBuilder sb = new StringBuilder();
        escribir(sb, v);
        return sb.toString();
    }

    private static void escribir(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String s) {
            cadena(sb, s);
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Map<?, ?> m) {
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                cadena(sb, String.valueOf(e.getKey()));
                sb.append(':');
                escribir(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Collection<?> c) {
            sb.append('[');
            boolean primero = true;
            for (Object o : c) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                escribir(sb, o);
            }
            sb.append(']');
        } else {
            cadena(sb, v.toString());
        }
    }

    private static void cadena(StringBuilder sb, String s) {
        sb.append('"');
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                default -> {
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        sb.append('"');
    }
}
