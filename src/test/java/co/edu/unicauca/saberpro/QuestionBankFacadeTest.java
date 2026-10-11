package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.facade.QuestionBankFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class QuestionBankFacadeTest {

    private Question create(QuestionBankFacade bank, String statement) {
        return bank.createCompleteQuestion(TestData.context(), statement,
                List.of("A", "B", "C", "D"), 1, "Justificación", "Área", "Competencia");
    }

    @Test @DisplayName("Crea y consulta preguntas a través de la fachada")
    void createsAndFindsQuestions() {
        QuestionBankFacade bank = new QuestionBankFacade();
        Question q = create(bank, "Enunciado 1");
        assertEquals(1, bank.countQuestions());
        assertSame(q, bank.findQuestionById(q.getId()).orElseThrow());
        assertEquals(1, bank.listAllQuestions().size());
        assertEquals(Optional.empty(), bank.findQuestionById("no-existe"));
    }

    @Test @DisplayName("Rechaza crear una pregunta con 3 opciones y no la registra")
    void rejectsInvalidQuestionCreation() {
        QuestionBankFacade bank = new QuestionBankFacade();
        assertThrows(IllegalStateException.class, () -> bank.createCompleteQuestion(
                TestData.context(), "Enunciado", List.of("A", "B", "C"), 0, "J", "Á", "C"));
        assertEquals(0, bank.countQuestions());
    }

    @Test @DisplayName("Importa del proveedor externo solo los reactivos válidos")
    void importsExternalQuestions() {
        QuestionBankFacade bank = new QuestionBankFacade();
        assertEquals(2, bank.importExternalQuestions().size());
        assertEquals(2, bank.countQuestions());
    }

    @Test @DisplayName("Elimina preguntas por id")
    void removesQuestions() {
        QuestionBankFacade bank = new QuestionBankFacade();
        Question q = create(bank, "Enunciado");
        assertTrue(bank.removeQuestion(q.getId()));
        assertFalse(bank.removeQuestion(q.getId()));
        assertEquals(0, bank.countQuestions());
    }

    @Test @DisplayName("Las preguntas nuevas quedan en Borrador y no son publicables")
    void newQuestionsStartAsDraft() {
        QuestionBankFacade bank = new QuestionBankFacade();
        Question q = create(bank, "Enunciado");
        assertEquals("Borrador", bank.getStateOf(q.getId()));
        assertTrue(bank.listPublishableQuestions().isEmpty());
    }

    @Test @DisplayName("Ciclo de vida completo a través de la fachada")
    void lifecycleThroughFacade() {
        QuestionBankFacade bank = new QuestionBankFacade();
        Question q = create(bank, "Enunciado");
        String id = q.getId();

        bank.submitForReview(id);
        bank.startReview(id);
        bank.requestCorrections(id);
        assertEquals("Borrador", bank.getStateOf(id));

        bank.submitForReview(id);
        bank.startReview(id);
        bank.approve(id);
        assertEquals("Aprobada", bank.getStateOf(id));
        assertEquals(List.of(id), bank.listPublishableQuestions().stream().map(Question::getId).toList());
    }

    @Test @DisplayName("Edita enunciado y opciones solo en Borrador")
    void editingOnlyInDraft() {
        QuestionBankFacade bank = new QuestionBankFacade();
        String id = create(bank, "Original").getId();

        bank.editStatement(id, "Editado");
        bank.editOptions(id, List.of("W", "X", "Y", "Z"), 3);
        Question edited = bank.findQuestionById(id).orElseThrow();
        assertEquals("Editado", edited.getStatement());
        assertEquals("Z", edited.getCorrectOption().getText());

        bank.submitForReview(id);
        assertThrows(IllegalStateException.class, () -> bank.editStatement(id, "No permitido"));
        assertEquals("Editado", bank.findQuestionById(id).orElseThrow().getStatement());
    }

    @Test @DisplayName("Operar sobre un id inexistente lanza IllegalArgumentException")
    void unknownIdIsRejected() {
        QuestionBankFacade bank = new QuestionBankFacade();
        assertThrows(IllegalArgumentException.class, () -> bank.submitForReview("Q-nada"));
        assertThrows(IllegalArgumentException.class, () -> bank.getStateOf("Q-nada"));
    }

    @Test @DisplayName("El resumen incluye conteo por área y por estado")
    void summaryIncludesAreaAndState() {
        QuestionBankFacade bank = new QuestionBankFacade();
        String id = create(bank, "Uno").getId();
        create(bank, "Dos");
        bank.submitForReview(id);
        String summary = bank.getSummary();
        assertTrue(summary.contains("2 pregunta(s)"));
        assertTrue(summary.contains("Área: 2"));
        assertTrue(summary.contains("Borrador: 1"));
        assertTrue(summary.contains("Pendiente de revisión: 1"));
    }
}
