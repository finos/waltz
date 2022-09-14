package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridDerivedColumnDefinition.class)
@JsonDeserialize(as = ImmutableReportGridDerivedColumnDefinition.class)
public abstract class ReportGridDerivedColumnDefinition implements ExternalIdProvider, EntityKindProvider {

    @Nullable
    public abstract Long id();

    public abstract String displayName();

    @Nullable
    public abstract String columnDescription();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public EntityKind columnEntityKind() {
        return EntityKind.REPORT_GRID_DERIVED_COLUMN_DEFINITION;
    }

    public abstract long position();


    public abstract String derivationScript();


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public EntityKind kind() {
        return EntityKind.REPORT_GRID_DERIVED_COLUMN_DEFINITION;
    }
}
