package asistente.modelo;

/**
 * Reporte anónimo de información inexacta sobre un trámite (RF-11).
 */
public class Retroalimentacion {

    private int id;
    private Integer idTramite;
    private String tramite;
    private String tipo;
    private String comentario;
    private boolean atendida;
    private java.time.LocalDateTime fecha;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(Integer idTramite) {
        this.idTramite = idTramite;
    }

    public String getTramite() {
        return tramite;
    }

    public void setTramite(String tramite) {
        this.tramite = tramite;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
    }

    public java.time.LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(java.time.LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getTipoTexto() {
        return switch (tipo == null ? "" : tipo) {
            case "REQUISITO" -> "Requisito";
            case "PAGO" -> "Monto o pago";
            case "PLAZO" -> "Plazo";
            case "UBICACION" -> "Ubicación u horario";
            case "NORMATIVA" -> "Normativa";
            default -> "Otro";
        };
    }
}
