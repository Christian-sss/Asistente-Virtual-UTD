package asistente.modelo;

/**
 * Opción de una pregunta guiada; conduce a otra pregunta o a un trámite.
 */
public class OpcionRespuesta {

    private int id;
    private int idPregunta;
    private String texto;
    private Integer idPreguntaSiguiente;
    private Integer idTramite;
    private int orden;
    private String destino;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Integer getIdPreguntaSiguiente() {
        return idPreguntaSiguiente;
    }

    public void setIdPreguntaSiguiente(Integer idPreguntaSiguiente) {
        this.idPreguntaSiguiente = idPreguntaSiguiente;
    }

    public Integer getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(Integer idTramite) {
        this.idTramite = idTramite;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

}
