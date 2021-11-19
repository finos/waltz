package org.finos.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableInstancesAndRecipientsCreateCommand.class)
@JsonDeserialize(as = ImmutableInstancesAndRecipientsCreateCommand.class)
public abstract class InstancesAndRecipientsCreateCommand implements Command {

    public abstract Long surveyRunId();
    public abstract LocalDate dueDate();
    public abstract LocalDate approvalDueDate();

    @Nullable
    public abstract String owningRole();

    public abstract Set<SurveyInstanceRecipient> excludedRecipients();
}
