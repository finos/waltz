package com.khartec.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridRatingCell.class)
@JsonDeserialize(as = ImmutableReportGridRatingCell.class)
public abstract class ReportGridRatingCell {

    public abstract EntityKind columnEntityKind();  // x
    public abstract long columnEntityId();  // x

    public abstract long applicationId(); // y
    public abstract long ratingId();

}