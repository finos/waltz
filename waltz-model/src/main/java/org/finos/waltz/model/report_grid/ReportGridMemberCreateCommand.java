package org.finos.waltz.model.report_grid;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridMemberCreateCommand.class)
@JsonDeserialize(as = ImmutableReportGridMemberCreateCommand.class)
public abstract class ReportGridMemberCreateCommand implements Command {

    public abstract long gridId();

    public abstract String userId();

    public abstract ReportGridMemberRole role();
}
