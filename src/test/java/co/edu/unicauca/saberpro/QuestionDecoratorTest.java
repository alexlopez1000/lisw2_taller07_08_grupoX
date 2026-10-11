package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.decorator.*;
import co.edu.unicauca.saberpro.domain.model.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionDecoratorTest {

    @Test @DisplayName("Decora con dificultad y retroalimentación técnica")
    void decoratesQuestionWithDifficultyAndTechnicalFeedback() {
        Question q = TestData.multipleChoice();
        QuestionComponent decorated = new FeedbackQuestion(
                new HighDifficultyQuestion(new BasicQuestion(q)), "Se evalúa inferencia y evidencia.");
        assertTrue(decorated.getStatement().startsWith("[ALTA DIFICULTAD]"));
        assertEquals(1.5, decorated.getScoreMultiplier());
        assertTrue(decorated.getFeedback().contains("Explicación técnica"));
    }

    @Test @DisplayName("BasicQuestion expone la pregunta sin modificarla")
    void basicQuestionDoesNotAlterTheQuestion() {
        Question q = TestData.multipleChoice();
        BasicQuestion basic = new BasicQuestion(q);
        assertEquals(q.getStatement(), basic.getStatement());
        assertEquals(1.0, basic.getScoreMultiplier());
        assertEquals("Dos es correcta", basic.getFeedback());
        assertEquals("", basic.getTechnicalExplanation());
        assertSame(q, basic.getQuestion());
    }

    @Test @DisplayName("Los decoradores no modifican la pregunta original")
    void decoratorsDoNotModifyTheBaseQuestion() {
        Question q = TestData.multipleChoice();
        String statementBefore = q.getStatement();
        new FeedbackQuestion(new HighDifficultyQuestion(new BasicQuestion(q)), "Explicación").getStatement();
        assertEquals(statementBefore, q.getStatement());
    }

    @Test @DisplayName("Los multiplicadores de decoradores apilados se acumulan")
    void stackedDifficultyMultipliersAccumulate() {
        QuestionComponent twice = new HighDifficultyQuestion(
                new HighDifficultyQuestion(new BasicQuestion(TestData.multipleChoice())), 2.0);
        assertEquals(3.0, twice.getScoreMultiplier());
    }

    @Test @DisplayName("El orden de apilamiento no cambia el multiplicador")
    void decoratorOrderDoesNotChangeMultiplier() {
        Question q = TestData.multipleChoice();
        QuestionComponent a = new FeedbackQuestion(new HighDifficultyQuestion(new BasicQuestion(q)), "x");
        QuestionComponent b = new HighDifficultyQuestion(new FeedbackQuestion(new BasicQuestion(q), "x"));
        assertEquals(a.getScoreMultiplier(), b.getScoreMultiplier());
    }

    @Test @DisplayName("Un decorador delega getQuestion() al componente base")
    void decoratorExposesBaseQuestion() {
        Question q = TestData.multipleChoice();
        QuestionComponent decorated = new FeedbackQuestion(new HighDifficultyQuestion(new BasicQuestion(q)), "x");
        assertSame(q, decorated.getQuestion());
    }

    @Test @DisplayName("Valida los argumentos de los decoradores")
    void validatesDecoratorArguments() {
        QuestionComponent basic = new BasicQuestion(TestData.multipleChoice());
        assertThrows(IllegalArgumentException.class, () -> new HighDifficultyQuestion(basic, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new FeedbackQuestion(basic, "  "));
        assertThrows(NullPointerException.class, () -> new HighDifficultyQuestion(null));
        assertThrows(NullPointerException.class, () -> new BasicQuestion(null));
    }
}
