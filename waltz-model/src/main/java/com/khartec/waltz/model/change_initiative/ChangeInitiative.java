package com.khartec.waltz.model.change_initiative;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.application.LifecyclePhase;
import org.immutables.value.Value;

import java.util.Date;


@Value.Immutable
@JsonSerialize(as = ImmutableChangeInitiative.class)
@JsonDeserialize(as = ImmutableChangeInitiative.class)
public abstract class ChangeInitiative implements
        ExternalIdProvider,
        ParentIdProvider,
        NameProvider,
        IdProvider,
        DescriptionProvider,
        ProvenanceProvider {

    public abstract ChangeInitiativeKind kind();
    public abstract LifecyclePhase lifecyclePhase();

    public abstract Date lastUpdate();

    public abstract Date startDate();
    public abstract Date endDate();

}
