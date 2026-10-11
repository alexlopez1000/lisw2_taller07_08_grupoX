package co.edu.unicauca.saberpro.decorator;

import co.edu.unicauca.saberpro.domain.model.Question;

/** Componente del patrón Decorator: pregunta con atributos que pueden añadirse en tiempo de ejecución. */
public interface QuestionComponent {
    String getStatement();
    double getScoreMultiplier();
    String getFeedback();

    /** Explicación técnica acumulada por los decoradores (vacía si no hay). */
    String getTechnicalExplanation();

    /** Pregunta base envuelta, sobre la cual se evalúan las respuestas. */
    Question getQuestion();
}
