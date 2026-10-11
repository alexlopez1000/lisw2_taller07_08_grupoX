package co.edu.unicauca.saberpro.decorator;

/** Decorador concreto: adjunta una explicación técnica detallada. */
public class FeedbackQuestion extends QuestionDecorator {
    private final String technicalExplanation;

    public FeedbackQuestion(QuestionComponent component, String technicalExplanation) {
        super(component);
        if (technicalExplanation == null || technicalExplanation.isBlank()) {
            throw new IllegalArgumentException("La explicación técnica es obligatoria");
        }
        this.technicalExplanation = technicalExplanation.trim();
    }

    @Override
    public String getFeedback() {
        String base = component.getFeedback();
        return base + (base.isBlank() ? "" : " ") + "Explicación técnica: " + technicalExplanation;
    }

    @Override
    public String getTechnicalExplanation() {
        String previous = component.getTechnicalExplanation();
        return previous.isBlank() ? technicalExplanation : previous + " " + technicalExplanation;
    }
}
