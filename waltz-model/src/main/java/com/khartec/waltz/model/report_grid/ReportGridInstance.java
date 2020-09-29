package com.khartec.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.rating.RagName;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridInstance.class)
@JsonDeserialize(as = ImmutableReportGridInstance.class)
public abstract class ReportGridInstance {
    public abstract Set<Application> applications();  // rows
    public abstract Set<RagName> ratingSchemeItems();  // color scheme
    public abstract Set<ReportGridRatingCell> cellData();  // raw cell data
}
