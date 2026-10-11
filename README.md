# Banco de Preguntas SABER PRO — Taller 07-08

Proyecto académico de **Ingeniería de Software II** (Universidad del Cauca) que implementa un
banco de reactivos para la prueba estatal **SABER PRO**, demostrando la aplicación combinada de
**seis patrones de diseño** del catálogo GoF. La aplicación permite crear, importar, consultar,
evaluar y gestionar el ciclo de vida pedagógico de preguntas de selección múltiple.

---

## Tecnologías

| Elemento       | Versión / Detalle                          |
|----------------|--------------------------------------------|
| Java           | 17                                         |
| Maven          | 4.0.0 (`pom.xml`)                          |
| Pruebas        | JUnit Jupiter 5.10.2 + Surefire 3.2.5      |
| Dependencias   | Ninguna externa en runtime (solo JUnit)    |
| Empaquetado    | `jar`                                      |

---

## Arquitectura general

La capa de presentación ([`ApplicationMain`](src/main/java/co/edu/unicauca/saberpro/ApplicationMain.java))
opera **únicamente** a través de la fachada [`QuestionBankFacade`](src/main/java/co/edu/unicauca/saberpro/facade/QuestionBankFacade.java),
que oculta toda la complejidad interna:

```
┌──────────────────────────────────────────────────────────────────┐
│                    ApplicationMain (cliente)                      │
└──────────────────────────────┬───────────────────────────────────┘
                               │ usa
┌──────────────────────────────▼───────────────────────────────────┐
│                  QuestionBankFacade (Facade)                      │
├───────────────────────────────────────────────────────────────────┤
│  Builder + Director  │  Adapter  │  Service / Repository / Validator  │  State  │
└───────────────────────────────────────────────────────────────────┘
```

Además, la **evaluación** (Template Method) y la **decoración** (Decorator) operan sobre preguntas
ya obtenidas del banco, demostrando la composición de patrones.

---

## Patrones de diseño implementados

### 1. Builder — Construcción paso a paso de preguntas
Paquete [`builder`](src/main/java/co/edu/unicauca/saberpro/builder)

| Rol              | Clase                          |
|------------------|--------------------------------|
| Abstract Builder | `IQuestionBuilder`             |
| Concrete Builder | `QuestionBuilder`              |
| Director         | `QuestionDirector`             |
| Producto         | `domain.model.Question`        |

El Director define secuencias canónicas (`constructCompleteQuestion`, `constructBasicQuestion`)
y el Builder acumula sub-objetos hasta `build()`, donde se valida la regla de negocio
(exactamente 4 opciones y 1 respuesta correcta). El producto es **inmutable**.

### 2. Adapter — Importación de reactivos externos
Paquete [`adapter`](src/main/java/co/edu/unicauca/saberpro/adapter)

| Rol      | Clase                       |
|----------|-----------------------------|
| Target   | `QuestionProvider`          |
| Adapter  | `ExternalQuestionAdapter`   |
| Adaptee  | `ExternalQuestionService`   |

El servicio externo entrega reactivos en **JSON crudo** con un formato propio. El Adapter los
traduce a `Question` usando el Builder, heredando todas las validaciones. Los registros
defectuosos se descartan sin interrumpir la importación. Incluye un mini-parser JSON sin
dependencias externas.

### 3. Facade — Interfaz unificada del banco
Paquete [`facade`](src/main/java/co/edu/unicauca/saberpro/facade)

`QuestionBankFacade` es el único punto de acceso para la presentación. Ensambla internamente el
servicio, el repositorio, el validador, el importador externo, el Director+Builder y el ciclo de
vía (State). Expone operaciones simplificadas: crear, importar, listar, buscar, eliminar,
gestionar el ciclo de vida y obtener un resumen estadístico.

### 4. State — Ciclo de vida de la pregunta
Paquete [`state`](src/main/java/co/edu/unicauca/saberpro/state)

| Estado                   | ¿Edita? | ¿Publica? | Transiciones permitidas                        |
|--------------------------|---------|-----------|------------------------------------------------|
| `DraftState` (Borrador)  | Sí      | No        | → Pendiente de revisión                        |
| `PendingReviewState`     | No      | No        | → En revisión                                  |
| `ReviewState`            | No      | No        | → Aprobada **o** → Borrador (correcciones)     |
| `ApprovedState`          | No      | Sí        | — (terminal)                                   |

`QuestionWorkflow` es el **Context** que delega cada operación al estado actual.
`AbstractQuestionState` niega todas las operaciones por defecto; cada estado concreto
sobrescribe solo lo que permite, resolviendo las transiciones por **polimorfismo** (sin `if/else`).

### 5. Template Method — Evaluación de respuestas
Paquete [`templatemethod`](src/main/java/co/edu/unicauca/saberpro/templatemethod)

`QuestionEvaluator` fija el algoritmo `evaluate()` (método `final`) en cuatro pasos:
1. `receiveAnswer` (normaliza la respuesta, con implementación por defecto)
2. `validateFormat` *(abstracto)*
3. `calculateScore` *(abstracto)*
4. `generateFeedback` *(abstracto)*

Subclases concretas: `MultipleChoiceEvaluator`, `TrueFalseEvaluator`, `CaseStudyEvaluator`.
También soporta la evaluación de preguntas **decoradas**, ponderando el puntaje con el
multiplicador acumulado.

