package org.finos.waltz.service.report_grid;

import org.apache.commons.jexl3.JexlScript;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.either.Either;
import org.finos.waltz.model.report_grid.ReportGridDerivedColumnDefinition;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CompiledCalculatedColumn {

    public abstract ReportGridDerivedColumnDefinition column();

    @Value.Auxiliary
    @Nullable
    public abstract Either<String, JexlScript> expression();

}
