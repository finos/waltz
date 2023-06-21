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
import org.finos.waltz.common.SetUtilities;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.model.HierarchyQueryScope.CHILDREN;
import static org.finos.waltz.model.HierarchyQueryScope.EXACT;


@Value.Immutable
@JsonSerialize(as = ImmutableIdSelectionOptions.class)
@JsonDeserialize(as = ImmutableIdSelectionOptions.class)
public abstract class IdSelectionOptions {

    public abstract EntityReference entityReference();
    public abstract HierarchyQueryScope scope();
    public abstract Optional<EntityKind> joiningEntityKind();

    @Value.Default
    public Set<EntityLifecycleStatus> entityLifecycleStatuses() {
        return SetUtilities.fromArray(EntityLifecycleStatus.ACTIVE);
    }

    @Value.Default
    public SelectionFilters filters() {
        return SelectionFilters.NO_FILTERS;
    }

  
    public static IdSelectionOptions mkOpts(EntityReference ref, HierarchyQueryScope scope) {
        return ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();
    }


    public static IdSelectionOptions mkOpts(EntityReference ref, HierarchyQueryScope scope, EntityKind joiningEntityKind) {
        return ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .joiningEntityKind(joiningEntityKind)
                .build();
    }

  
    public static IdSelectionOptions mkOptsForAllLifecycleStates(EntityReference ref, HierarchyQueryScope scope) {
        return ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .entityLifecycleStatuses(asList(EntityLifecycleStatus.values()))
                .build();
    }


    public static IdSelectionOptions mkOpts(EntityReference ref) {
        return ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(determineDefaultScope(ref.kind()))
                .build();
    }


    protected static HierarchyQueryScope determineDefaultScope(EntityKind kind) {
        switch (kind) {
            case ACTOR:
            case APPLICATION:
            case APP_GROUP:
            case CHANGE_INITIATIVE:
            case FLOW_DIAGRAM:
            case LOGICAL_DATA_ELEMENT:
            case LOGICAL_DATA_FLOW:
            case PHYSICAL_FLOW:
            case PHYSICAL_SPECIFICATION:
            case PROCESS_DIAGRAM:
            case SCENARIO:
            case SERVER:
                return EXACT;
            default:
                return CHILDREN;
        }

    }
}
