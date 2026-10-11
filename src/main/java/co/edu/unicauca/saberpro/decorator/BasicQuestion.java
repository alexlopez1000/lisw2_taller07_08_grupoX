package co.edu.unicauca.saberpro.decorator;

import co.edu.unicauca.saberpro.domain.model.Question;
import java.util.Objects;

/** Componente concreto: la pregunta sin decorar. */
public class BasicQuestion implements QuestionComponent {
    private final Question question;

    public BasicQuestion(Question question) {
        this.question = Objects.requireNonNull(question, "La pregunta es obligatoria");
    }

    @Override public String getStatement() { return question.getStatement(); }
    @Override public double getScoreMultiplier() { return 1.0; }
    @Override public String getFeedback() { return question.getJustification() == null ? "" : question.getJustification(); }
    @Override public String getTechnicalExplanation() { return ""; }
    @Override public Question getQuestion() { return question; }
}
