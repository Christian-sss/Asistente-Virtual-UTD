package asistente.modelo;

/**
 * Norma, directiva o reglamento que sustenta requisitos.
 */
public class Normativa {

    private int id;
    private String tipo;
    private String numero;
    private String titulo;
    private String organoEmisor;
    private java.time.LocalDate fechaAprobacion;
    private boolean vigente;
    private String fuenteOficial;
    private java.time.LocalDate fechaUltimaVerificacion;
    private java.util.List<ArticuloNormativo> articulos = new java.util.ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getOrganoEmisor() {
        return organoEmisor;
    }

    public void setOrganoEmisor(String organoEmisor) {
        this.organoEmisor = organoEmisor;
    }

    public java.time.LocalDate getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(java.time.LocalDate fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
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

    public java.time.LocalDate getFechaUltimaVerificacion() {
        return fechaUltimaVerificacion;
    }

    public void setFechaUltimaVerificacion(java.time.LocalDate fechaUltimaVerificacion) {
        this.fechaUltimaVerificacion = fechaUltimaVerificacion;
    }

    public java.util.List<ArticuloNormativo> getArticulos() {
        return articulos;
    }

    public void setArticulos(java.util.List<ArticuloNormativo> articulos) {
        this.articulos = articulos;
    }


    /** Se marca para revisión si la última verificación supera un semestre académico. */
    public boolean isRevisionPendiente() {
        return fechaUltimaVerificacion != null && fechaUltimaVerificacion.isBefore(java.time.LocalDate.now().minusMonths(6));
    }

    public String getNombreCompleto() {
        return tipo + " " + numero;
    }

}
