package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyRun.class)
@JsonDeserialize(as = ImmutableSurveyRun.class)
public abstract class SurveyRun implements IdProvider, NameProvider, DescriptionProvider {

    public abstract Long surveyTemplateId();
    public abstract IdSelectionOptions selectionOptions();
    public abstract Set<Long> involvementKindIds();
    public abstract Optional<LocalDate> issuedOn();
    public abstract LocalDate dueDate();
    public abstract SurveyIssuanceKind issuanceKind();
    public abstract Long ownerId();
    public abstract String contactEmail();
    public abstract SurveyRunStatus status();
}
