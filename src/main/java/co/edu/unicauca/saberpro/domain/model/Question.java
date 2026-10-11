package co.edu.unicauca.saberpro.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Producto complejo del Builder: Pregunta del Banco Saber PRO.
 *
 * Es inmutable: su contenido solo cambia creando una copia con el mismo id
 * (withStatement / withOptions), lo que permite que el patrón State controle
 * cuándo se puede editar sin que exista una vía para saltarse el control.
 * Los invariantes (contexto, enunciado, 4 opciones, 1 correcta) se verifican
 * siempre en el constructor.
 */
public class Question {

    public static final int REQUIRED_OPTIONS = 4;

    private final String id;
    private final Context context;
    private final String statement;
    private final List<Option> options;
    private final String justification;
    private final String area;
    private final String competence;

    public Question(Context context, String statement, List<Option> options,
                    String justification, String area, String competence) {
        this("Q-" + UUID.randomUUID().toString().substring(0, 8),
                context, statement, options, justification, area, competence);
    }

    private Question(String id, Context context, String statement, List<Option> options,
                     String justification, String area, String competence) {
        if (context == null) {
            throw new IllegalStateException("La pregunta requiere un Contexto asociado.");
        }
        if (statement == null || statement.isBlank()) {
            throw new IllegalStateException("La pregunta requiere un Enunciado no vacio.");
        }
        if (options == null || options.size() != REQUIRED_OPTIONS
                || options.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("La pregunta debe tener exactamente "
                    + REQUIRED_OPTIONS + " opciones.");
        }
        long correct = options.stream().filter(Option::isCorrect).count();
        if (correct != 1) {
            throw new IllegalStateException(
                    "La pregunta debe tener exactamente 1 respuesta correcta (actuales: " + correct + ").");
        }
        this.id = id;
        this.context = context;
        this.statement = statement;
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
        this.justification = justification;
        this.area = area;
        this.competence = competence;
    }

    public String getId() { return id; }
    public Context getContext() { return context; }
    public String getStatement() { return statement; }
    public List<Option> getOptions() { return options; }
    public String getJustification() { return justification; }
    public String getArea() { return area; }
    public String getCompetence() { return competence; }

    public int countOptions() { return options.size(); }

    public long countCorrectOptions() {
        return options.stream().filter(Option::isCorrect).count();
    }

    /** La única opción correcta (garantizada por los invariantes). */
    public Option getCorrectOption() {
        return options.stream().filter(Option::isCorrect).findFirst().orElseThrow();
    }

    /** Copia de la pregunta (mismo id) con otro enunciado. */
    public Question withStatement(String newStatement) {
        return new Question(id, context, newStatement, options, justification, area, competence);
    }

    /** Copia de la pregunta (mismo id) con otras opciones. */
    public Question withOptions(List<Option> newOptions) {
        return new Question(id, context, statement, Objects.requireNonNull(newOptions),
                justification, area, competence);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pregunta ").append(id)
          .append(" [").append(area == null ? "sin area" : area)
          .append(" / ").append(competence == null ? "sin competencia" : competence).append("]\n");
        sb.append("  Contexto     : ").append(context.getTitle()).append("\n");
        sb.append("  Enunciado    : ").append(statement).append("\n");
        char letter = 'A';
        for (Option option : options) {
            sb.append("     ").append(letter++).append(") ").append(option.getText());
            if (option.isCorrect()) {
                sb.append("   <-- CORRECTA");
            }
            sb.append("\n");
        }
        sb.append("  Justificacion: ")
          .append(justification == null ? "(sin justificacion)" : justification);
        return sb.toString();
    }
}
