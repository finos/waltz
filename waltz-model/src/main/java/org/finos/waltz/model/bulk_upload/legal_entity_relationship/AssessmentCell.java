package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.immutables.value.Value;

import java.util.Set;

import static org.finos.waltz.common.SetUtilities.map;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentCell.class)
@JsonDeserialize(as = ImmutableAssessmentCell.class)
public abstract class AssessmentCell {


    public abstract String inputString();

    public abstract int columnId();

    public abstract Set<AssessmentCellRating> ratings(); //can be provided via header rating or listed in cell

    @Value.Derived
    public Set<ResolutionStatus> statuses() {
        return map(ratings(), AssessmentCellRating::status);
    }

}
