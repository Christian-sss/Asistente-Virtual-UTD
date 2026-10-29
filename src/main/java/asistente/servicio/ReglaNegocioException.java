package asistente.servicio;

/**
 * Error esperado por incumplir una regla de negocio; su mensaje se muestra al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
