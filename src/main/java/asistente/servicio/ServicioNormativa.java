package asistente.servicio;

import asistente.dao.NormativaDAO;
import asistente.modelo.Normativa;

import java.util.List;

/**
 * Consulta de normativa y sustento normativo de los requisitos (RF-08).
 */
public class ServicioNormativa extends ServicioBase {

    private final NormativaDAO dao = new NormativaDAO();

    public List<Normativa> buscar(String texto, Integer idTramite) {
        return consultar(cn -> dao.buscar(cn, texto, idTramite));
    }

    public Normativa detalle(int id) {
        return consultar(cn -> dao.buscarPorId(cn, id));
    }
}
