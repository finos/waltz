package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyTemplate.class)
@JsonDeserialize(as = ImmutableSurveyTemplate.class)
public abstract class SurveyTemplate implements IdProvider, NameProvider, DescriptionProvider {

    public abstract EntityKind targetEntityKind();
    public abstract Long ownerId();


    @Value.Default
    public LocalDateTime createdAt() {
        return LocalDateTime.now();
    }


    @Value.Default
    public ReleaseLifecycleStatus status() {
        return ReleaseLifecycleStatus.ACTIVE;
    }
}
