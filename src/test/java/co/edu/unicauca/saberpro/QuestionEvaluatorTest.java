package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.decorator.BasicQuestion;
import co.edu.unicauca.saberpro.decorator.FeedbackQuestion;
import co.edu.unicauca.saberpro.decorator.HighDifficultyQuestion;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.templatemethod.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionEvaluatorTest {

    // ---- Template Method: orden fijo de los 4 pasos ----

    @Test @DisplayName("La plantilla ejecuta los 4 pasos en orden: recibir, validar, puntuar, retroalimentar")
    void templateRunsStepsInOrder() {
        List<String> calls = new ArrayList<>();
        QuestionEvaluator recorder = new QuestionEvaluator() {
            @Override protected String receiveAnswer(String a) { calls.add("recibir"); return super.receiveAnswer(a); }
            @Override protected void validateFormat(String a) { calls.add("validar"); }
            @Override protected double calculateScore(Question q, String a) { calls.add("puntuar"); return 1.0; }
            @Override protected String generateFeedback(Question q, String a, double s) { calls.add("retroalimentar"); return "ok"; }
        };
        recorder.evaluate(TestData.multipleChoice(), "x");
        assertEquals(List.of("recibir", "validar", "puntuar", "retroalimentar"), calls);
    }

    @Test @DisplayName("Si falla la validación de formato no se calcula el puntaje")
    void scoreIsNotComputedWhenFormatIsInvalid() {
        List<String> calls = new ArrayList<>();
        QuestionEvaluator evaluator = new QuestionEvaluator() {
            @Override protected void validateFormat(String a) { throw new IllegalArgumentException("formato"); }
            @Override protected double calculateScore(Question q, String a) { calls.add("puntuar"); return 1.0; }
            @Override protected String generateFeedback(Question q, String a, double s) { return ""; }
        };
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(TestData.multipleChoice(), "x"));
        assertTrue(calls.isEmpty());
    }

    @Test @DisplayName("Rechaza pregunta o respuesta nulas")
    void rejectsNullInputs() {
        MultipleChoiceEvaluator e = new MultipleChoiceEvaluator();
        assertThrows(IllegalArgumentException.class, () -> e.evaluate(TestData.multipleChoice(), null));
        assertThrows(IllegalArgumentException.class, () -> e.evaluate((Question) null, "A"));
    }

    // ---- Selección múltiple ----

    @Test @DisplayName("Selección múltiple: 5.0 con la letra correcta")
    void multipleChoiceGivesFiveForCorrectLetter() {
        EvaluationResult r = new MultipleChoiceEvaluator().evaluate(TestData.multipleChoice(), "B");
        assertEquals(5.0, r.getScore());
        assertTrue(r.getFeedback().contains("correcta"));
    }

    @Test @DisplayName("Selección múltiple: acepta el texto de la opción y la letra en minúscula")
    void multipleChoiceAcceptsTextAndLowercase() {
        MultipleChoiceEvaluator e = new MultipleChoiceEvaluator();
        assertEquals(5.0, e.evaluate(TestData.multipleChoice(), "dos").getScore());
        assertEquals(5.0, e.evaluate(TestData.multipleChoice(), " b ").getScore());
    }

    @Test @DisplayName("Selección múltiple: 0 con una opción incorrecta")
    void multipleChoiceGivesZeroForWrongOption() {
        EvaluationResult r = new MultipleChoiceEvaluator().evaluate(TestData.multipleChoice(), "A");
        assertEquals(0.0, r.getScore());
        assertTrue(r.getFeedback().startsWith("Respuesta incorrecta"));
    }

    @Test @DisplayName("Selección múltiple: rechaza respuestas desconocidas o vacías")
    void multipleChoiceRejectsUnknownOrBlankAnswer() {
        MultipleChoiceEvaluator e = new MultipleChoiceEvaluator();
        assertThrows(IllegalArgumentException.class, () -> e.evaluate(TestData.multipleChoice(), "Z"));
        assertThrows(IllegalArgumentException.class, () -> e.evaluate(TestData.multipleChoice(), "   "));
    }

    // ---- Verdadero/Falso ----

    @Test @DisplayName("Verdadero/Falso: puntúa la respuesta correcta")
    void trueFalseScoresCorrectAnswer() {
        TrueFalseEvaluator e = new TrueFalseEvaluator();
        assertEquals(5.0, e.evaluate(TestData.trueFalse("Verdadero"), "Verdadero").getScore());
        assertEquals(5.0, e.evaluate(TestData.trueFalse("Verdadero"), "true").getScore());
        assertEquals(5.0, e.evaluate(TestData.trueFalse("Falso"), "falso").getScore());
    }

    @Test @DisplayName("Verdadero/Falso: 0 con la respuesta contraria y explica con la justificación")
    void trueFalseGivesZeroForWrongAnswer() {
        EvaluationResult r = new TrueFalseEvaluator().evaluate(TestData.trueFalse("Verdadero"), "Falso");
        assertEquals(0.0, r.getScore());
        assertTrue(r.getFeedback().contains("100 °C"));
    }

    @Test @DisplayName("Verdadero/Falso: rechaza formatos distintos de Verdadero/Falso")
    void trueFalseRejectsInvalidFormat() {
        assertThrows(IllegalArgumentException.class,
                () -> new TrueFalseEvaluator().evaluate(TestData.trueFalse("Verdadero"), "tal vez"));
    }

    @Test @DisplayName("Verdadero/Falso: la opción correcta de la pregunta debe ser Verdadero o Falso")
    void trueFalseRequiresTrueOrFalseCorrectOption() {
        assertThrows(IllegalArgumentException.class,
                () -> new TrueFalseEvaluator().evaluate(TestData.multipleChoice(), "Verdadero"));
    }

    // ---- Caso de estudio ----

    @Test @DisplayName("Caso de estudio: puntaje completo con todas las palabras clave")
    void caseStudyAwardsFullScore() {
        EvaluationResult r = new CaseStudyEvaluator(List.of("agua", "energía"))
                .evaluate(TestData.multipleChoice(), "El agua produce energía");
        assertEquals(5.0, r.getScore());
    }

    @Test @DisplayName("Caso de estudio: puntaje proporcional a las palabras clave presentes")
    void caseStudyAwardsProportionalScore() {
        EvaluationResult r = new CaseStudyEvaluator(List.of("agua", "energía", "oxígeno", "luz"))
                .evaluate(TestData.multipleChoice(), "Solo menciono el AGUA y la luz");
        assertEquals(2.5, r.getScore());
        assertTrue(r.getFeedback().startsWith("Análisis parcial"));
    }

    @Test @DisplayName("Caso de estudio: exige palabras clave y respuesta no vacía")
    void caseStudyValidatesInputs() {
        assertThrows(IllegalArgumentException.class, () -> new CaseStudyEvaluator(List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new CaseStudyEvaluator(List.of("a")).evaluate(TestData.multipleChoice(), "  "));
    }

    // ---- Integración Template Method + Decorator ----

    @Test @DisplayName("Una pregunta de alta dificultad pondera el puntaje con el multiplicador")
    void highDifficultyWeightsTheScore() {
        MultipleChoiceEvaluator e = new MultipleChoiceEvaluator();
        HighDifficultyQuestion hard = new HighDifficultyQuestion(new BasicQuestion(TestData.multipleChoice()));
        assertEquals(7.5, e.evaluate(hard, "B").getScore());
        assertEquals(0.0, e.evaluate(hard, "A").getScore());
    }

    @Test @DisplayName("La evaluación de una pregunta decorada incluye la explicación técnica")
    void decoratedEvaluationIncludesTechnicalExplanation() {
        FeedbackQuestion q = new FeedbackQuestion(new BasicQuestion(TestData.multipleChoice()), "Detalle técnico");
        EvaluationResult r = new MultipleChoiceEvaluator().evaluate(q, "B");
        assertEquals(5.0, r.getScore());
        assertTrue(r.getFeedback().contains("Explicación técnica: Detalle técnico"));
    }
}
