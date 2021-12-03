package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
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

    @Nullable
    public abstract String displayName();

}
