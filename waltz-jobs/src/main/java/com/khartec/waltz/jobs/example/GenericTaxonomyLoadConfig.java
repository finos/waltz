package com.khartec.waltz.jobs.example;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class GenericTaxonomyLoadConfig {
    public abstract String resourcePath();

    public abstract Integer maxLevels();
    public abstract Optional<Integer> descriptionOffset();
    public abstract String taxonomyName();
    public abstract String taxonomyDescription();
    public abstract String taxonomyExternalId();
    public abstract Long ratingSchemeId();

}
