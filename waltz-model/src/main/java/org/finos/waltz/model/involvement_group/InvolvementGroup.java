package org.finos.waltz.model.involvement_group;

import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
public abstract class InvolvementGroup implements IdProvider, NameProvider, ProvenanceProvider {

    public abstract String externalId();
}
