package org.finos.waltz.model.assessment_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.Operation;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as=ImmutableAssessmentRatingOperations.class)
@JsonDeserialize(as=ImmutableAssessmentRatingOperations.class)
public abstract class AssessmentRatingOperations {


    @Nullable
    public abstract Long ratingId();
    public abstract Set<Operation> operations();

    @Value.Derived
    public boolean isDefault() {
        return ratingId() == null;
    }
}
