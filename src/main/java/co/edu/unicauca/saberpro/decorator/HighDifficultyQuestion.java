package co.edu.unicauca.saberpro.decorator;

public class HighDifficultyQuestion extends QuestionDecorator {
    private final double multiplier;
    public HighDifficultyQuestion(QuestionComponent component) { this(component, 1.5); }
    public HighDifficultyQuestion(QuestionComponent component, double multiplier) {
        super(component);
        if (multiplier < 1.0) throw new IllegalArgumentException("El multiplicador de dificultad debe ser >= 1.0");
        this.multiplier = multiplier;
    }
    @Override public double getScoreMultiplier() { return component.getScoreMultiplier() * multiplier; }
    @Override public String getStatement() { return "[ALTA DIFICULTAD] " + component.getStatement(); }
}
