package asistente.servicio;

import asistente.modelo.Tramite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Copia de la versión publicada de un trámite (RN-11): mientras un cambio espera la validación del
 * personal de la UTD, el público sigue viendo esta versión. Solo admite clases del modelo y de Java.
 */
final class VersionPublicada {

    private static final ObjectInputFilter FILTRO = ObjectInputFilter.Config.createFilter(
            "asistente.modelo.*;java.util.*;java.lang.*;java.math.*;java.time.*;maxdepth=20;!*");

    private VersionPublicada() {
    }

    static byte[] serializar(Tramite t) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(b)) {
            out.writeObject(t);
            out.flush();
            return b.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la versión publicada del trámite.", e);
        }
    }

    /** Devuelve la versión guardada, o null si no existe o ya no es compatible con el modelo actual. */
    static Tramite leer(byte[] datos) {
        if (datos == null) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(datos))) {
            in.setObjectInputFilter(FILTRO);
            return (Tramite) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return null;
        }
    }
}
