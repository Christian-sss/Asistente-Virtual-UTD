package asistente.modelo;

/**
 * Registro de una operación administrativa.
 */
public class BitacoraCambio {

    private long id;
    private String usuario;
    private String entidad;
    private String accion;
    private String valorAnterior;
    private String valorNuevo;
    private java.time.LocalDateTime fecha;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public java.time.LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(java.time.LocalDateTime fecha) {
        this.fecha = fecha;
    }


    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

}
