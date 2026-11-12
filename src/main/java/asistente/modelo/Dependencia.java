package asistente.modelo;

/**
 * Dependencia de la universidad con sus puntos de atención y canales digitales (RF-09, RF-14).
 */
public class Dependencia {

    private int id;
    private String nombre;
    private String sigla;
    private String funciones;
    private java.util.List<PuntoAtencion> puntos = new java.util.ArrayList<>();
    private java.util.List<CanalDigital> canales = new java.util.ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getFunciones() {
        return funciones;
    }

    public void setFunciones(String funciones) {
        this.funciones = funciones;
    }

    public java.util.List<PuntoAtencion> getPuntos() {
        return puntos;
    }

    public void setPuntos(java.util.List<PuntoAtencion> puntos) {
        this.puntos = puntos;
    }

    public java.util.List<CanalDigital> getCanales() {
        return canales;
    }

    public void setCanales(java.util.List<CanalDigital> canales) {
        this.canales = canales;
    }
    public boolean isUtd() {
        return "UTD".equalsIgnoreCase(sigla);
    }
}
