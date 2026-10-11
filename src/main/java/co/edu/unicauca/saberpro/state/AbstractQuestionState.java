package co.edu.unicauca.saberpro.state;

import co.edu.unicauca.saberpro.domain.model.Option;

import java.util.List;

/**
 * Comportamiento por defecto: toda operación está prohibida. Cada estado
 * concreto sobrescribe únicamente las operaciones que SÍ permite, de modo
 * que las transiciones se resuelven por polimorfismo, sin if/else ni switch.
 */
public abstract class AbstractQuestionState implements IQuestionState {

    @Override public void submitForReview(QuestionWorkflow w) { throw denied("enviar a revisión"); }
    @Override public void startReview(QuestionWorkflow w) { throw denied("iniciar la revisión"); }
    @Override public void approve(QuestionWorkflow w) { throw denied("aprobar"); }
    @Override public void requestCorrections(QuestionWorkflow w) { throw denied("solicitar correcciones"); }
    @Override public void editStatement(QuestionWorkflow w, String s) { throw denied("editar el enunciado"); }
    @Override public void editOptions(QuestionWorkflow w, List<Option> o) { throw denied("editar las opciones"); }

    @Override public boolean canEdit() { return false; }
    @Override public boolean canPublish() { return false; }

    private IllegalStateException denied(String action) {
        return new IllegalStateException(
                "No se puede " + action + ": la pregunta está en estado " + getName() + ".");
    }
}
