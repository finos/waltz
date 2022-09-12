package org.finos.waltz.service.report_grid;

import org.apache.commons.jexl3.JexlExpression;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.either.Either;
import org.finos.waltz.model.report_grid.ReportGridCalculatedColumnDefinition;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CompiledCalculatedColumn {

    public abstract ReportGridCalculatedColumnDefinition column();

    @Value.Auxiliary
    @Nullable
    public abstract Either<String, JexlExpression> valueExpression();

    @Value.Auxiliary
    @Nullable
    public abstract Either<String, JexlExpression> outcomeExpression();

}
