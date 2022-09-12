package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridCalculatedColumnDefinition.class)
@JsonDeserialize(as = ImmutableReportGridCalculatedColumnDefinition.class)
public abstract class ReportGridCalculatedColumnDefinition {

    @Nullable
    public abstract Long id();

    public abstract String displayName();

    @Nullable
    public abstract String columnDescription();

    public abstract long position();


    public abstract String valueExpression();

    @Nullable
    public abstract String outcomeExpression();

    //  (r) => r.colA && r.colB ? r.colA + r.colB : null;
    //  (r, v) => v > 10 ? (v > 100 ? "Loads": "Lots") : "Few"
    //  (r, v) => {
    //      if (v > 100) "Loads";
    //      if (v > 10) "Lots";
    //      if (v > 0) "Few";
    //      else "None";

}
