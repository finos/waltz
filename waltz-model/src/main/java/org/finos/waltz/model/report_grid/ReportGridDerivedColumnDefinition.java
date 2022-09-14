package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridDerivedColumnDefinition.class)
@JsonDeserialize(as = ImmutableReportGridDerivedColumnDefinition.class)
public abstract class ReportGridDerivedColumnDefinition implements ExternalIdProvider {

    @Nullable
    public abstract Long id();

    public abstract String displayName();

    @Nullable
    public abstract String columnDescription();

    @Value.Default
    public EntityKind columnEntityKind() {
        return EntityKind.REPORT_GRID_DERIVED_COLUMN_DEFINITION;
    }

    public abstract long position();


    public abstract String derivationScript();

}
