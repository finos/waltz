package com.khartec.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridColumnDefinition.class)
@JsonDeserialize(as = ImmutableReportGridColumnDefinition.class)
public abstract class ReportGridColumnDefinition {

    public abstract EntityReference columnEntityReference();
    public abstract long position();


    @Value.Default
    public ColumnUsageKind usageKind() {
        return ColumnUsageKind.NONE;
    }


    @Value.Default
    public RatingRollupRule ratingRollupRule() {
        return RatingRollupRule.NONE;
    }

}
