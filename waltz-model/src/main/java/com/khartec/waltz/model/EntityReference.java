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

package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;


@Value.Immutable
@JsonSerialize(as = ImmutableEntityReference.class)
@JsonDeserialize(as = ImmutableEntityReference.class)
/**
 * Represents a generic entity reference.  Note that the optional name
 * and description fields are not used in equals/hashcode computations.
 */
public abstract class EntityReference implements EntityLifecycleStatusProvider {

    public abstract EntityKind kind();
    public abstract long id();


    @Value.Auxiliary
    public abstract Optional<String> name();

    @Value.Auxiliary
    public abstract Optional<String> description();

    @Value.Default
    public  EntityLifecycleStatus entityLifecycleStatus() { return  EntityLifecycleStatus.ACTIVE; }


    public static <T extends NameProvider & IdProvider & DescriptionProvider> EntityReference fromEntity(T entity, EntityKind kind) {
        Long id = entity.id()
                .orElseThrow(() -> new IllegalArgumentException("Cannot create a reference from an entity with an empty id"));
        return mkRef(kind, id, entity.name(), entity.description());
    }


    public static EntityReference mkRef(EntityKind kind, long id) {
        return mkRef(kind, id, null);
    }


    public static EntityReference mkRef(EntityKind kind, long id, String name) {
        return mkRef(kind, id, name, null);
    }


    public static EntityReference mkRef(EntityKind kind, long id, String name, String description) {
        return ImmutableEntityReference.builder()
                .kind(kind)
                .id(id)
                .name(Optional.ofNullable(name))
                .description(Optional.ofNullable(description))
                .build();
    }


    public static EntityReference mkRef(Map map) {
        checkNotNull(map, "map cannot be null");
        checkTrue(map.containsKey("kind"), "kind does not exist");
        checkTrue(map.containsKey("id"), "id does not exist");
        checkNotNull(map.get("kind"), "kind cannot be null");
        checkNotNull(map.get("id"), "id cannot be null");

        return EntityReference.mkRef(
                Enum.valueOf(EntityKind.class, map.get("kind").toString()),
                Long.parseLong(map.get("id").toString()),
                (String) map.get("name"),
                (String) map.get("description"));
    }



}
