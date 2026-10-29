package asistente.servicio;

import asistente.config.Configuracion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Guarda los documentos fuera del directorio público, con nombre UUID, hasta su eliminación.
 */
public class AlmacenamientoTemporal {

    public String guardar(byte[] contenido) {
        try {
            Path dir = Configuracion.directorioTemporal();
            Files.createDirectories(dir);
            String uuid = UUID.randomUUID().toString();
            Files.write(dir.resolve(uuid), contenido);
            return uuid;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo temporal.", e);
        }
    }

    /** Elimina el archivo; devuelve true si existía. */
    public boolean eliminar(String uuid) {
        try {
            return Files.deleteIfExists(Configuracion.directorioTemporal().resolve(uuid));
        } catch (IOException e) {
            return false;
        }
    }
}
