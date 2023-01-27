package org.finos.waltz.service.report_grid;

import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.ReportGridCell;
import org.immutables.value.Value;

import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class CellVariable extends ReportGridCell {

    public abstract Set<RatingSchemeItem> ratings();

    @Value.Derived
    public String cellName() {
        return ratings()
                .stream()
                .map(NameProvider::name)
                .collect(Collectors.joining(", "));
    }
}
