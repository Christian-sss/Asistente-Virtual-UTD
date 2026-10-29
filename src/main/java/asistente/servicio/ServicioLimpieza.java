package asistente.servicio;

import asistente.dao.RevisionDocumentoDAO;
import asistente.modelo.RevisionDocumento;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Eliminación automática de los documentos y revisiones vencidos (RF-17).
 */
public class ServicioLimpieza extends ServicioBase {

    private final RevisionDocumentoDAO revisionDAO = new RevisionDocumentoDAO();
    private final AlmacenamientoTemporal almacenamiento = new AlmacenamientoTemporal();

    /** Elimina archivos y revisiones con fecha de expiración vencida; devuelve la cantidad eliminada. */
    public int eliminarVencidos(String origen) {
        return enTransaccion(cn -> {
            List<RevisionDocumento> vencidas = revisionDAO.buscarVencidas(cn, LocalDateTime.now());
            for (RevisionDocumento r : vencidas) {
                almacenamiento.eliminar(r.getNombreArchivoUuid());
                revisionDAO.eliminar(cn, r.getId());
            }
            revisionDAO.registrarLimpieza(cn, vencidas.size(), origen);
            return vencidas.size();
        });
    }

    public int documentosTemporales() {
        return consultar(revisionDAO::contarActivas);
    }

    public String ultimaEjecucion() {
        return consultar(revisionDAO::ultimaLimpieza);
    }
}
