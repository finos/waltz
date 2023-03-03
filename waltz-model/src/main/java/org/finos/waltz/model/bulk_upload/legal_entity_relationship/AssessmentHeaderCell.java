package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.bulk_upload.ResolvedAssessmentHeaderStatus;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentHeaderCell.class)
@JsonDeserialize(as = ImmutableAssessmentHeaderCell.class)
public abstract class AssessmentHeaderCell {

    public abstract String inputString();

    public abstract int columnId();

    public abstract Optional<AssessmentDefinition> resolvedAssessmentDefinition();

    public abstract Optional<RatingSchemeItem> resolvedRating();  //optionally included in header to allow comment

    public abstract ResolvedAssessmentHeaderStatus status();

    public abstract Map<String, RatingSchemeItem> ratingLookupMap();


    // ----- HELPERS -----

    public static AssessmentHeaderCell mkHeader(String inputString,
                                                Optional<AssessmentDefinition> defn,
                                                Optional<RatingSchemeItem> rating,
                                                ResolvedAssessmentHeaderStatus status,
                                                Set<RatingSchemeItem> ratingSchemeItems,
                                                int columnId) {

        Map<String, RatingSchemeItem> ratingSchemeItemsByName = MapUtilities.indexBy(ratingSchemeItems, d -> d.name());
        Map<String, RatingSchemeItem> ratingSchemeItemsByExternalId = ratingSchemeItems
                .stream()
                .filter(d -> d.externalId().isPresent())
                .collect(toMap(d -> d.externalId().get(), v -> v));

        Map<String, RatingSchemeItem> ratingLookipMap = MapUtilities.merge(ratingSchemeItemsByName, ratingSchemeItemsByExternalId);

        return ImmutableAssessmentHeaderCell.builder()
                .resolvedAssessmentDefinition(defn)
                .resolvedRating(rating)
                .status(status)
                .inputString(inputString)
                .columnId(columnId)
                .ratingLookupMap(ratingLookipMap)
                .build();
    }


}
