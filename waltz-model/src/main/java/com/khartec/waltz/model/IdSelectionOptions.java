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
import com.khartec.waltz.common.SetUtilities;
import org.immutables.value.Value;

import java.util.Set;

import static com.khartec.waltz.model.HierarchyQueryScope.CHILDREN;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;

@Value.Immutable
@JsonSerialize(as = ImmutableIdSelectionOptions.class)
@JsonDeserialize(as = ImmutableIdSelectionOptions.class)
public abstract class IdSelectionOptions {

    public abstract EntityReference entityReference();
    public abstract HierarchyQueryScope scope();

    @Value.Default
    public Set<EntityLifecycleStatus> entityLifecycleStatuses() {
        return SetUtilities.fromArray(EntityLifecycleStatus.ACTIVE);
    }

    public static IdSelectionOptions mkOpts(EntityReference ref, HierarchyQueryScope scope) {
        return ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();
    }

    public static IdSelectionOptions mkOpts(EntityReference ref) {
        return ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(determineDefaultScope(ref.kind()))
                .build();
    }


    private static HierarchyQueryScope determineDefaultScope(EntityKind kind) {
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
            case SCENARIO:
            case SERVER:
                return EXACT;
            default:
                return CHILDREN;
        }

    }
}
