/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.model.immediate_hierarchy;


import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ParentIdProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.filter;
import static com.khartec.waltz.common.ListUtilities.ensureNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.indexBy;

public class ImmediateHierarchyUtilities {

    public static<T extends IdProvider & ParentIdProvider> ImmediateHierarchy<T> build(long selfId, List<T> ts) {
        return build(selfId, ts, Function.identity());
    }


    public static<X, T extends IdProvider & ParentIdProvider> ImmediateHierarchy<X> build(long selfId, List<X> xs, Function<X, T> converter) {

        Checks.checkNotNull(converter, "converter cannot be null");
        checkNotEmpty(xs, "Cannot construct a hierarchy with no building blocks");

        ImmutableImmediateHierarchy.Builder<X> builder = ImmutableImmediateHierarchy.builder();

        Map<Long, X> byId = indexBy(x -> converter.apply(x).id().get(), xs);

        Map<Long, Collection<X>> byParentId = groupBy(
                x -> converter.apply(x).parentId().get(),
                filter(xs, x -> converter.apply(x).parentId().isPresent()));

        // self
        X self = byId.get(selfId);
        checkNotNull(self, "self could not be found, cannot create immediate hierarchy");

        builder.self(self);

        // parent
        converter.apply(self).parentId()
                .map(pId -> byId.get(pId))
                .map(parent -> builder.parent(parent));

        // children
        builder.children(ensureNotNull(byParentId.get(selfId)));


        return builder.build();
    }

}
