package asistente.modelo;

/**
 * Regla que deriva una consulta a la dependencia competente según palabras clave (RF-10, RF-14, RN-13).
 */
public class ReglaDerivacion {

    private int id;
    private int idDependencia;
    private String dependencia;
    private String tema;
    private String palabrasClave;
    private String indicacion;
    private boolean activa;
    private java.time.LocalDate fechaUltimaVerificacion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdDependencia() {
        return idDependencia;
    }

    public void setIdDependencia(int idDependencia) {
        this.idDependencia = idDependencia;
    }

    public String getDependencia() {
        return dependencia;
    }

    public void setDependencia(String dependencia) {
        this.dependencia = dependencia;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getPalabrasClave() {
        return palabrasClave;
    }

    public void setPalabrasClave(String palabrasClave) {
        this.palabrasClave = palabrasClave;
    }

    public String getIndicacion() {
        return indicacion;
    }

    public void setIndicacion(String indicacion) {
        this.indicacion = indicacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public java.time.LocalDate getFechaUltimaVerificacion() {
        return fechaUltimaVerificacion;
    }

    public void setFechaUltimaVerificacion(java.time.LocalDate fechaUltimaVerificacion) {
        this.fechaUltimaVerificacion = fechaUltimaVerificacion;
    }

    /** RN-12: se marca para revisión si la última verificación supera un semestre académico. */
    public boolean isRevisionPendiente() {
        return fechaUltimaVerificacion != null && fechaUltimaVerificacion.isBefore(java.time.LocalDate.now().minusMonths(6));
    }
}
