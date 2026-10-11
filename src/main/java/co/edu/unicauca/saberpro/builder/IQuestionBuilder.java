package co.edu.unicauca.saberpro.builder;

import co.edu.unicauca.saberpro.domain.model.Context;
import co.edu.unicauca.saberpro.domain.model.Question;

/**
 * Abstract Builder: define los pasos de ensamblado de una Pregunta.
 * Cada metodo retorna el propio builder para permitir construccion
 * fluida paso a paso (method chaining), estilo del libro de curso.
 */
public interface IQuestionBuilder {

    /** Reinicia el ensamblado creando una pregunta vacia */
    IQuestionBuilder newQuestion();

    /** Paso 1: asocia el contexto de lectura */
    IQuestionBuilder withContext(Context context);

    /** Paso 2: define el enunciado de la pregunta */
    IQuestionBuilder withStatement(String statement);

    /** Paso 3: agrega una opcion de respuesta (se invoca 4 veces) */
    IQuestionBuilder withOption(String text, boolean correct);

    /** Paso 4: define la justificacion de la respuesta correcta */
    IQuestionBuilder withJustification(String justification);

    /** Paso 5: define la clasificacion (area y competencia) */
    IQuestionBuilder withClassification(String area, String competence);

    /**
     * Finaliza la construccion validando la regla de negocio:
     * exactamente 4 opciones y exactamente 1 respuesta correcta.
     * @throws IllegalStateException si la pregunta queda invalida
     */
    Question build();
}
