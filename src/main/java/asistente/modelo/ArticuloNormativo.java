package asistente.modelo;

/**
 * Artículo o disposición de una normativa.
 */
public class ArticuloNormativo {

    private int id;
    private int idNormativa;
    private String numero;
    private String contenido;
    private String normativa;
    private boolean vigente;
    private String fuenteOficial;
    private java.util.List<String> requisitosSustentados = new java.util.ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdNormativa() {
        return idNormativa;
    }

    public void setIdNormativa(int idNormativa) {
        this.idNormativa = idNormativa;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getNormativa() {
        return normativa;
    }

    public void setNormativa(String normativa) {
        this.normativa = normativa;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }

    public String getFuenteOficial() {
        return fuenteOficial;
    }

    public void setFuenteOficial(String fuenteOficial) {
        this.fuenteOficial = fuenteOficial;
    }

    public java.util.List<String> getRequisitosSustentados() {
        return requisitosSustentados;
    }

    public void setRequisitosSustentados(java.util.List<String> requisitosSustentados) {
        this.requisitosSustentados = requisitosSustentados;
    }

}
