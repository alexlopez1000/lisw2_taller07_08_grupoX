package co.edu.unicauca.saberpro.subsystems;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Subsistema: validador de reglas de negocio. Certifica la
 * integridad de una pregunta antes de ser registrada en el Banco,
 * sin importar su origen (Builder o importacion externa).
 */
public class QuestionValidator {

    /** Retorna la lista de violaciones detectadas (vacia = valida) */
    public List<String> validate(Question question) {
        List<String> errors = new ArrayList<>();
        if (question.getContext() == null) {
            errors.add("Falta el contexto de lectura.");
        }
        if (question.getStatement() == null || question.getStatement().isBlank()) {
            errors.add("Falta el enunciado.");
        }
        if (question.countOptions() != 4) {
            errors.add("Debe tener exactamente 4 opciones (actuales: "
                    + question.countOptions() + ").");
        }
        if (question.countCorrectOptions() != 1) {
            errors.add("Debe tener exactamente 1 respuesta correcta (actuales: "
                    + question.countCorrectOptions() + ").");
        }
        return errors;
    }

    public boolean isValid(Question question) {
        return validate(question).isEmpty();
    }
}
