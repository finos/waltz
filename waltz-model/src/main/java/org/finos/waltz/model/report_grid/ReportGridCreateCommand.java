package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridCreateCommand.class)
@JsonDeserialize(as = ImmutableReportGridCreateCommand.class)
public abstract class ReportGridCreateCommand implements Command {

    public abstract String name();
    public abstract String externalId();

    @Nullable
    public abstract String description();
}
