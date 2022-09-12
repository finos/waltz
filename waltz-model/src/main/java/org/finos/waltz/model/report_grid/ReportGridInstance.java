package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridInstance.class)
@JsonDeserialize(as = ImmutableReportGridInstance.class)
public abstract class ReportGridInstance {
    public abstract Set<ReportSubject> subjects();  // rows

    public abstract Set<RatingSchemeItem> ratingSchemeItems();  // color scheme
    public abstract Set<ReportGridCell> cellData();  // raw cell data
    public abstract Set<ReportGridCell> calculatedCellData();  // calculated cell data
}
