package co.edu.unicauca.saberpro.state;

import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.List;
import java.util.Objects;

/**
 * Contexto del patrón State: asocia una Pregunta con su estado de ciclo de
 * vida y delega cada operación en el estado actual. Como Question es
 * inmutable, la única forma de cambiar su contenido es a través de
 * DraftState, que reemplaza la pregunta por una copia editada.
 */
public class QuestionWorkflow {

    private IQuestionState state;
    private Question question;

    public QuestionWorkflow(Question question) {
        this.question = Objects.requireNonNull(question, "La pregunta no puede ser null");
        this.state = new DraftState();
    }

    public Question getQuestion() { return question; }
    public IQuestionState getState() { return state; }
    public String getStateName() { return state.getName(); }
    public boolean canEdit() { return state.canEdit(); }
    public boolean canPublish() { return state.canPublish(); }

    public void editStatement(String newStatement) { state.editStatement(this, newStatement); }
    public void editOptions(List<Option> newOptions) { state.editOptions(this, newOptions); }

    public void submitForReview() { state.submitForReview(this); }
    public void startReview() { state.startReview(this); }
    public void approve() { state.approve(this); }
    public void requestCorrections() { state.requestCorrections(this); }

    // Solo los estados (mismo paquete) pueden cambiar el estado o el contenido.
    void transitionTo(IQuestionState next) { state = Objects.requireNonNull(next); }
    void replaceQuestion(Question edited) { question = Objects.requireNonNull(edited); }
}
