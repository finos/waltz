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

package org.finos.waltz.model.app_group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableAppGroup.class)
@JsonDeserialize(as = ImmutableAppGroup.class)
public interface AppGroup extends
        IdProvider,
        EntityKindProvider,
        NameProvider,
        DescriptionProvider,
        WaltzEntity,
        ExternalIdProvider,
        IsRemovedProvider {

    AppGroupKind appGroupKind();


    @Value.Default
    default EntityKind kind() { return EntityKind.APP_GROUP; }


    default EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.APP_GROUP)
                .id(id().get())
                .name(name())
                .externalId(externalId())
                .description(description())
                .build();
    }


    @Value.Default
    default boolean isFavouriteGroup() {
        return false;
    }


}
