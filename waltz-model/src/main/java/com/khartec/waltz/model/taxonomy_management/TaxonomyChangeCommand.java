package com.khartec.waltz.model.taxonomy_management;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as =  ImmutableTaxonomyChangeCommand.class)
@JsonDeserialize(as =  ImmutableTaxonomyChangeCommand.class)
public abstract class TaxonomyChangeCommand implements
        Command,
        IdProvider,
        CreatedProvider {

    public abstract TaxonomyChangeType changeType();

    @Value.Default
    public TaxonomyChangeLifecycleStatus status() {
        return TaxonomyChangeLifecycleStatus.DRAFT;
    }

    public abstract EntityReference changeDomain();

    public abstract EntityReference a();

    @Nullable
    public abstract EntityReference b();

    @Nullable
    public abstract String newValue();

    @Nullable
    public abstract LocalDateTime executedAt();

    @Nullable
    public abstract String executedBy();

}
