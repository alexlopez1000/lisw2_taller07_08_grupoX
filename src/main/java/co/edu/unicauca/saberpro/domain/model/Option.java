package co.edu.unicauca.saberpro.domain.model;

/**
 * Opción de respuesta de una pregunta. Es inmutable: una pregunta posee
 * exactamente cuatro opciones y solo una es la correcta.
 */
public class Option {

    private final String text;
    private final boolean correct;

    public Option(String text, boolean correct) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("El texto de la opción no puede estar vacío");
        }
        this.text = text;
        this.correct = correct;
    }

    public String getText() { return text; }
    public boolean isCorrect() { return correct; }

    @Override
    public String toString() {
        return text + (correct ? " (correcta)" : "");
    }
}
