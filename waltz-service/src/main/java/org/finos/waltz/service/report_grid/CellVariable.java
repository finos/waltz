package org.finos.waltz.service.report_grid;

import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.ReportGridCell;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CellVariable extends ReportGridCell {

    @Nullable
    public abstract RatingSchemeItem rating();
}
