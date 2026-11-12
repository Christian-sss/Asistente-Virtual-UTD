package asistente.modelo;

/**
 * Portal, mesa de partes virtual, correo o plataforma de una dependencia (RF-09, RF-14).
 */
public class CanalDigital {

    private int id;
    private int idDependencia;
    private String dependencia;
    private String tipo;
    private String nombre;
    private String valor;
    private String descripcion;
    private java.time.LocalDate fechaUltimaVerificacion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdDependencia() {
        return idDependencia;
    }

    public void setIdDependencia(int idDependencia) {
        this.idDependencia = idDependencia;
    }

    public String getDependencia() {
        return dependencia;
    }

    public void setDependencia(String dependencia) {
        this.dependencia = dependencia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public java.time.LocalDate getFechaUltimaVerificacion() {
        return fechaUltimaVerificacion;
    }

    public void setFechaUltimaVerificacion(java.time.LocalDate fechaUltimaVerificacion) {
        this.fechaUltimaVerificacion = fechaUltimaVerificacion;
    }

    /** RN-12: se marca para revisión si la última verificación supera un semestre académico. */
    public boolean isRevisionPendiente() {
        return fechaUltimaVerificacion != null && fechaUltimaVerificacion.isBefore(java.time.LocalDate.now().minusMonths(6));
    }

    public String getTipoTexto() {
        return switch (tipo == null ? "" : tipo) {
            case "PORTAL" -> "Portal web";
            case "MESA_PARTES_VIRTUAL" -> "Mesa de partes virtual";
            case "CORREO" -> "Correo electrónico";
            case "PLATAFORMA" -> "Plataforma";
            case "TELEFONO" -> "Teléfono";
            case "RED_SOCIAL" -> "Red social";
            default -> tipo;
        };
    }

    public String getIcono() {
        return switch (tipo == null ? "" : tipo) {
            case "PORTAL" -> "bi-globe";
            case "MESA_PARTES_VIRTUAL" -> "bi-inbox";
            case "CORREO" -> "bi-envelope";
            case "TELEFONO" -> "bi-telephone";
            case "RED_SOCIAL" -> "bi-share";
            default -> "bi-window";
        };
    }

    /** El valor es un enlace que puede abrirse. */
    public boolean isEnlace() {
        return valor != null && (valor.startsWith("https://") || valor.startsWith("http://"));
    }
}
