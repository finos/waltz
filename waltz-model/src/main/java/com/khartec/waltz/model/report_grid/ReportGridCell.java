package com.khartec.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.CommentProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridCell.class)
@JsonDeserialize(as = ImmutableReportGridCell.class)
public abstract class ReportGridCell implements CommentProvider {

    public abstract EntityKind columnEntityKind();  // x
    public abstract long columnEntityId();  // x

    public abstract long applicationId(); // y

    @Nullable
    public abstract Long ratingId();

    @Nullable
    public abstract BigDecimal value();

}