package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyTemplateChangeCommand.class)
@JsonDeserialize(as = ImmutableSurveyTemplateChangeCommand.class)
public abstract class SurveyTemplateChangeCommand implements IdProvider, NameProvider, DescriptionProvider {

    public abstract EntityKind targetEntityKind();
}