### 6. Decorator — Atributos dinámicos en tiempo de ejecución
Paquete [`decorator`](src/main/java/co/edu/unicauca/saberpro/decorator)

| Rol                | Clase                    | Aporta                                   |
|--------------------|--------------------------|------------------------------------------|
| Component          | `QuestionComponent`      | interfaz de la pregunta decorada         |
| Concrete Component | `BasicQuestion`          | la pregunta base (multiplicador = 1.0)   |
| Base Decorator     | `QuestionDecorator`      | delega todo al componente envuelto       |
| Decorator          | `HighDifficultyQuestion` | multiplica el puntaje (×1.5 por defecto) |
| Decorator          | `FeedbackQuestion`       | añade una explicación técnica            |

Los decoradores son **apilables**: `new FeedbackQuestion(new HighDifficultyQuestion(new BasicQuestion(q)), "...")`.

---

## Modelo de dominio

Paquete [`domain.model`](src/main/java/co/edu/unicauca/saberpro/domain/model)

| Clase      | Descripción                                                                 |
|------------|-----------------------------------------------------------------------------|
| `Question` | Reactivo inmutable: contexto, enunciado, 4 opciones, justificación, área y competencia. |
| `Option`   | Opción de respuesta inmutable con un texto y un flag de corrección.         |
| `Context`  | Texto o caso sobre el cual se formula el enunciado.                         |

La inmutabilidad de `Question` garantiza que el contenido solo cambie a través de `DraftState`
(mediante copias con el mismo id), cerrando cualquier vía para saltarse el control del ciclo de vida.

---

## Estructura del proyecto

```
src/
├── main/java/co/edu/unicauca/saberpro/
│   ├── ApplicationMain.java          ← cliente de demostración
│   ├── adapter/                      ← Adapter (importación externa)
│   ├── builder/                      ← Builder (creación de preguntas)
│   ├── decorator/                    ← Decorator (atributos dinámicos)
│   ├── domain/model/                 ← entidades del dominio
│   ├── facade/                       ← Facade (punto de acceso unificado)
│   ├── state/                        ← State (ciclo de vida)
│   ├── subsystems/                   ← Service, Repository, Validator
│   └── templatemethod/               ← Template Method (evaluación)
└── test/java/co/edu/unicauca/saberpro/
    ├── ExternalQuestionAdapterTest.java
    ├── QuestionBankFacadeTest.java
    ├── QuestionBuilderTest.java
    ├── QuestionDecoratorTest.java
    ├── QuestionEvaluatorTest.java
    ├── QuestionStateTest.java
    └── TestData.java
```

---

## Cómo compilar y ejecutar

> Requisito: JDK 17 y Maven disponibles en el `PATH`.

```powershell
# Compilar
mvn clean compile

# Ejecutar la demostración (cliente ApplicationMain)
mvn exec:java "-Dexec.mainClass=co.edu.unicauca.saberpro.ApplicationMain"

# Ejecutar las pruebas unitarias
mvn test

# Generar el paquete JAR (incluye compilación y tests)
mvn package
```

### Ejecución del JAR

```powershell
java -cp target/lisw2_taller07_08_grupoX-1.0.0.jar co.edu.unicauca.saberpro.ApplicationMain
```

---

## Pruebas unitarias

Se incluyen **6 clases de prueba** (JUnit 5) que cubren cada patrón:

| Archivo                           | Qué valida                                            |
|-----------------------------------|-------------------------------------------------------|
| `QuestionBuilderTest`             | Construcción válida, rechazo de opciones incorrectas  |
| `ExternalQuestionAdapterTest`     | Adaptación de JSON, descarte de registros defectuosos |
| `QuestionBankFacadeTest`          | Operaciones del banco y ciclo de vida                 |
| `QuestionStateTest`               | Transiciones y restricciones por estado               |
| `QuestionEvaluatorTest`           | Evaluación por tipo de pregunta (Template Method)     |
| `QuestionDecoratorTest`           | Apilado de decoradores y multiplicador de puntaje     |

---

## Ejemplo de salida

```
=====================================================
  BANCO DE PREGUNTAS SABER PRO - Taller 07-08
  Patrones: Builder + Adapter + Facade + State + Template Method + Decorator
=====================================================

>> Pregunta registrada: Q-d71ca855
>> Pregunta registrada: Q-53529247
>> Intento de registro con solo 3 opciones...
   [VALIDACION] La pregunta debe tener exactamente 4 opciones (actuales: 3).
>> Importando reactivos del proveedor externo...
[ADAPTER] Reactivo externo descartado: La pregunta debe tener exactamente 4 opciones (actuales: 3).
>> Reactivos externos incorporados: 2
...
Estado inicial: Borrador
Después de enviar: Pendiente de revisión
   [BLOQUEADO] No se puede editar el enunciado: la pregunta está en estado Pendiente de revisión.
Estado final: Aprobada | preguntas publicables: 1

>> Demostración Template Method...
Puntaje: 5.0 | Respuesta correcta. El texto senala la fragmentacion del habitat como causa principal.

>> Demostración Decorator...
[ALTA DIFICULTAD] Segun el texto, cual es la principal causa de la disminucion del condor?
Multiplicador de puntaje: 1.5
Evaluación ponderada: Puntaje: 7.5 | ...
```

---

## Integrantes — Grupo X

- Iván Alexander López
- Santiago Caicedo
- Adrián Araujo

---

*Asignatura: Ingeniería de Software II — Universidad del Cauca*
