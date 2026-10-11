package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.model.Context;
import co.edu.unicauca.saberpro.domain.model.Question;

/** Datos de prueba compartidos por las pruebas unitarias. */
final class TestData {

    private TestData() { }

    static Context context() {
        return new Context("C1", "Contexto", "Texto de contexto");
    }

    /** Pregunta válida de selección múltiple; la opción correcta es "Dos" (letra B). */
    static Question multipleChoice() {
        return new QuestionBuilder().newQuestion().withContext(context())
                .withStatement("¿Cuál es la correcta?")
                .withOption("Uno", false).withOption("Dos", true)
                .withOption("Tres", false).withOption("Cuatro", false)
                .withJustification("Dos es correcta").withClassification("General", "Análisis").build();
    }

    /** Pregunta Verdadero/Falso (el Builder exige 4 opciones, dos son de relleno). */
    static Question trueFalse(String correctText) {
        boolean isTrue = correctText.equalsIgnoreCase("Verdadero");
        return new QuestionBuilder().newQuestion().withContext(context())
                .withStatement("El agua hierve a 100 °C al nivel del mar")
                .withOption("Verdadero", isTrue).withOption("Falso", !isTrue)
                .withOption("No aplica", false).withOption("No se sabe", false)
                .withJustification("A nivel del mar el punto de ebullición es 100 °C").build();
    }
}
