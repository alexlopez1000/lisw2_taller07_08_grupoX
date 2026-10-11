package co.edu.unicauca.saberpro.adapter;

import co.edu.unicauca.saberpro.builder.IQuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.model.Context;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter: adapta el servicio externo (Adaptee), que retorna JSON crudo,
 * a la interfaz QuestionProvider que espera el Banco. Cada registro se
 * traduce a una Question usando el Builder, de modo que la importación
 * hereda todas las reglas de negocio del ensamblado. Un registro externo
 * defectuoso (opciones incorrectas, campos faltantes, JSON mal formado)
 * se descarta sin interrumpir la importación de los demás.
 */
public class ExternalQuestionAdapter implements QuestionProvider {

    private final ExternalQuestionService adaptee;

    public ExternalQuestionAdapter() {
        this(new ExternalQuestionService());
    }

    public ExternalQuestionAdapter(ExternalQuestionService adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public List<Question> provideQuestions() {
        List<Question> adapted = new ArrayList<>();
        for (String raw : adaptee.fetchQuestionsAsRawJson()) {
            try {
                adapted.add(adaptOne(raw));
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("[ADAPTER] Reactivo externo descartado: " + e.getMessage());
            }
        }
        return adapted;
    }

    /** Traduce un registro JSON crudo a una Question mediante Builder fluido. */
    private Question adaptOne(String json) {
        Context context = new Context(
                extractString(json, "contextoId"),
                extractString(json, "contextoTitulo"),
                extractString(json, "contextoTexto"));

        List<String> optionTexts = extractArray(json, "opciones");
        int correctIndex = extractInt(json, "correcta");

        IQuestionBuilder builder = new QuestionBuilder();
        builder.withContext(context).withStatement(extractString(json, "enunciado"));
        for (int i = 0; i < optionTexts.size(); i++) {
            builder.withOption(optionTexts.get(i), i == correctIndex);
        }
        return builder.withJustification(extractString(json, "justificacion"))
                      .withClassification(extractString(json, "area"), extractString(json, "competencia"))
                      .build();
    }

    // ---------- Mini-parser JSON (sin dependencias externas) ----------

    /** Texto leído y posición siguiente al cierre de la cadena. */
    private record Parsed(String value, int next) { }

    /** Posición del primer carácter del valor de la clave, o -1 si no existe. */
    private int valueStart(String json, String key) {
        String token = "\"" + key + "\"";
        int from = 0;
        while (true) {
            int at = json.indexOf(token, from);
            if (at == -1) {
                return -1;
            }
            int pos = skipSpaces(json, at + token.length());
            if (pos < json.length() && json.charAt(pos) == ':') {
                return skipSpaces(json, pos + 1);
            }
            from = at + token.length();
        }
    }

    private int skipSpaces(String json, int pos) {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    /** Lee una cadena JSON que empieza en pos (comilla de apertura), con escapes. */
    private Parsed readString(String json, int pos) {
        StringBuilder sb = new StringBuilder();
        int i = pos + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '"') {
                return new Parsed(sb.toString(), i + 1);
            }
            if (c == '\\' && i + 1 < json.length()) {
                char e = json.charAt(++i);
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (i + 4 >= json.length()) {
                            throw new IllegalArgumentException("Secuencia unicode incompleta en el JSON");
                        }
                        sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    default -> sb.append(e); // \" \\ \/
                }
            } else {
                sb.append(c);
            }
            i++;
        }
        throw new IllegalArgumentException("Cadena JSON sin cerrar");
    }

    /** Valor textual de una clave: "clave":"valor" (null si falta o no es texto). */
    private String extractString(String json, String key) {
        int start = valueStart(json, key);
        if (start == -1 || start >= json.length() || json.charAt(start) != '"') {
            return null;
        }
        return readString(json, start).value();
    }

    /** Valor numérico de una clave: "clave":1 o "clave":"1" (-1 si falta o no es número). */
    private int extractInt(String json, String key) {
        int start = valueStart(json, key);
        if (start == -1) {
            return -1;
        }
        if (start < json.length() && json.charAt(start) == '"') {
            start++;
        }
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return end == start ? -1 : Integer.parseInt(json.substring(start, end));
    }

    /** Arreglo de textos de una clave: "clave":["a","b",...] (vacío si falta). */
    private List<String> extractArray(String json, String key) {
        List<String> values = new ArrayList<>();
        int pos = valueStart(json, key);
        if (pos == -1 || pos >= json.length() || json.charAt(pos) != '[') {
            return values;
        }
        pos++;
        while (pos < json.length() && json.charAt(pos) != ']') {
            if (json.charAt(pos) == '"') {
                Parsed parsed = readString(json, pos);
                values.add(parsed.value());
                pos = parsed.next();
            } else {
                pos++;
            }
        }
        return values;
    }
}
