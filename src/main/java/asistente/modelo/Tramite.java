package asistente.modelo;

/**
 * Procedimiento del TUPA publicado en la base de conocimiento.
 */
public class Tramite {

    private int id;
    private int idCategoria;
    private String categoria;
    private Integer idPunto;
    private PuntoAtencion punto;
    private String codigo;
    private String codigoTupa;
    private String nombre;
    private String descripcion;
    private String dirigidoA;
    private java.math.BigDecimal derechoPago;
    private String detallePago;
    private String plazoReferencial;
    private String calificacion;
    private String estadoContenido;
    private java.time.LocalDate fechaUltimaVerificacion;
    private java.util.List<Paso> pasos = new java.util.ArrayList<>();
    private java.util.List<Requisito> requisitos = new java.util.ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getIdPunto() {
        return idPunto;
    }

    public void setIdPunto(Integer idPunto) {
        this.idPunto = idPunto;
    }

    public PuntoAtencion getPunto() {
        return punto;
    }

    public void setPunto(PuntoAtencion punto) {
        this.punto = punto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigoTupa() {
        return codigoTupa;
    }

    public void setCodigoTupa(String codigoTupa) {
        this.codigoTupa = codigoTupa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDirigidoA() {
        return dirigidoA;
    }

    public void setDirigidoA(String dirigidoA) {
        this.dirigidoA = dirigidoA;
    }

    public java.math.BigDecimal getDerechoPago() {
        return derechoPago;
    }

    public void setDerechoPago(java.math.BigDecimal derechoPago) {
        this.derechoPago = derechoPago;
    }

    public String getDetallePago() {
        return detallePago;
    }

    public void setDetallePago(String detallePago) {
        this.detallePago = detallePago;
    }

    public String getPlazoReferencial() {
        return plazoReferencial;
    }

    public void setPlazoReferencial(String plazoReferencial) {
        this.plazoReferencial = plazoReferencial;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(String calificacion) {
        this.calificacion = calificacion;
    }

    public String getEstadoContenido() {
        return estadoContenido;
    }

    public void setEstadoContenido(String estadoContenido) {
        this.estadoContenido = estadoContenido;
    }

    public java.time.LocalDate getFechaUltimaVerificacion() {
        return fechaUltimaVerificacion;
    }

    public void setFechaUltimaVerificacion(java.time.LocalDate fechaUltimaVerificacion) {
        this.fechaUltimaVerificacion = fechaUltimaVerificacion;
    }

    public java.util.List<Paso> getPasos() {
        return pasos;
    }

    public void setPasos(java.util.List<Paso> pasos) {
        this.pasos = pasos;
    }

    public java.util.List<Requisito> getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(java.util.List<Requisito> requisitos) {
        this.requisitos = requisitos;
    }


    /** Texto del derecho de pago para las vistas. */
    public String getPagoTexto() {
        if (detallePago != null && !detallePago.isBlank()) {
            return detallePago;
        }
        if (derechoPago == null || derechoPago.signum() == 0) {
            return "Gratuito";
        }
        return "S/ " + derechoPago.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    public boolean isPublicado() {
        return "PUBLICADO".equals(estadoContenido);
    }

}
