package com.khartec.waltz.model.lineage_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLineageReportDescriptor.class)
@JsonDeserialize(as = ImmutableLineageReportDescriptor.class)
public abstract class LineageReportDescriptor {

    public abstract EntityReference reportReference();
    public abstract EntityReference articleReference();
    public abstract EntityReference applicationReference();

}
