package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridInfo.class)
@JsonDeserialize(as = ImmutableReportGridInfo.class)
public abstract class ReportGridInfo implements LastUpdatedProvider, ProvenanceProvider, NameProvider, ExternalIdProvider, DescriptionProvider {

    public abstract long gridId();

    public abstract EntityKind subjectKind();

    @Value.Default
    public ReportGridKind visibilityKind() {
        return ReportGridKind.PRIVATE;
    }
}
