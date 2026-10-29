package asistente.modelo;

/**
 * Categoría del catálogo de trámites.
 */
public class Categoria {

    private int id;
    private String nombre;
    private String descripcion;
    private String icono;
    private int totalTramites;

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public int getTotalTramites() {
        return totalTramites;
    }

    public void setTotalTramites(int totalTramites) {
        this.totalTramites = totalTramites;
    }

}
