package org.finos.waltz.web.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.NameProvider;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridDefinitionJSON.class)
public interface ReportGridDefinitionJSON extends IdProvider, NameProvider, ExternalIdProvider, DescriptionProvider {

    @Value.Immutable
    @JsonSerialize(as = ImmutableReportGridColumnJSON.class)
    interface ReportGridColumnJSON extends IdProvider, NameProvider, DescriptionProvider {
        int position();
    }

    EntityKind subjectKind();

    List<ReportGridColumnJSON> columns();
}
