package com.khartec.waltz.model.lineage_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLineageReportContributor.class)
@JsonDeserialize(as = ImmutableLineageReportContributor.class)
public abstract class LineageReportContributor implements DescriptionProvider {

    public abstract long physicalDataFlowId();
    public abstract long reportId();

}
