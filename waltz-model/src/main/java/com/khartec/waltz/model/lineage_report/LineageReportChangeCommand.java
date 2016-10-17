package com.khartec.waltz.model.lineage_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.command.EntityChangeCommand;
import com.khartec.waltz.model.command.FieldChange;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableLineageReportChangeCommand.class)
@JsonDeserialize(as = ImmutableLineageReportChangeCommand.class)
public abstract class LineageReportChangeCommand implements EntityChangeCommand {

    public abstract Optional<FieldChange<String>> name();

    public abstract Optional<FieldChange<String>> description();

}
