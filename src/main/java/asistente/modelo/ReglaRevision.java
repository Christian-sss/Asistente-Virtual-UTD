package asistente.modelo;

/**
 * Regla parametrizada que evalúa un criterio formal del documento.
 */
public class ReglaRevision {

    private int id;
    private int idTramite;
    private String criterio;
    private String tipo;
    private String expresion;
    private String mensajeCorreccion;
    private boolean activa;

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

    public String getExpresion() {
        return expresion;
    }

    public void setExpresion(String expresion) {
        this.expresion = expresion;
    }

    public String getMensajeCorreccion() {
        return mensajeCorreccion;
    }

    public void setMensajeCorreccion(String mensajeCorreccion) {
        this.mensajeCorreccion = mensajeCorreccion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

}
