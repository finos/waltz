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

package com.khartec.waltz.model.entity_search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import org.immutables.value.Value;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityLifecycleStatus.ACTIVE;
import static com.khartec.waltz.model.EntityLifecycleStatus.PENDING;

@Value.Immutable
@JsonSerialize(as = ImmutableEntitySearchOptions.class)
@JsonDeserialize(as = ImmutableEntitySearchOptions.class)
public abstract class EntitySearchOptions {

    public static final int DEFAULT_SEARCH_RESULTS_LIMIT = 40;

    public abstract List<EntityKind> entityKinds();

    public abstract String searchQuery();


    @Value.Default
    public List<EntityLifecycleStatus> entityLifecycleStatuses() {
        return newArrayList(ACTIVE, PENDING);
    }


    @Value.Default
    public int limit() {
        return DEFAULT_SEARCH_RESULTS_LIMIT;
    }


    @Value.Default
    public String userId() {
        return "UNKNOWN";
    }


    public static EntitySearchOptions mkForEntity(EntityKind entityKind, String searchQuery) {
        return ImmutableEntitySearchOptions.builder()
                .entityKinds(newArrayList(entityKind))
                .searchQuery(searchQuery)
                .build();
    }
}
