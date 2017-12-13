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

package com.khartec.waltz.model.entity_search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableEntitySearchOptions.class)
@JsonDeserialize(as = ImmutableEntitySearchOptions.class)
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
