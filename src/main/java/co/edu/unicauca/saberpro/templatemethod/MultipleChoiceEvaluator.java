package co.edu.unicauca.saberpro.templatemethod;

import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.List;
import java.util.stream.IntStream;

/** Selección múltiple: la respuesta es la letra (A-D) o el texto de una opción. */
public class MultipleChoiceEvaluator extends QuestionEvaluator {

    @Override
    protected void validateFormat(String answer) {
        if (answer.isBlank()) {
            throw new IllegalArgumentException("Debe indicar una opción");
        }
    }

    @Override
    protected double calculateScore(Question q, String answer) {
        Option chosen = q.getOptions().get(resolveOptionIndex(q, answer));
        return chosen.isCorrect() ? maximumScore() : 0.0;
    }

    @Override
    protected String generateFeedback(Question q, String answer, double score) {
        String justification = q.getJustification() == null
                ? "Revise la justificación de la pregunta." : q.getJustification();
        return (score > 0 ? "Respuesta correcta. " : "Respuesta incorrecta. ") + justification;
    }

    /** Índice de la opción elegida, por letra o por texto. */
    private int resolveOptionIndex(Question q, String answer) {
        List<Option> options = q.getOptions();
        int byLetter = Character.toUpperCase(answer.charAt(0)) - 'A';
        if (answer.length() == 1 && byLetter >= 0 && byLetter < options.size()) {
            return byLetter;
        }
        return IntStream.range(0, options.size())
                .filter(i -> options.get(i).getText().equalsIgnoreCase(answer))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La respuesta debe coincidir con A-D o con el texto de una opción"));
    }
}
