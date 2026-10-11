package co.edu.unicauca.saberpro.builder;

import co.edu.unicauca.saberpro.domain.model.Context;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.List;

/**
 * Director: controla la creacion paso a paso definiendo las
 * secuencias canonicas de ensamblado. Solo cuando el Builder ha
 * terminado de construir el objeto, el Director lo recupera
 * (rol equivalente a "Cocina" en el ejemplo del libro).
 */
public class QuestionDirector {

    private IQuestionBuilder builder;

    public void setBuilder(IQuestionBuilder builder) {
        this.builder = builder;
    }

    public IQuestionBuilder getBuilder() {
        return builder;
    }

    /**
     * Secuencia completa: contexto -> enunciado -> 4 opciones ->
     * justificacion -> clasificacion -> build().
     */
    public Question constructCompleteQuestion(Context context, String statement,
            List<String> optionTexts, int correctIndex,
            String justification, String area, String competence) {
        prepare();
        builder.withContext(context)
               .withStatement(statement);
        addOptions(optionTexts, correctIndex);
        builder.withJustification(justification)
               .withClassification(area, competence);
        return builder.build();
    }

    /**
     * Secuencia basica: contexto + enunciado + opciones,
     * sin justificacion ni clasificacion.
     */
    public Question constructBasicQuestion(Context context, String statement,
            List<String> optionTexts, int correctIndex) {
        prepare();
        builder.withContext(context)
               .withStatement(statement);
        addOptions(optionTexts, correctIndex);
        return builder.build();
    }

    /** Verifica que haya builder asignado y reinicia el ensamblado */
    private void prepare() {
        if (builder == null) {
            throw new IllegalStateException(
                "Debe asignar un builder mediante setBuilder() antes de construir.");
        }
        builder.newQuestion();
    }

    /** Agrega las opciones marcando como correcta la del indice indicado */
    private void addOptions(List<String> optionTexts, int correctIndex) {
        for (int i = 0; i < optionTexts.size(); i++) {
            builder.withOption(optionTexts.get(i), i == correctIndex);
        }
    }
}
