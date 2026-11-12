package asistente.servicio;

import asistente.dao.UbicacionDAO;
import asistente.modelo.Dependencia;
import asistente.modelo.ReglaDerivacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Derivación a la dependencia competente según las reglas de palabras clave (RF-10, RN-13). Solo
 * orienta: no consulta el sistema institucional ni asigna números de expediente (RN-16).
 */
public class ServicioDerivacion extends ServicioBase {

    private final UbicacionDAO dao = new UbicacionDAO();

    /** Dependencia sugerida con la regla que coincidió y las palabras encontradas. */
    public record Derivacion(Dependencia dependencia, ReglaDerivacion regla, List<String> coincidencias, int puntaje) {
    }

    /** Hasta tres dependencias ordenadas por coincidencia; lista vacía si ninguna regla aplica. */
    public List<Derivacion> derivar(String consulta) {
        String q = " " + ServicioOrientacion.normalizar(consulta).replaceAll("[^a-z0-9ñ ]", " ").replaceAll("\\s+", " ") + " ";
        if (q.isBlank()) {
            return List.of();
        }
        return consultar(cn -> {
            List<Derivacion> l = new ArrayList<>();
            List<Dependencia> dependencias = dao.listarDependencias(cn);
            for (ReglaDerivacion r : dao.listarReglas(cn, true)) {
                List<String> encontradas = new ArrayList<>();
                int puntaje = 0;
                for (String palabra : r.getPalabrasClave().split(",")) {
                    String p = ServicioOrientacion.normalizar(palabra).replaceAll("\\s+", " ");
                    if (!p.isEmpty() && q.contains(" " + p + " ")) {
                        encontradas.add(palabra.strip());
                        puntaje += p.split(" ").length;   // las frases de varias palabras pesan más
                    }
                }
                if (puntaje > 0) {
                    Dependencia d = dependencias.stream().filter(x -> x.getId() == r.getIdDependencia()).findFirst().orElse(null);
                    if (d != null && l.stream().noneMatch(x -> x.dependencia().getId() == d.getId())) {
                        l.add(new Derivacion(d, r, encontradas, puntaje));
                    } else if (d != null) {
                        // misma dependencia por otra regla: se conserva la de mayor puntaje
                        for (int i = 0; i < l.size(); i++) {
                            if (l.get(i).dependencia().getId() == d.getId() && l.get(i).puntaje() < puntaje) {
                                l.set(i, new Derivacion(d, r, encontradas, puntaje));
                            }
                        }
                    }
                }
            }
            l.sort(Comparator.comparingInt(Derivacion::puntaje).reversed());
            return l.size() > 3 ? l.subList(0, 3) : l;
        });
    }
}
