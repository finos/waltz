package org.finos.waltz.model.survey_template_exchange;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyTemplateModel.class)
@JsonDeserialize(as = ImmutableSurveyTemplateModel.class)
public interface SurveyTemplateModel extends NameProvider, DescriptionProvider, ExternalIdProvider {

    EntityKind targetEntityKind();

    @Nullable
    String issuanceRole();

    String ownerEmployeeId();

}
