package asistente.config;

import jakarta.servlet.ServletContext;

import java.nio.file.Path;

/**
 * Parámetros del sistema. Se leen de META-INF/context.xml (o de la configuración
 * de Tomcat), de modo que la conexión y la ruta temporal no quedan en el código.
 */
public final class Configuracion {

    private static String dbUrl;
    private static String dbUsuario;
    private static String dbContrasena;
    private static Path directorioTemporal;
    private static int horasRetencion;
    private static int minutosLimpieza;
    private static String clamavHost;
    private static int clamavPuerto;
    private static boolean clamavObligatorio;

    private Configuracion() {
    }

    public static void cargar(ServletContext ctx) {
        dbUrl = valor(ctx, "db.url", "jdbc:postgresql://localhost:5432/asistente_utd");
        dbUsuario = valor(ctx, "db.usuario", "postgres");
        dbContrasena = valor(ctx, "db.contrasena", "postgres");
        String dir = valor(ctx, "archivos.directorio", "");
        directorioTemporal = dir.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "asistente-utd-revisiones")
                : Path.of(dir);
        horasRetencion = Integer.parseInt(valor(ctx, "archivos.horasRetencion", "24"));
        minutosLimpieza = Integer.parseInt(valor(ctx, "limpieza.intervaloMinutos", "60"));
        clamavHost = valor(ctx, "clamav.host", "127.0.0.1");
        clamavPuerto = Integer.parseInt(valor(ctx, "clamav.puerto", "3310"));
        clamavObligatorio = Boolean.parseBoolean(valor(ctx, "clamav.obligatorio", "false"));
    }

    private static String valor(ServletContext ctx, String nombre, String porDefecto) {
        String v = ctx.getInitParameter(nombre);
        return v == null ? porDefecto : v.trim();
    }

    public static String dbUrl() { return dbUrl; }
    public static String dbUsuario() { return dbUsuario; }
    public static String dbContrasena() { return dbContrasena; }
    public static Path directorioTemporal() { return directorioTemporal; }
    public static int horasRetencion() { return horasRetencion; }
    public static int minutosLimpieza() { return minutosLimpieza; }
    public static String clamavHost() { return clamavHost; }
    public static int clamavPuerto() { return clamavPuerto; }
    public static boolean clamavObligatorio() { return clamavObligatorio; }
}
