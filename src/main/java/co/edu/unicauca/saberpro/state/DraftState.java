package co.edu.unicauca.saberpro.state;

import co.edu.unicauca.saberpro.domain.model.Option;

import java.util.List;

/** Borrador: estado inicial. Permite edición completa y puede enviarse a revisión. */
public class DraftState extends AbstractQuestionState {
    @Override public String getName() { return "Borrador"; }
    @Override public void submitForReview(QuestionWorkflow w) { w.transitionTo(new PendingReviewState()); }
    @Override public void editStatement(QuestionWorkflow w, String s) { w.replaceQuestion(w.getQuestion().withStatement(s)); }
    @Override public void editOptions(QuestionWorkflow w, List<Option> o) { w.replaceQuestion(w.getQuestion().withOptions(o)); }
    @Override public boolean canEdit() { return true; }
}
