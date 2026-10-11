package co.edu.unicauca.saberpro.state;

/** Aprobada: validada pedagógicamente y habilitada para publicación en simulacros. */
public class ApprovedState extends AbstractQuestionState {
    @Override public String getName() { return "Aprobada"; }
    @Override public boolean canPublish() { return true; }
}
