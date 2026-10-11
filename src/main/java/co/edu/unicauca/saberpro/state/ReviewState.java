package co.edu.unicauca.saberpro.state;

/** En revisión (por pares): el revisor aprueba o solicita correcciones (vuelve a Borrador). */
public class ReviewState extends AbstractQuestionState {
    @Override public String getName() { return "En revisión"; }
    @Override public void approve(QuestionWorkflow w) { w.transitionTo(new ApprovedState()); }
    @Override public void requestCorrections(QuestionWorkflow w) { w.transitionTo(new DraftState()); }
}
