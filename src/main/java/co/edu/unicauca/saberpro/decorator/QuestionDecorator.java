package co.edu.unicauca.saberpro.decorator;

import co.edu.unicauca.saberpro.domain.model.Question;
import java.util.Objects;

/** Decorador base: implementa la misma interfaz y delega todo en el componente envuelto. */
public abstract class QuestionDecorator implements QuestionComponent {
    protected final QuestionComponent component;

    protected QuestionDecorator(QuestionComponent component) {
        this.component = Objects.requireNonNull(component, "El componente decorado es obligatorio");
    }

    @Override public String getStatement() { return component.getStatement(); }
    @Override public double getScoreMultiplier() { return component.getScoreMultiplier(); }
    @Override public String getFeedback() { return component.getFeedback(); }
    @Override public String getTechnicalExplanation() { return component.getTechnicalExplanation(); }
    @Override public Question getQuestion() { return component.getQuestion(); }
}
