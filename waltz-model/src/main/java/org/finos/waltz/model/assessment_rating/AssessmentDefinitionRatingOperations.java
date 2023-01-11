package org.finos.waltz.model.assessment_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.Operation;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.*;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentDefinitionRatingOperations.class)
@JsonDeserialize(as = ImmutableAssessmentDefinitionRatingOperations.class)
public abstract class AssessmentDefinitionRatingOperations {


    @Nullable
    public abstract Set<AssessmentRatingOperations> ratingOperations();

    public Set<Operation> findForRatingId(Long ratingId) {
        return find(ratingOperations(), d -> d.ratingId() != null && d.ratingId().equals(ratingId))
                .map(AssessmentRatingOperations::operations)
                .orElse(findDefault());
    }

    public Set<Operation> findDefault() {
        return find(ratingOperations(), AssessmentRatingOperations::isDefault)
                .map(AssessmentRatingOperations::operations)
                .orElse(emptySet());
    }
}
