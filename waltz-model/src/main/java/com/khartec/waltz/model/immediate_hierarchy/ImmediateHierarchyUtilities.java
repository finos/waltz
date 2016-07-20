package com.khartec.waltz.model.immediate_hierarchy;


import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ParentIdProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.filter;
import static com.khartec.waltz.common.ListUtilities.ensureNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.indexBy;

public class ImmediateHierarchyUtilities {

    public static<T extends IdProvider & ParentIdProvider> ImmediateHierarchy<T> build(long selfId, List<T> ts) {

        checkNotEmpty(ts, "Cannot construct a hierarchy with no building blocks");

        ImmutableImmediateHierarchy.Builder<T> builder = ImmutableImmediateHierarchy.builder();

        Map<Long, T> byId = indexBy(t -> t.id().get(), ts);

        Map<Long, Collection<T>> byParentId = groupBy(
                t -> t.parentId().get(),
                filter(ts, t -> t.parentId().isPresent()));

        // self
        T self = byId.get(selfId);
        checkNotNull(self, "self could not be found, cannot create immediate hierarchy");

        builder.self(self);

        // parent
        self.parentId()
                .map(pId -> byId.get(pId))
                .map(parent -> builder.parent(parent));

        // siblings
        self.parentId()
                .map(pId -> byParentId.get(pId))
                .map(siblings -> filter(siblings, t -> t.id().get() != selfId))
                .map(siblings -> builder.siblings(siblings));

        // children
        builder.children(ensureNotNull(byParentId.get(selfId)));


        return builder.build();
    }

}
