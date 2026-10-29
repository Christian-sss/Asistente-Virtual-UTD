package asistente.servicio;

import asistente.config.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manejo de conexiones y transacciones para los servicios.
 */
public abstract class ServicioBase {

    @FunctionalInterface
    protected interface Operacion<T> {
        T ejecutar(Connection cn) throws Exception;
    }

    /** Ejecuta una consulta de solo lectura. */
    protected <T> T consultar(Operacion<T> op) {
        try (Connection cn = ConexionBD.obtener()) {
            return op.ejecutar(cn);
        } catch (ReglaNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la base de datos: " + e.getMessage(), e);
        }
    }

    /** Ejecuta cambios en una sola transacción (el cambio y su bitácora se confirman juntos). */
    protected <T> T enTransaccion(Operacion<T> op) {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try {
                T r = op.ejecutar(cn);
                cn.commit();
                return r;
            } catch (Exception e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (ReglaNegocioException e) {
            throw e;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al guardar en la base de datos: " + e.getMessage(), e);
        } catch (Exception e) {
            throw e instanceof RuntimeException re ? re : new IllegalStateException(e.getMessage(), e);
        }
    }
}
