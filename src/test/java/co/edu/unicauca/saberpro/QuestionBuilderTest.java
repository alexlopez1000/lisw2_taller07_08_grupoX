package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.builder.IQuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionDirector;
import co.edu.unicauca.saberpro.domain.model.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionBuilderTest {

    private IQuestionBuilder withFourOptions(IQuestionBuilder b) {
        return b.withContext(TestData.context()).withStatement("Enunciado")
                .withOption("A", false).withOption("B", true).withOption("C", false).withOption("D", false);
    }

    @Test @DisplayName("Construye una pregunta con 4 opciones y 1 correcta")
    void buildsQuestionWithFourOptionsAndOneCorrectAnswer() {
        Question q = withFourOptions(new QuestionBuilder().newQuestion())
                .withJustification("Porque B").withClassification("Área", "Competencia").build();
        assertEquals(4, q.countOptions());
        assertEquals(1, q.countCorrectOptions());
        assertEquals("B", q.getCorrectOption().getText());
        assertEquals("Área", q.getArea());
        assertEquals("Competencia", q.getCompetence());
    }

    @Test @DisplayName("Rechaza una pregunta con 3 opciones")
    void rejectsQuestionWithThreeOptions() {
        IQuestionBuilder b = new QuestionBuilder().newQuestion().withContext(TestData.context())
                .withStatement("Enunciado").withOption("A", true).withOption("B", false).withOption("C", false);
        assertThrows(IllegalStateException.class, b::build);
    }

    @Test @DisplayName("Rechaza una pregunta con 2 respuestas correctas")
    void rejectsQuestionWithTwoCorrectAnswers() {
        IQuestionBuilder b = new QuestionBuilder().newQuestion().withContext(TestData.context())
                .withStatement("Enunciado").withOption("A", true).withOption("B", true)
                .withOption("C", false).withOption("D", false);
        assertThrows(IllegalStateException.class, b::build);
    }

    @Test @DisplayName("Rechaza una pregunta sin respuesta correcta")
    void rejectsQuestionWithoutCorrectAnswer() {
        IQuestionBuilder b = new QuestionBuilder().newQuestion().withContext(TestData.context())
                .withStatement("Enunciado").withOption("A", false).withOption("B", false)
                .withOption("C", false).withOption("D", false);
        assertThrows(IllegalStateException.class, b::build);
    }

    @Test @DisplayName("Rechaza una quinta opción en cuanto se agrega")
    void rejectsFifthOption() {
        IQuestionBuilder b = withFourOptions(new QuestionBuilder().newQuestion());
        assertThrows(IllegalStateException.class, () -> b.withOption("E", false));
    }

    @Test @DisplayName("Rechaza preguntas sin contexto o con enunciado vacío")
    void rejectsMissingContextOrBlankStatement() {
        IQuestionBuilder noContext = new QuestionBuilder().newQuestion().withStatement("Enunciado")
                .withOption("A", true).withOption("B", false).withOption("C", false).withOption("D", false);
        assertThrows(IllegalStateException.class, noContext::build);

        IQuestionBuilder blank = new QuestionBuilder().newQuestion().withContext(TestData.context())
                .withStatement("   ")
                .withOption("A", true).withOption("B", false).withOption("C", false).withOption("D", false);
        assertThrows(IllegalStateException.class, blank::build);
    }

    @Test @DisplayName("Rechaza opciones con texto nulo o vacío")
    void rejectsInvalidOptionText() {
        IQuestionBuilder b = new QuestionBuilder().newQuestion();
        assertThrows(IllegalArgumentException.class, () -> b.withOption(null, true));
        assertThrows(IllegalArgumentException.class, () -> b.withOption("  ", false));
    }

    @Test @DisplayName("Tras build() el builder se reinicia y no comparte estado con la pregunta")
    void builderIsResetAfterBuild() {
        IQuestionBuilder b = withFourOptions(new QuestionBuilder().newQuestion());
        Question first = b.build();

        b.withStatement("Otro enunciado");               // no debe alterar a 'first'
        assertEquals("Enunciado", first.getStatement());
        assertThrows(IllegalStateException.class, b::build); // faltan contexto y opciones
    }

    @Test @DisplayName("La pregunta construida es inmutable")
    void builtQuestionIsImmutable() {
        Question q = TestData.multipleChoice();
        assertThrows(UnsupportedOperationException.class, () -> q.getOptions().clear());
        assertEquals(4, q.countOptions());
    }

    @Test @DisplayName("withStatement crea una copia con el mismo id y no altera la original")
    void withStatementCreatesCopyKeepingId() {
        Question original = TestData.multipleChoice();
        Question edited = original.withStatement("Nuevo enunciado");
        assertEquals(original.getId(), edited.getId());
        assertEquals("Nuevo enunciado", edited.getStatement());
        assertEquals("¿Cuál es la correcta?", original.getStatement());
        assertNotSame(original, edited);
    }

    @Test @DisplayName("Director construye la secuencia completa")
    void directorBuildsCompleteQuestion() {
        QuestionDirector director = new QuestionDirector();
        director.setBuilder(new QuestionBuilder());
        Question q = director.constructCompleteQuestion(TestData.context(), "Enunciado",
                List.of("A", "B", "C", "D"), 2, "Justificación", "Área", "Competencia");
        assertEquals("C", q.getCorrectOption().getText());
        assertEquals("Justificación", q.getJustification());
    }

    @Test @DisplayName("Director construye la secuencia básica sin justificación")
    void directorBuildsBasicQuestion() {
        QuestionDirector director = new QuestionDirector();
        director.setBuilder(new QuestionBuilder());
        Question q = director.constructBasicQuestion(TestData.context(), "Enunciado",
                List.of("A", "B", "C", "D"), 0);
        assertNull(q.getJustification());
        assertEquals("A", q.getCorrectOption().getText());
    }

    @Test @DisplayName("Director exige un builder asignado")
    void directorRequiresBuilder() {
        QuestionDirector director = new QuestionDirector();
        assertThrows(IllegalStateException.class, () -> director.constructBasicQuestion(
                TestData.context(), "Enunciado", List.of("A", "B", "C", "D"), 0));
    }
}
