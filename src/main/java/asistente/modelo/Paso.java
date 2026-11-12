package asistente.modelo;

/**
 * Paso ordenado de la hoja de ruta de un trámite.
 */
public class Paso implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int idTramite;
    private int orden;
    private String descripcion;
    private boolean pagoPrevio;
    private Integer idPunto;
    private PuntoAtencion punto;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(int idTramite) {
        this.idTramite = idTramite;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isPagoPrevio() {
        return pagoPrevio;
    }

    public void setPagoPrevio(boolean pagoPrevio) {
        this.pagoPrevio = pagoPrevio;
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
}
