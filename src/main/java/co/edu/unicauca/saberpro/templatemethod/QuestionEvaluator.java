package co.edu.unicauca.saberpro.templatemethod;

import co.edu.unicauca.saberpro.decorator.QuestionComponent;
import co.edu.unicauca.saberpro.domain.model.Question;

/**
 * Template Method: fija el orden de los cuatro pasos de evaluación
 * (recibir respuesta, validar formato, calcular puntaje, generar
 * retroalimentación). Las subclases solo implementan los pasos variables.
 */
public abstract class QuestionEvaluator {

    /** Plantilla del algoritmo (final: las subclases no pueden alterar el orden). */
    public final EvaluationResult evaluate(Question question, String answer) {
        if (question == null) throw new IllegalArgumentException("La pregunta es obligatoria");
        String normalized = receiveAnswer(answer);
        validateFormat(normalized);
        double score = calculateScore(question, normalized);
        String feedback = generateFeedback(question, normalized, score);
        return new EvaluationResult(normalized, score, feedback);
    }

    /**
     * Evalúa una pregunta decorada: el puntaje base se pondera con el
     * multiplicador acumulado por los decoradores (p. ej. alta dificultad)
     * y se añade la explicación técnica, si la hay.
     */
    public final EvaluationResult evaluate(QuestionComponent component, String answer) {
        if (component == null) throw new IllegalArgumentException("La pregunta es obligatoria");
        EvaluationResult base = evaluate(component.getQuestion(), answer);
        String explanation = component.getTechnicalExplanation();
        String feedback = explanation.isBlank()
                ? base.getFeedback()
                : base.getFeedback() + " Explicación técnica: " + explanation;
        return new EvaluationResult(base.getAnswer(),
                base.getScore() * component.getScoreMultiplier(), feedback);
    }

    // Paso 1 (con implementación por defecto; las subclases pueden ajustarlo)
    protected String receiveAnswer(String answer) {
        if (answer == null) throw new IllegalArgumentException("La respuesta no puede ser null");
        return answer.trim();
    }

    // Pasos 2, 3 y 4: variables según el tipo de pregunta
    protected abstract void validateFormat(String answer);
    protected abstract double calculateScore(Question question, String answer);
    protected abstract String generateFeedback(Question question, String answer, double score);

    protected double maximumScore() { return 5.0; }
}
