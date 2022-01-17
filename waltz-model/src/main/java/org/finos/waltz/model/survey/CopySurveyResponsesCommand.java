package org.finos.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableCopySurveyResponsesCommand.class)
@JsonDeserialize(as = ImmutableCopySurveyResponsesCommand.class)
public abstract class CopySurveyResponsesCommand implements Command {

    public abstract Set<Long> targetSurveyInstanceIds();

    @Value.Default
    public boolean overrideExistingResponses() {
        return false;
    };

    public abstract List<Long> questionIds();
}
