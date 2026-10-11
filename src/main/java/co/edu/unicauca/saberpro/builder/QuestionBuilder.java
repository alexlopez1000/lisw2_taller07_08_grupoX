package co.edu.unicauca.saberpro.builder;

import co.edu.unicauca.saberpro.domain.model.Context;
import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Builder: acumula los sub-objetos de la pregunta paso a paso y
 * solo en build() crea el producto, que es inmutable. Tras build() el
 * builder queda reiniciado, de modo que no comparte estado con la pregunta
 * ya entregada y puede reutilizarse.
 */
public class QuestionBuilder implements IQuestionBuilder {

    private Context context;
    private String statement;
    private final List<Option> options = new ArrayList<>();
    private String justification;
    private String area;
    private String competence;

    @Override
    public IQuestionBuilder newQuestion() {
        context = null;
        statement = null;
        options.clear();
        justification = null;
        area = null;
        competence = null;
        return this;
    }

    @Override
    public IQuestionBuilder withContext(Context context) {
        this.context = context;
        return this;
    }

    @Override
    public IQuestionBuilder withStatement(String statement) {
        this.statement = statement;
        return this;
    }

    @Override
    public IQuestionBuilder withOption(String text, boolean correct) {
        if (options.size() >= Question.REQUIRED_OPTIONS) {
            throw new IllegalStateException("La pregunta admite exactamente "
                    + Question.REQUIRED_OPTIONS + " opciones.");
        }
        options.add(new Option(text, correct));
        return this;
    }

    @Override
    public IQuestionBuilder withJustification(String justification) {
        this.justification = justification;
        return this;
    }

    @Override
    public IQuestionBuilder withClassification(String area, String competence) {
        this.area = area;
        this.competence = competence;
        return this;
    }

    @Override
    public Question build() {
        validate();
        Question question = new Question(context, statement, options, justification, area, competence);
        newQuestion();
        return question;
    }

    /**
     * Reglas de negocio del Banco Saber PRO: contexto obligatorio,
     * enunciado no vacío, exactamente 4 opciones y exactamente 1 correcta.
     */
    private void validate() {
        if (context == null) {
            throw new IllegalStateException("La pregunta requiere un Contexto asociado.");
        }
        if (statement == null || statement.isBlank()) {
            throw new IllegalStateException("La pregunta requiere un Enunciado no vacio.");
        }
        if (options.size() != Question.REQUIRED_OPTIONS) {
            throw new IllegalStateException(
                "La pregunta debe tener exactamente 4 opciones (actuales: " + options.size() + ").");
        }
        long correct = options.stream().filter(Option::isCorrect).count();
        if (correct != 1) {
            throw new IllegalStateException(
                "La pregunta debe tener exactamente 1 respuesta correcta (actuales: " + correct + ").");
        }
    }
}
