package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.LastUpdatedProvider;
import org.finos.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridInfo.class)
@JsonDeserialize(as = ImmutableReportGridInfo.class)
public abstract class ReportGridInfo implements LastUpdatedProvider, ProvenanceProvider {

    public abstract EntityReference gridReference();

    public abstract EntityKind subjectKind();

    @Value.Default
    public ReportGridKind visibilityKind() {
        return ReportGridKind.PRIVATE;
    }
}
