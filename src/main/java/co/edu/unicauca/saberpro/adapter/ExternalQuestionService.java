package co.edu.unicauca.saberpro.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptee (servicio externo): simula una plataforma de terceros que
 * expone preguntas en un formato PROPIO, distinto al del Banco.
 * Retorna cadenas crudas en JSON que el Banco no puede consumir
 * directamente.
 */
public class ExternalQuestionService {

    /**
     * Simula la consulta al sistema externo.
     * @return preguntas en JSON crudo (formato del proveedor externo)
     */
    public List<String> fetchQuestionsAsRawJson() {
        List<String> raw = new ArrayList<>();

        raw.add("{\"contextoId\":\"CTX-EXT-01\",\"contextoTitulo\":\"Lectura: La fotosintesis\","
                + "\"contextoTexto\":\"La fotosintesis es el proceso por el cual los organismos con clorofila "
                + "convierten la energia luminosa en energia quimica, liberando oxigeno como subproducto.\","
                + "\"enunciado\":\"Segun el contexto, cual es el subproducto de la fotosintesis?\","
                + "\"opciones\":[\"Dioxido de carbono\",\"Oxigeno\",\"Nitrogeno\",\"Metano\"],"
                + "\"correcta\":1,"
                + "\"justificacion\":\"El texto indica que el oxigeno se libera como subproducto del proceso.\","
                + "\"area\":\"Ciencias Naturales\",\"competencia\":\"Uso del conocimiento cientifico\"}");

        raw.add("{\"contextoId\":\"CTX-EXT-02\",\"contextoTitulo\":\"Caso: Demanda energetica\","
                + "\"contextoTexto\":\"Una fabrica consume 480 kWh al dia y decide reducir su consumo en 25%.\","
                + "\"enunciado\":\"Cual sera el nuevo consumo diario de la fabrica, en kWh?\","
                + "\"opciones\":[\"360\",\"380\",\"400\",\"420\"],"
                + "\"correcta\":0,"
                + "\"justificacion\":\"480 - (480 x 0.25) = 480 - 120 = 360 kWh.\","
                + "\"area\":\"Razonamiento Cuantitativo\",\"competencia\":\"Resolucion de problemas cuantitativos\"}");

        // Registro defectuoso: solo 3 opciones (el Builder lo rechazara en la importacion)
        raw.add("{\"contextoId\":\"CTX-EXT-03\",\"contextoTitulo\":\"Lectura: Biodiversidad\","
                + "\"contextoTexto\":\"Colombia es uno de los paises con mayor biodiversidad del planeta.\","
                + "\"enunciado\":\"Cual de las siguientes regiones concentra mayor biodiversidad?\","
                + "\"opciones\":[\"Amazonia\",\"Caribe\",\"Andina\"],"
                + "\"correcta\":0,"
                + "\"justificacion\":\"La Amazonia concentra la mayor biodiversidad.\","
                + "\"area\":\"Ciencias Naturales\",\"competencia\":\"Indagacion\"}");

        return raw;
    }
}
