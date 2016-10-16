package com.khartec.waltz.model.lineage_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCreateLineageReportCommand.class)
@JsonDeserialize(as = ImmutableCreateLineageReportCommand.class)
public abstract class CreateLineageReportCommand implements NameProvider, DescriptionProvider {

    public abstract long articleId();

}
