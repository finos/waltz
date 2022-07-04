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
package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityReferenceKeyCell.class)
@JsonDeserialize(as = ImmutableEntityReferenceKeyCell.class)
public interface EntityReferenceKeyCell extends KeyCell {

    Optional<String> name();

    @Value.Default
    default String type() {
        return ApiTypes.ENTITY_REFERENCE_KEYCELL;
    }

    Optional<EntityKind> kind();

    Optional<Long> waltzId();

    Optional<String> externalId();

    Optional<EntityLifecycleStatus> lifecycleStatus();


    static EntityReferenceKeyCell fromRef(EntityReference ref) {
        return ImmutableEntityReferenceKeyCell
                .builder()
                .name(ref.name())
                .kind(ref.kind())
                .waltzId(ref.id())
                .externalId(ref.externalId())
                .lifecycleStatus(ref.entityLifecycleStatus())
                .build();
    }

}
