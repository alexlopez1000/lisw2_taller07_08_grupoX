package co.edu.unicauca.saberpro.state;

/** Pendiente de revisión: bloquea modificaciones del autor; un revisor puede iniciar la revisión. */
public class PendingReviewState extends AbstractQuestionState {
    @Override public String getName() { return "Pendiente de revisión"; }
    @Override public void startReview(QuestionWorkflow w) { w.transitionTo(new ReviewState()); }
}
