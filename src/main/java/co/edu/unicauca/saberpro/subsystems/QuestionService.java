package co.edu.unicauca.saberpro.subsystems;

import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.state.QuestionWorkflow;

import java.util.List;
import java.util.Optional;

/**
 * Subsistema: servicio de negocio. Orquesta repositorio, validador y el
 * ciclo de vida (State) de las preguntas del Banco.
 */
public class QuestionService {

    private final QuestionRepository repository;
    private final QuestionValidator validator;

    public QuestionService(QuestionRepository repository, QuestionValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    /** Registra una pregunta (en estado Borrador) tras certificar su validez. */
    public Question register(Question question) {
        List<String> errors = validator.validate(question);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Pregunta invalida: " + String.join(" | ", errors));
        }
        return repository.save(question);
    }

    public Optional<Question> findById(String id) { return repository.findById(id); }
    public List<Question> findAll() { return repository.findAll(); }
    public boolean remove(String id) { return repository.deleteById(id); }
    public int count() { return repository.count(); }

    // ---------------- ciclo de vida (State) ----------------

    public void submitForReview(String id) { workflowOf(id).submitForReview(); }
    public void startReview(String id) { workflowOf(id).startReview(); }
    public void approve(String id) { workflowOf(id).approve(); }
    public void requestCorrections(String id) { workflowOf(id).requestCorrections(); }
    public void editStatement(String id, String statement) { workflowOf(id).editStatement(statement); }
    public void editOptions(String id, List<Option> options) { workflowOf(id).editOptions(options); }
    public String stateOf(String id) { return workflowOf(id).getStateName(); }

    /** Preguntas aprobadas, habilitadas para publicación en simulacros. */
    public List<Question> findPublishable() {
        return repository.findAllWorkflows().stream()
                .filter(QuestionWorkflow::canPublish)
                .map(QuestionWorkflow::getQuestion)
                .toList();
    }

    private QuestionWorkflow workflowOf(String id) {
        return repository.findWorkflowById(id).orElseThrow(
                () -> new IllegalArgumentException("No existe la pregunta con id " + id));
    }
}
