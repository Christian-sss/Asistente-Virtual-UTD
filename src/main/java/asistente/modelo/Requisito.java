package asistente.modelo;

/**
 * Requisito de un trámite, sustentado en uno o más artículos normativos.
 */
public class Requisito {

    private int id;
    private int idTramite;
    private int orden;
    private String descripcion;
    private boolean obligatorio;
    private java.util.List<ArticuloNormativo> articulos = new java.util.ArrayList<>();

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

    public boolean isObligatorio() {
        return obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    public java.util.List<ArticuloNormativo> getArticulos() {
        return articulos;
    }

    public void setArticulos(java.util.List<ArticuloNormativo> articulos) {
        this.articulos = articulos;
    }

}
