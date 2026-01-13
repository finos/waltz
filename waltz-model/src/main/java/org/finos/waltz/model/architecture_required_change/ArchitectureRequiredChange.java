package org.finos.waltz.model.architecture_required_change;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableArchitectureRequiredChange.class)
@JsonDeserialize(as = ImmutableArchitectureRequiredChange.class)
public abstract class ArchitectureRequiredChange {
    public abstract long id();
    public abstract String externalId();
    public abstract String title();
    public abstract String description();
    public abstract String status();
    public abstract Optional<String> milestoneRag();
    public abstract Optional<LocalDateTime> milestoneForecastDate();
    public abstract Optional<String> externalParentId();
    public abstract Optional<Long> linkedEntityId();
    public abstract Optional<EntityKind> linkedEntityKind();
    public abstract String createdBy();
    public abstract LocalDateTime createdAt();
    public abstract String updatedBy();
    public abstract LocalDateTime updatedAt();
    public abstract Optional<String> provenance();
    public abstract EntityLifecycleStatus entityLifecycleStatus();
}
