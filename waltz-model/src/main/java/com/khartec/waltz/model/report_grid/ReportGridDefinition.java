package com.khartec.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
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

    public abstract List<ReportGridColumnDefinition> columnDefinitions();  // columns


}
