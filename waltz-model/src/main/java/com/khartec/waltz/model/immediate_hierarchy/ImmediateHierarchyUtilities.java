/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
