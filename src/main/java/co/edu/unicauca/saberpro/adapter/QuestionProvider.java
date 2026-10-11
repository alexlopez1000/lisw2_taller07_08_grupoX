package co.edu.unicauca.saberpro.adapter;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.List;

/**
 * Target (interfaz objetivo): contrato que el Banco de Preguntas
 * Saber PRO acepta de cualquier proveedor de reactivos.
 * Los proveedores externos deben adaptarse a esta interfaz.
 */
public interface QuestionProvider {

    /** Obtiene preguntas listas para ser registradas en el Banco */
    List<Question> provideQuestions();
}
