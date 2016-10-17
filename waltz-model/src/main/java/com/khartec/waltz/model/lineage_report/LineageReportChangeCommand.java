package com.khartec.waltz.model.lineage_report;

import com.khartec.waltz.model.command.EntityChangeCommand;
import com.khartec.waltz.model.command.FieldChange;

import java.util.Optional;


public abstract class LineageReportChangeCommand implements EntityChangeCommand {

    public abstract Optional<FieldChange<String>> name();

    public abstract Optional<FieldChange<String>> description();

}
