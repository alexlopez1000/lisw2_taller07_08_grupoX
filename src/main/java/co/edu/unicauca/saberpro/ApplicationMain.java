package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.decorator.BasicQuestion;
import co.edu.unicauca.saberpro.decorator.FeedbackQuestion;
import co.edu.unicauca.saberpro.decorator.HighDifficultyQuestion;
import co.edu.unicauca.saberpro.decorator.QuestionComponent;
import co.edu.unicauca.saberpro.domain.model.Context;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.facade.QuestionBankFacade;
import co.edu.unicauca.saberpro.templatemethod.EvaluationResult;
import co.edu.unicauca.saberpro.templatemethod.MultipleChoiceEvaluator;

import java.util.Arrays;
import java.util.List;

/**
 * Cliente de presentación: gestiona el Banco de Preguntas (creación,
 * importación, consulta y ciclo de vida) SOLO a través de la Fachada, que
 * oculta Builder, Director, Adapter, repositorio, servicio, validador y
 * State. Las demostraciones de evaluación (Template Method) y decoración
 * (Decorator) operan sobre preguntas ya obtenidas del Banco.
 */
public class ApplicationMain {

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("  BANCO DE PREGUNTAS SABER PRO - Taller 07-08");
        System.out.println("  Patrones: Builder + Adapter + Facade + State + Template Method + Decorator");
        System.out.println("=====================================================\n");

        QuestionBankFacade bank = new QuestionBankFacade();

        // ---------- 1. Creación de preguntas (Builder + Director ocultos) ----------
        Context condor = new Context("CTX-01", "Lectura: El condor andino",
                "El condor andino es una de las aves voladoras mas grandes del planeta. "
                + "Su poblacion ha disminuido principalmente por la fragmentacion de su "
                + "habitat y, en menor medida, por la caza ilegal.");

        Question q1 = bank.createCompleteQuestion(
                condor,
                "Segun el contexto, cual es la principal amenaza del condor andino?",
                Arrays.asList("La caza ilegal", "La fragmentacion del habitat",
                        "El cambio climatico", "La contaminacion del aire"),
                1,
                "El texto senala la fragmentacion del habitat como causa principal.",
                "Lectura Critica",
                "Interpretacion de textos");
        System.out.println(">> Pregunta registrada: " + q1.getId());

        Context energia = new Context("CTX-02", "Caso: Ahorro energetico",
                "Un edificio consume 1.200 kWh al mes y instala paneles solares "
                + "que cubren el 35% de su demanda.");

        Question q2 = bank.createCompleteQuestion(
                energia,
                "Cuanto consume el edificio de la red electrica, en kWh?",
                Arrays.asList("420", "640", "780", "860"),
                2,
                "1.200 x (1 - 0.35) = 1.200 x 0.65 = 780 kWh.",
                "Razonamiento Cuantitativo",
                "Resolucion de problemas cuantitativos");
        System.out.println(">> Pregunta registrada: " + q2.getId());

        // ---------- 2. Validación del Builder ----------
        System.out.println("\n>> Intento de registro con solo 3 opciones...");
        try {
            bank.createCompleteQuestion(
                    condor, "Pregunta de prueba invalida",
                    Arrays.asList("Opcion A", "Opcion B", "Opcion C"),
                    0, "Justificacion de prueba",
                    "Lectura Critica", "Interpretacion de textos");
        } catch (IllegalStateException e) {
            System.out.println("   [VALIDACION] " + e.getMessage());
        }

        // ---------- 3. Importación externa (Adapter oculto) ----------
        System.out.println("\n>> Importando reactivos del proveedor externo...");
        List<Question> imported = bank.importExternalQuestions();
        System.out.println(">> Reactivos externos incorporados: " + imported.size());

        // ---------- 4. Consultas simplificadas ----------
        System.out.println("\n>> Contenido actual del Banco:\n");
        for (Question q : bank.listAllQuestions()) {
            System.out.println(q);
            System.out.println("--------------------------------------------------");
        }
        System.out.println(bank.getSummary());

        // ---------- 5. State: ciclo de vida de una pregunta del Banco ----------
        System.out.println(">> Demostración State sobre la pregunta " + q1.getId() + "...");
        String id = q1.getId();
        System.out.println("Estado inicial: " + bank.getStateOf(id));
        bank.editStatement(id, "Segun el texto, cual es la principal causa de la disminucion del condor?");
        System.out.println("Enunciado editado en Borrador: "
                + bank.findQuestionById(id).orElseThrow().getStatement());

        bank.submitForReview(id);
        System.out.println("Después de enviar: " + bank.getStateOf(id));
        try {
            bank.editStatement(id, "Intento de edicion no permitido");
        } catch (IllegalStateException e) {
            System.out.println("   [BLOQUEADO] " + e.getMessage());
        }

        bank.startReview(id);
        bank.requestCorrections(id);
        System.out.println("El revisor solicita correcciones -> " + bank.getStateOf(id));

        bank.submitForReview(id);
        bank.startReview(id);
        bank.approve(id);
        System.out.println("Estado final: " + bank.getStateOf(id)
                + " | preguntas publicables: " + bank.listPublishableQuestions().size());
        System.out.println(bank.getSummary());

        // ---------- 6. Template Method: evaluación ----------
        System.out.println(">> Demostración Template Method...");
        Question approved = bank.findQuestionById(id).orElseThrow();
        EvaluationResult evaluation = new MultipleChoiceEvaluator().evaluate(approved, "B");
        System.out.println(evaluation);

        // ---------- 7. Decorator: atributos añadidos dinámicamente ----------
        System.out.println("\n>> Demostración Decorator...");
        QuestionComponent decorated = new FeedbackQuestion(
                new HighDifficultyQuestion(new BasicQuestion(approved)),
                "La respuesta requiere inferir la relación causal descrita en el texto.");
        System.out.println(decorated.getStatement());
        System.out.println("Multiplicador de puntaje: " + decorated.getScoreMultiplier());
        System.out.println(decorated.getFeedback());
        System.out.println("Evaluación ponderada: " + new MultipleChoiceEvaluator().evaluate(decorated, "B"));
    }
}
