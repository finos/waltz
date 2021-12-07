package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridUpdateCommand.class)
@JsonDeserialize(as = ImmutableReportGridUpdateCommand.class)
public abstract class ReportGridUpdateCommand implements Command {

    public abstract String name();

    @Nullable
    public abstract String description();

    @Value.Default
    public ReportGridKind kind(){
        return ReportGridKind.PUBLIC;
    }
}
