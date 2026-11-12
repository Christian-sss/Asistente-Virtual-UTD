package asistente.modelo;

/**
 * Oficina o ventanilla donde se presenta un trámite.
 */
public class PuntoAtencion implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String dependencia;
    private String nombre;
    private String sede;
    private String direccion;
    private String horario;
    private String telefono;
    private String correo;
    private Integer idDependencia;
    private String referencia;
    /** Ubicación real en el mapa (WGS84). */
    private java.math.BigDecimal latitud;
    private java.math.BigDecimal longitud;
    private java.time.LocalDate fechaUltimaVerificacion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDependencia() {
        return dependencia;
    }

    public void setDependencia(String dependencia) {
        this.dependencia = dependencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Integer getIdDependencia() {
        return idDependencia;
    }

    public void setIdDependencia(Integer idDependencia) {
        this.idDependencia = idDependencia;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public java.math.BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(java.math.BigDecimal latitud) {
        this.latitud = latitud;
    }

    public java.math.BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(java.math.BigDecimal longitud) {
        this.longitud = longitud;
    }

    public java.time.LocalDate getFechaUltimaVerificacion() {
        return fechaUltimaVerificacion;
    }

    public void setFechaUltimaVerificacion(java.time.LocalDate fechaUltimaVerificacion) {
        this.fechaUltimaVerificacion = fechaUltimaVerificacion;
    }

    public boolean isUbicado() {
        return latitud != null && longitud != null;
    }

    /** RN-12: se marca para revisión si la última verificación supera un semestre académico. */
    public boolean isRevisionPendiente() {
        return fechaUltimaVerificacion != null && fechaUltimaVerificacion.isBefore(java.time.LocalDate.now().minusMonths(6));
    }
}
