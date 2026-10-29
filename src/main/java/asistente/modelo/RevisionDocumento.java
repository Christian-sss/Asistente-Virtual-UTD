package asistente.modelo;

/**
 * Revisión preliminar temporal de un documento cargado.
 */
public class RevisionDocumento {

    private int id;
    private int idTramite;
    private String tramite;
    private String tokenSesion;
    private String nombreOriginal;
    private String nombreArchivoUuid;
    private String tipoMime;
    private long tamanoBytes;
    private Integer numeroPaginas;
    private java.time.LocalDateTime fechaCarga;
    private java.time.LocalDateTime fechaExpiracion;
    private String estado;
    private java.util.List<ResultadoCriterio> resultados = new java.util.ArrayList<>();

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

    public String getTramite() {
        return tramite;
    }

    public void setTramite(String tramite) {
        this.tramite = tramite;
    }

    public String getTokenSesion() {
        return tokenSesion;
    }

    public void setTokenSesion(String tokenSesion) {
        this.tokenSesion = tokenSesion;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getNombreArchivoUuid() {
        return nombreArchivoUuid;
    }

    public void setNombreArchivoUuid(String nombreArchivoUuid) {
        this.nombreArchivoUuid = nombreArchivoUuid;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public java.time.LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(java.time.LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public java.time.LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(java.time.LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public java.util.List<ResultadoCriterio> getResultados() {
        return resultados;
    }

    public void setResultados(java.util.List<ResultadoCriterio> resultados) {
        this.resultados = resultados;
    }


    public String getTamanoTexto() {
        return tamanoBytes >= 1024 * 1024
                ? String.format(java.util.Locale.US, "%.1f MB", tamanoBytes / 1048576.0)
                : String.format(java.util.Locale.US, "%.0f KB", tamanoBytes / 1024.0);
    }

    public String getFechaExpiracionTexto() {
        return fechaExpiracion == null ? "" : fechaExpiracion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

}
