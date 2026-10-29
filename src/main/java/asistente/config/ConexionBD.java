package asistente.config;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pool de conexiones JDBC de Tomcat hacia PostgreSQL.
 */
public final class ConexionBD {

    private static DataSource dataSource;

    private ConexionBD() {
    }

    public static void iniciar() {
        PoolProperties p = new PoolProperties();
        p.setUrl(Configuracion.dbUrl());
        p.setDriverClassName("org.postgresql.Driver");
        p.setUsername(Configuracion.dbUsuario());
        p.setPassword(Configuracion.dbContrasena());
        p.setInitialSize(2);
        p.setMaxActive(30);
        p.setMinIdle(2);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setValidationInterval(30000);
        dataSource = new DataSource(p);
    }

    public static Connection obtener() throws SQLException {
        return dataSource.getConnection();
    }

    public static void cerrar() {
        if (dataSource != null) {
            dataSource.close(true);
        }
    }
}
