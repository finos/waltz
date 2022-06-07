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

package org.finos.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;


@Value.Immutable
@JsonSerialize(as = ImmutableEntityReference.class)
@JsonDeserialize(as = ImmutableEntityReference.class)
/**
 * Represents a generic entity reference.  Note that the optional name
 * and description fields are not used in equals/hashcode computations.
 */
public abstract class EntityReference implements EntityLifecycleStatusProvider, DescriptionProvider, ExternalIdProvider {

    public abstract EntityKind kind();
    public abstract long id();


    @Value.Auxiliary
    public abstract Optional<String> name();

    @Value.Auxiliary
    @Override
    public abstract Optional<String> externalId();

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
        return mkRef(kind, id, name, description, null);
    }


    public static EntityReference mkRef(EntityKind kind, long id, String name, String description, String externalId) {
        return ImmutableEntityReference.builder()
                .kind(kind)
                .id(id)
                .name(Optional.ofNullable(name))
                .description(description)
                .externalId(Optional.ofNullable(externalId))
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
