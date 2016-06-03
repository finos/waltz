package com.khartec.waltz.model.process;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ParentIdProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableProcess.class)
@JsonDeserialize(as = ImmutableProcess.class)
public abstract class Process implements
        IdProvider,
        ParentIdProvider,
        NameProvider,
        DescriptionProvider{

    public abstract int level();

    public abstract Optional<Long> level1();
    public abstract Optional<Long> level2();
    public abstract Optional<Long> level3();
}
