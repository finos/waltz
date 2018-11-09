package com.khartec.waltz.model.taxonomy_management;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableTaxonomyChangeImpact.class)
@JsonDeserialize(as = ImmutableTaxonomyChangeImpact.class)
public abstract class TaxonomyChangeImpact implements DescriptionProvider {

    public abstract Severity severity();
    public abstract String description();
    public abstract Set<EntityReference> impactedReferences();

}
