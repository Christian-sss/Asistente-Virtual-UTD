package asistente.modelo;

/**
 * Resultado de un criterio de la revisión: Cumple, Observado o No verificable.
 */
public class ResultadoCriterio {

    private int id;
    private int idRevision;
    private Integer idRegla;
    private String criterio;
    private String tipo;
    private String resultado;
    private String detalle;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdRevision() {
        return idRevision;
    }

    public void setIdRevision(int idRevision) {
        this.idRevision = idRevision;
    }

    public Integer getIdRegla() {
        return idRegla;
    }

    public void setIdRegla(Integer idRegla) {
        this.idRegla = idRegla;
    }

    public String getCriterio() {
        return criterio;
    }

    public void setCriterio(String criterio) {
        this.criterio = criterio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

}
