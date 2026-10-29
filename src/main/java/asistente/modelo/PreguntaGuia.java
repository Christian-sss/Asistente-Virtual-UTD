package asistente.modelo;

/**
 * Pregunta cerrada del árbol de orientación.
 */
public class PreguntaGuia {

    private int id;
    private String texto;
    private boolean inicial;
    private java.util.List<OpcionRespuesta> opciones = new java.util.ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isInicial() {
        return inicial;
    }

    public void setInicial(boolean inicial) {
        this.inicial = inicial;
    }

    public java.util.List<OpcionRespuesta> getOpciones() {
        return opciones;
    }

    public void setOpciones(java.util.List<OpcionRespuesta> opciones) {
        this.opciones = opciones;
    }

}
