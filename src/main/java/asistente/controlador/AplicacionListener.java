package asistente.controlador;

import asistente.config.ConexionBD;
import asistente.config.Configuracion;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Carga la configuración e inicia el pool de conexiones al desplegar la aplicación.
 */
public class AplicacionListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Configuracion.cargar(sce.getServletContext());
        ConexionBD.iniciar();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConexionBD.cerrar();
    }
}
