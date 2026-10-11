package co.edu.unicauca.saberpro.templatemethod;

import co.edu.unicauca.saberpro.domain.model.Question;
import java.util.List;
import java.util.Locale;

/** Evaluación sencilla de caso: puntaje completo si aparecen todas las palabras clave configuradas. */
public class CaseStudyEvaluator extends QuestionEvaluator {
    private final List<String> requiredKeywords;
    public CaseStudyEvaluator(List<String> requiredKeywords) {
        if (requiredKeywords == null || requiredKeywords.isEmpty()) throw new IllegalArgumentException("Configure al menos una palabra clave");
        this.requiredKeywords = List.copyOf(requiredKeywords);
    }
    protected void validateFormat(String answer) { if (answer.isBlank()) throw new IllegalArgumentException("La respuesta del caso no puede estar vacía"); }
    protected double calculateScore(Question q, String answer) {
        String normalized = answer.toLowerCase(Locale.ROOT);
        long matches = requiredKeywords.stream().filter(k -> normalized.contains(k.toLowerCase(Locale.ROOT))).count();
        return maximumScore() * matches / requiredKeywords.size();
    }
    protected String generateFeedback(Question q, String answer, double score) {
        if (score == maximumScore()) return "Análisis completo: se identificaron todos los elementos clave. " + (q.getJustification() == null ? "" : q.getJustification());
        return "Análisis parcial: puntaje proporcional a las palabras clave identificadas (" + score + "/" + maximumScore() + ").";
    }
}
