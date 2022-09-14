package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityKindProvider;
import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.entity_field_reference.EntityFieldReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridFixedColumnDefinition.class)
@JsonDeserialize(as = ImmutableReportGridFixedColumnDefinition.class)
public abstract class ReportGridFixedColumnDefinition implements ExternalIdProvider, EntityKindProvider {

    @Nullable
    public abstract Long id();

    public abstract EntityKind columnEntityKind();

    @Nullable
    public abstract Long columnEntityId();

    @Nullable
    public abstract String columnName();

    @Nullable
    public abstract String columnDescription();

    public abstract long position();

    @Value.Default
    public RatingRollupRule ratingRollupRule() {
        return RatingRollupRule.NONE;
    }

    @Nullable
    public abstract String displayName();

    @Nullable
    public abstract EntityFieldReference entityFieldReference();


    @Nullable
    public abstract EntityKind columnQualifierKind();

    @Nullable
    public abstract Long columnQualifierId();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public EntityKind kind() {
        return EntityKind.REPORT_GRID_FIXED_COLUMN_DEFINITION;
    }

}
