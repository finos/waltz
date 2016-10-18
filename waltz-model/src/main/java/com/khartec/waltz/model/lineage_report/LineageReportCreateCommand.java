package com.khartec.waltz.model.lineage_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLineageReportCreateCommand.class)
@JsonDeserialize(as = ImmutableLineageReportCreateCommand.class)
public abstract class LineageReportCreateCommand implements NameProvider, DescriptionProvider {

    public abstract long specificationId();

}
