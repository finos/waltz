package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridColumnDefinitionsUpdateCommand.class)
@JsonDeserialize(as = ImmutableReportGridColumnDefinitionsUpdateCommand.class)
public abstract class ReportGridColumnDefinitionsUpdateCommand implements Command {

    public abstract List<ReportGridFixedColumnDefinition> columnDefinitions();

}
