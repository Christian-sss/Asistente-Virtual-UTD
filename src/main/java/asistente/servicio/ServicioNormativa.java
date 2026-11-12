package asistente.servicio;

import asistente.dao.NormativaDAO;
import asistente.modelo.Normativa;

import java.util.List;

/**
 * Consulta de normativa y sustento normativo de los requisitos (RF-08): por palabra clave,
 * trámite y tipo de documento normativo.
 */
public class ServicioNormativa extends ServicioBase {

    private final NormativaDAO dao = new NormativaDAO();

    public List<Normativa> buscar(String texto, Integer idTramite) {
        return buscar(texto, idTramite, null);
    }

    public List<Normativa> buscar(String texto, Integer idTramite, String tipo) {
        return consultar(cn -> dao.buscar(cn, texto, idTramite, tipo));
    }

    public List<String> tipos() {
        return consultar(dao::listarTipos);
    }

    public Normativa detalle(int id) {
        return consultar(cn -> dao.buscarPorId(cn, id));
    }
}
