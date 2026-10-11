package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.adapter.ExternalQuestionAdapter;
import co.edu.unicauca.saberpro.adapter.ExternalQuestionService;
import co.edu.unicauca.saberpro.adapter.QuestionProvider;
import co.edu.unicauca.saberpro.domain.model.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExternalQuestionAdapterTest {

    /** Servicio externo falso que retorna los registros JSON indicados. */
    private static ExternalQuestionService serviceReturning(String... records) {
        return new ExternalQuestionService() {
            @Override public List<String> fetchQuestionsAsRawJson() { return List.of(records); }
        };
    }

    private static final String VALID = "{\"contextoId\":\"X1\",\"contextoTitulo\":\"Titulo\","
            + "\"contextoTexto\":\"Texto del contexto\",\"enunciado\":\"Enunciado externo\","
            + "\"opciones\":[\"a\",\"b\",\"c\",\"d\"],\"correcta\":2,"
            + "\"justificacion\":\"Porque c\",\"area\":\"Area X\",\"competencia\":\"Comp X\"}";

    @Test @DisplayName("El adapter implementa la interfaz QuestionProvider")
    void adapterImplementsTargetInterface() {
        QuestionProvider provider = new ExternalQuestionAdapter();
        assertNotNull(provider.provideQuestions());
    }

    @Test @DisplayName("Con el servicio por defecto importa 2 de 3 reactivos (descarta el de 3 opciones)")
    void importsValidRecordsAndDiscardsInvalidOnes() {
        List<Question> questions = new ExternalQuestionAdapter().provideQuestions();
        assertEquals(2, questions.size());
        for (Question q : questions) {
            assertEquals(4, q.countOptions());
            assertEquals(1, q.countCorrectOptions());
        }
    }

    @Test @DisplayName("Traduce todos los campos del JSON externo al modelo del Banco")
    void translatesAllFields() {
        Question q = new ExternalQuestionAdapter(serviceReturning(VALID)).provideQuestions().get(0);
        assertEquals("Enunciado externo", q.getStatement());
        assertEquals("Texto del contexto", q.getContext().getContent());
        assertEquals("X1", q.getContext().getId());
        assertEquals("c", q.getCorrectOption().getText());
        assertEquals("Porque c", q.getJustification());
        assertEquals("Area X", q.getArea());
        assertEquals("Comp X", q.getCompetence());
    }

    @Test @DisplayName("Tolera espacios en el JSON y el índice correcto como texto")
    void toleratesWhitespaceAndStringIndex() {
        String json = "{ \"contextoId\" : \"X2\", \"contextoTitulo\": \"T\", \"contextoTexto\": \"Texto\","
                + " \"enunciado\": \"E\", \"opciones\": [ \"a\", \"b\", \"c\", \"d\" ], \"correcta\": \"1\" }";
        Question q = new ExternalQuestionAdapter(serviceReturning(json)).provideQuestions().get(0);
        assertEquals("b", q.getCorrectOption().getText());
    }

    @Test @DisplayName("Interpreta comillas escapadas dentro de los textos")
    void handlesEscapedQuotes() {
        String json = VALID.replace("Enunciado externo", "Dijo \\\"hola\\\" al entrar");
        Question q = new ExternalQuestionAdapter(serviceReturning(json)).provideQuestions().get(0);
        assertEquals("Dijo \"hola\" al entrar", q.getStatement());
    }

    @Test @DisplayName("Un registro defectuoso no interrumpe la importación de los demás")
    void badRecordsDoNotAbortImport() {
        String badIndex = VALID.replace("\"correcta\":2", "\"correcta\":\"abc\"");   // sin número
        String noOptions = VALID.replace("\"opciones\":[\"a\",\"b\",\"c\",\"d\"],", "");
        String garbage = "esto no es json";
        String noContext = VALID.replace("\"contextoTexto\":\"Texto del contexto\",", "");
        List<Question> questions = new ExternalQuestionAdapter(
                serviceReturning(badIndex, noOptions, garbage, noContext, VALID)).provideQuestions();
        assertEquals(1, questions.size());
        assertEquals("Enunciado externo", questions.get(0).getStatement());
    }

    @Test @DisplayName("Sin registros externos devuelve una lista vacía")
    void emptyServiceReturnsEmptyList() {
        assertTrue(new ExternalQuestionAdapter(serviceReturning()).provideQuestions().isEmpty());
    }
}
