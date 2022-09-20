package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridDefinition.class)
@JsonDeserialize(as = ImmutableReportGridDefinition.class)
public abstract class ReportGridDefinition implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        LastUpdatedProvider,
        ProvenanceProvider,
        ExternalIdProvider {

    public abstract List<ReportGridFixedColumnDefinition> fixedColumnDefinitions();  // columns

    public abstract List<ReportGridDerivedColumnDefinition> derivedColumnDefinitions();  // columns

    public abstract EntityKind subjectKind();

    @Value.Default
    public ReportGridKind kind() {
        return ReportGridKind.PUBLIC;
    }
}
