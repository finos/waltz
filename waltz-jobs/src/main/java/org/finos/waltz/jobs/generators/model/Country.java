package org.finos.waltz.jobs.generators.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Country {

    public abstract String name();

    public abstract String code();

    public abstract String region();

    public abstract String regionCode();
}
