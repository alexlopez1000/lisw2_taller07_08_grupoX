package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.state.QuestionWorkflow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionStateTest {

    private QuestionWorkflow draft() { return new QuestionWorkflow(TestData.multipleChoice()); }

    private QuestionWorkflow inPendingReview() { QuestionWorkflow w = draft(); w.submitForReview(); return w; }

    private QuestionWorkflow inReview() { QuestionWorkflow w = inPendingReview(); w.startReview(); return w; }

    private QuestionWorkflow approved() { QuestionWorkflow w = inReview(); w.approve(); return w; }

    @Test @DisplayName("Ciclo de vida completo: Borrador → Pendiente → En revisión → Aprobada")
    void followsDraftPendingReviewReviewApprovedLifecycle() {
        QuestionWorkflow w = draft();
        assertEquals("Borrador", w.getStateName());
        assertTrue(w.canEdit());
        assertFalse(w.canPublish());

        w.submitForReview();
        assertEquals("Pendiente de revisión", w.getStateName());
        assertFalse(w.canEdit());

        w.startReview();
        assertEquals("En revisión", w.getStateName());

        w.approve();
        assertEquals("Aprobada", w.getStateName());
        assertTrue(w.canPublish());
        assertFalse(w.canEdit());
    }

    @Test @DisplayName("El revisor solicita correcciones y la pregunta vuelve a Borrador")
    void reviewerCanRequestCorrectionsAndReturnToDraft() {
        QuestionWorkflow w = inReview();
        w.requestCorrections();
        assertEquals("Borrador", w.getStateName());
        assertTrue(w.canEdit());
    }

    @Test @DisplayName("En Borrador se edita el enunciado y se conserva el id")
    void draftAllowsEditingStatement() {
        QuestionWorkflow w = draft();
        String id = w.getQuestion().getId();
        w.editStatement("Enunciado editado");
        assertEquals("Enunciado editado", w.getQuestion().getStatement());
        assertEquals(id, w.getQuestion().getId());
    }

    @Test @DisplayName("En Borrador se pueden reemplazar las opciones (validando 4 y 1 correcta)")
    void draftAllowsEditingOptions() {
        QuestionWorkflow w = draft();
        w.editOptions(List.of(new Option("W", false), new Option("X", false),
                new Option("Y", true), new Option("Z", false)));
        assertEquals("Y", w.getQuestion().getCorrectOption().getText());

        assertThrows(IllegalStateException.class, () -> w.editOptions(
                List.of(new Option("W", true), new Option("X", false))));
        assertEquals("Y", w.getQuestion().getCorrectOption().getText()); // sin cambios tras el error
    }

    @Test @DisplayName("Fuera de Borrador no se puede editar el enunciado ni las opciones")
    void editingIsBlockedOutsideDraft() {
        List<Option> options = List.of(new Option("A", true), new Option("B", false),
                new Option("C", false), new Option("D", false));
        for (QuestionWorkflow w : List.of(inPendingReview(), inReview(), approved())) {
            Question before = w.getQuestion();
            assertThrows(IllegalStateException.class, () -> w.editStatement("No permitido"));
            assertThrows(IllegalStateException.class, () -> w.editOptions(options));
            assertSame(before, w.getQuestion());
        }
    }

    @Test @DisplayName("Borrador solo admite enviar a revisión")
    void draftRejectsOtherTransitions() {
        QuestionWorkflow w = draft();
        assertThrows(IllegalStateException.class, w::startReview);
        assertThrows(IllegalStateException.class, w::approve);
        assertThrows(IllegalStateException.class, w::requestCorrections);
        assertEquals("Borrador", w.getStateName());
    }

    @Test @DisplayName("Pendiente de revisión solo admite iniciar la revisión")
    void pendingReviewRejectsOtherTransitions() {
        QuestionWorkflow w = inPendingReview();
        assertThrows(IllegalStateException.class, w::submitForReview);
        assertThrows(IllegalStateException.class, w::approve);
        assertThrows(IllegalStateException.class, w::requestCorrections);
        assertEquals("Pendiente de revisión", w.getStateName());
    }

    @Test @DisplayName("En revisión solo admite aprobar o solicitar correcciones")
    void reviewRejectsOtherTransitions() {
        QuestionWorkflow w = inReview();
        assertThrows(IllegalStateException.class, w::submitForReview);
        assertThrows(IllegalStateException.class, w::startReview);
        assertEquals("En revisión", w.getStateName());
    }

    @Test @DisplayName("Aprobada es un estado final: no admite ninguna transición")
    void approvedRejectsAllTransitions() {
        QuestionWorkflow w = approved();
        assertThrows(IllegalStateException.class, w::submitForReview);
        assertThrows(IllegalStateException.class, w::startReview);
        assertThrows(IllegalStateException.class, w::approve);
        assertThrows(IllegalStateException.class, w::requestCorrections);
        assertEquals("Aprobada", w.getStateName());
    }

    @Test @DisplayName("Solo las preguntas aprobadas son publicables")
    void onlyApprovedIsPublishable() {
        assertFalse(draft().canPublish());
        assertFalse(inPendingReview().canPublish());
        assertFalse(inReview().canPublish());
        assertTrue(approved().canPublish());
    }

    @Test @DisplayName("El mensaje de error indica la operación y el estado actual")
    void errorMessageNamesOperationAndState() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> inPendingReview().approve());
        assertTrue(e.getMessage().contains("aprobar"));
        assertTrue(e.getMessage().contains("Pendiente de revisión"));
    }
}
