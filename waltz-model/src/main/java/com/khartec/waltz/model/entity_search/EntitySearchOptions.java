package com.khartec.waltz.model.entity_search;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class EntitySearchOptions {
    public abstract List<EntityKind> entityKinds();

    @Value.Default
    public int limit() {
        return 20;
    }


    @Value.Default
    public String userId() {
        return "UNKNOWN";
    }


    public static EntitySearchOptions mkForEntity(EntityKind entityKind) {
        return ImmutableEntitySearchOptions.builder()
                .entityKinds(ListUtilities.newArrayList(entityKind))
                .build();
    }
}
