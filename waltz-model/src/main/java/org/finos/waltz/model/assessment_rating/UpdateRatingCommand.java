package org.finos.waltz.model.assessment_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateRatingCommand.class)
@JsonDeserialize(as = ImmutableUpdateRatingCommand.class)
public abstract class UpdateRatingCommand {
    public abstract Long newRatingId();

}
