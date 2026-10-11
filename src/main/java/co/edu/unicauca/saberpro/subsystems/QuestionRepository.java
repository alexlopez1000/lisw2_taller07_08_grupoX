package co.edu.unicauca.saberpro.subsystems;

import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.state.QuestionWorkflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Subsistema: repositorio del Banco (persistencia en memoria simulada).
 * Guarda cada pregunta junto con su ciclo de vida (QuestionWorkflow).
 * La capa de presentación nunca lo manipula directamente: siempre opera
 * a través de la Fachada.
 */
public class QuestionRepository {

    private final Map<String, QuestionWorkflow> storage = new LinkedHashMap<>();

    /** Registra una pregunta nueva; inicia su ciclo de vida en Borrador. */
    public Question save(Question question) {
        storage.put(question.getId(), new QuestionWorkflow(question));
        return question;
    }

    public Optional<QuestionWorkflow> findWorkflowById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Question> findById(String id) {
        return findWorkflowById(id).map(QuestionWorkflow::getQuestion);
    }

    public List<Question> findAll() {
        List<Question> all = new ArrayList<>();
        storage.values().forEach(w -> all.add(w.getQuestion()));
        return all;
    }

    public List<QuestionWorkflow> findAllWorkflows() {
        return new ArrayList<>(storage.values());
    }

    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }

    public int count() {
        return storage.size();
    }
}
