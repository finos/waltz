package com.khartec.waltz.model.taxonomy_management;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableTaxonomyChangePreview.class)
@JsonDeserialize(as = ImmutableTaxonomyChangePreview.class)
public abstract class TaxonomyChangePreview {

    public abstract TaxonomyChangeCommand command();
    public abstract List<TaxonomyChangeImpact> impacts();
}
