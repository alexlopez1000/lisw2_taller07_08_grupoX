package co.edu.unicauca.saberpro.templatemethod;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.Locale;
import java.util.Map;

/**
 * Verdadero/Falso: la opción correcta de la pregunta debe ser "Verdadero"
 * o "Falso". Nota: como el Builder exige 4 opciones, las otras dos opciones
 * de una pregunta V/F son de relleno.
 */
public class TrueFalseEvaluator extends QuestionEvaluator {

    private static final Map<String, Boolean> VALUES = Map.of(
            "true", true, "verdadero", true, "v", true,
            "false", false, "falso", false, "f", false);

    @Override
    protected void validateFormat(String answer) {
        if (parse(answer) == null) {
            throw new IllegalArgumentException("Use Verdadero/Falso (o true/false)");
        }
    }

    @Override
    protected double calculateScore(Question q, String answer) {
        Boolean expected = parse(q.getCorrectOption().getText());
        if (expected == null) {
            throw new IllegalArgumentException("La opción correcta debe ser Verdadero o Falso");
        }
        return expected.equals(parse(answer)) ? maximumScore() : 0.0;
    }

    @Override
    protected String generateFeedback(Question q, String answer, double score) {
        if (score > 0) {
            return "Correcto: la afirmación es " + answer.toLowerCase(Locale.ROOT) + ".";
        }
        return "Incorrecto. " + (q.getJustification() == null
                ? "Revise el concepto evaluado." : q.getJustification());
    }

    private static Boolean parse(String text) {
        return VALUES.get(text.trim().toLowerCase(Locale.ROOT));
    }
}
