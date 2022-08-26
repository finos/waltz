package org.finos.waltz.model.report_grid;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridFilterInfo.class)
@JsonDeserialize(as = ImmutableReportGridFilterInfo.class)
public abstract class ReportGridFilterInfo {

    public abstract Long appGroupId();

    public abstract ReportGridDefinition gridDefinition();

    public abstract IdSelectionOptions idSelectionOptions();

    public abstract Set<GridFilter> gridFilters();

}
