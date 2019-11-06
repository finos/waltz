package com.khartec.waltz.model.tag;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.CreatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.WaltzEntity;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTagUsage.class)
@JsonDeserialize(as = ImmutableTagUsage.class)
public abstract class TagUsage implements
        WaltzEntity,
        CreatedProvider,
        ProvenanceProvider {

    public abstract long tagId();
}
