package org.finos.waltz.model.report_grid;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridMemberUpdateRoleCommand.class)
@JsonDeserialize(as = ImmutableReportGridMemberUpdateRoleCommand.class)
public abstract class ReportGridMemberUpdateRoleCommand implements Command {

    public abstract String userId();

    @Value.Default
    public ReportGridMemberRole role() {
        return ReportGridMemberRole.VIEWER;
    }

}
