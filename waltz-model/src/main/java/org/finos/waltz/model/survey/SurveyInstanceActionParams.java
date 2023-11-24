package org.finos.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceActionParams.class)
@JsonDeserialize(as = ImmutableSurveyInstanceActionParams.class)
public abstract class SurveyInstanceActionParams {

    public abstract Optional<String> reason();
    public abstract Optional<LocalDate> newDueDate();
    public abstract Optional<LocalDate> newApprovalDueDate();

}
