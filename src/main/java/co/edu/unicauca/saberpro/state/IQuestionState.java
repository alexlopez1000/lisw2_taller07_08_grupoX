package co.edu.unicauca.saberpro.state;

import co.edu.unicauca.saberpro.domain.model.Option;

import java.util.List;

/** Estado del ciclo de vida de una pregunta. Cada estado define sus operaciones válidas. */
public interface IQuestionState {
    String getName();

    void submitForReview(QuestionWorkflow workflow);
    void startReview(QuestionWorkflow workflow);
    void approve(QuestionWorkflow workflow);
    void requestCorrections(QuestionWorkflow workflow);

    void editStatement(QuestionWorkflow workflow, String newStatement);
    void editOptions(QuestionWorkflow workflow, List<Option> newOptions);

    boolean canEdit();
    boolean canPublish();
}
