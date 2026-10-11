package co.edu.unicauca.saberpro.domain.model;

/**
 * Contexto de lectura asociado a una pregunta: texto, caso o situación
 * sobre el cual se formula el enunciado. Es inmutable.
 */
public class Context {

    private final String id;
    private final String title;
    private final String content;

    public Context(String id, String title, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("El contenido del contexto no puede estar vacío");
        }
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " :: " + content;
    }
}
