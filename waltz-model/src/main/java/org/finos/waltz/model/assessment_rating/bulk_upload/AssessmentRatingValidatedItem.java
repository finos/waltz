package org.finos.waltz.model.assessment_rating.bulk_upload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonDeserialize(as = ImmutableAssessmentRatingValidatedItem.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public interface AssessmentRatingValidatedItem {

    AssessmentRatingParsedItem parsedItem();

    ChangeOperation changeOperation();

    Set<ChangedFieldType> changedFields();

    Set<ValidationError> errors();

    @Nullable
    RatingSchemeItem ratingSchemeItem();

    @Nullable
    EntityReference entityKindReference();
}
