package org.finos.waltz.model.report_grid;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridMemberDeleteCommand.class)
@JsonDeserialize(as = ImmutableReportGridMemberDeleteCommand.class)
public abstract class ReportGridMemberDeleteCommand implements Command {

    public abstract String userId();
    public abstract ReportGridMemberRole role();

}
