package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridCreateCommand.class)
@JsonDeserialize(as = ImmutableReportGridCreateCommand.class)
public abstract class ReportGridCreateCommand implements Command {

    public abstract String name();

    public abstract EntityKind subjectKind();

    @Nullable
    public abstract String description();

    @Value.Default
    public ReportGridKind kind() {
        return ReportGridKind.PUBLIC;
    }

    public String toExtId() {
        UUID uuid  =  UUID.randomUUID();
        return uuid.toString();
    }
}
