package asistente.controlador;

import asistente.config.Configuracion;
import asistente.servicio.ServicioLimpieza;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tarea programada que elimina cada hora los documentos cargados cuyo plazo de 24 horas venció (RF-17).
 */
public class LimpiezaArchivosListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(LimpiezaArchivosListener.class.getName());
    private ScheduledExecutorService planificador;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServicioLimpieza servicio = new ServicioLimpieza();
        planificador = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "limpieza-archivos");
            t.setDaemon(true);
            return t;
        });
        int minutos = Configuracion.minutosLimpieza();
        planificador.scheduleAtFixedRate(() -> {
            try {
                int n = servicio.eliminarVencidos("PROGRAMADA");
                LOG.info("Limpieza de documentos vencidos: " + n + " eliminado(s).");
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "No se pudo ejecutar la limpieza de documentos", e);
            }
        }, 1, minutos, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (planificador != null) {
            planificador.shutdownNow();
        }
    }
}
