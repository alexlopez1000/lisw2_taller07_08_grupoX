package co.edu.unicauca.saberpro.templatemethod;

public final class EvaluationResult {
    private final String answer;
    private final double score;
    private final String feedback;
    public EvaluationResult(String answer, double score, String feedback) {
        this.answer = answer; this.score = score; this.feedback = feedback;
    }
    public String getAnswer() { return answer; }
    public double getScore() { return score; }
    public String getFeedback() { return feedback; }
    @Override public String toString() { return "Puntaje: " + score + " | " + feedback; }
}
