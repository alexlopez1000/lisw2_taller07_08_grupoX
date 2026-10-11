package co.edu.unicauca.saberpro.facade;

import co.edu.unicauca.saberpro.adapter.ExternalQuestionAdapter;
import co.edu.unicauca.saberpro.adapter.ExternalQuestionService;
import co.edu.unicauca.saberpro.adapter.QuestionProvider;
import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionDirector;
import co.edu.unicauca.saberpro.domain.model.Context;
import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.state.QuestionWorkflow;
import co.edu.unicauca.saberpro.subsystems.QuestionRepository;
import co.edu.unicauca.saberpro.subsystems.QuestionService;
import co.edu.unicauca.saberpro.subsystems.QuestionValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Facade: interfaz unificada y simplificada para la capa de presentación
 * (ApplicationMain / GUI). Oculta los subsistemas internos del Banco:
 * servicio, repositorio, validador, importador externo, núcleo creacional
 * (Director + Builder) y el ciclo de vida de las preguntas (State).
 */
public class QuestionBankFacade {

    private final QuestionService service;
    private final QuestionRepository repository;
    private final QuestionProvider externalImporter;
    private final QuestionDirector director;

    /** Ensambla internamente todos los subsistemas con el importador por defecto. */
    public QuestionBankFacade() {
        this(new ExternalQuestionAdapter(new ExternalQuestionService()));
    }

    /** Permite inyectar otro proveedor de preguntas (por ejemplo, en pruebas). */
    public QuestionBankFacade(QuestionProvider externalImporter) {
        this.repository = new QuestionRepository();
        this.service = new QuestionService(repository, new QuestionValidator());
        this.externalImporter = externalImporter;
        this.director = new QuestionDirector();
    }

    // ---------------- Creación e importación ----------------

    /** Crea y registra una pregunta completa (queda en estado Borrador). */
    public Question createCompleteQuestion(Context context, String statement,
            List<String> optionTexts, int correctIndex,
            String justification, String area, String competence) {
        director.setBuilder(new QuestionBuilder());
        Question question = director.constructCompleteQuestion(context, statement,
                optionTexts, correctIndex, justification, area, competence);
        return service.register(question);
    }

    /** Importa reactivos del proveedor externo; los inválidos se descartan. */
    public List<Question> importExternalQuestions() {
        List<Question> registered = new ArrayList<>();
        for (Question question : externalImporter.provideQuestions()) {
            try {
                registered.add(service.register(question));
            } catch (IllegalStateException e) {
                System.out.println("[FACADE] Importacion rechazada: " + e.getMessage());
            }
        }
        return registered;
    }

    // ---------------- Consultas ----------------

    public List<Question> listAllQuestions() { return service.findAll(); }
    public Optional<Question> findQuestionById(String id) { return service.findById(id); }
    public boolean removeQuestion(String id) { return service.remove(id); }
    public int countQuestions() { return service.count(); }

    /** Preguntas aprobadas, habilitadas para publicación en simulacros. */
    public List<Question> listPublishableQuestions() { return service.findPublishable(); }

    // ---------------- Ciclo de vida (State) ----------------

    public String getStateOf(String id) { return service.stateOf(id); }
    public void submitForReview(String id) { service.submitForReview(id); }
    public void startReview(String id) { service.startReview(id); }
    public void approve(String id) { service.approve(id); }
    public void requestCorrections(String id) { service.requestCorrections(id); }

    /** Edita el enunciado; solo permitido en Borrador. */
    public void editStatement(String id, String newStatement) {
        service.editStatement(id, newStatement);
    }

    /** Reemplaza las 4 opciones; solo permitido en Borrador. */
    public void editOptions(String id, List<String> optionTexts, int correctIndex) {
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            options.add(new Option(optionTexts.get(i), i == correctIndex));
        }
        service.editOptions(id, options);
    }

    // ---------------- Resumen ----------------

    /** Resumen estadístico por área y por estado del ciclo de vida. */
    public String getSummary() {
        Map<String, Long> byArea = repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        q -> q.getArea() == null ? "sin area" : q.getArea(),
                        TreeMap::new, Collectors.counting()));
        Map<String, Long> byState = repository.findAllWorkflows().stream()
                .collect(Collectors.groupingBy(
                        QuestionWorkflow::getStateName, TreeMap::new, Collectors.counting()));
        StringBuilder sb = new StringBuilder();
        sb.append("Resumen del Banco: ").append(repository.count())
          .append(" pregunta(s) registrada(s)\n");
        byArea.forEach((area, total) ->
                sb.append("  - ").append(area).append(": ").append(total).append("\n"));
        sb.append("Por estado:\n");
        byState.forEach((state, total) ->
                sb.append("  - ").append(state).append(": ").append(total).append("\n"));
        return sb.toString();
    }
}
