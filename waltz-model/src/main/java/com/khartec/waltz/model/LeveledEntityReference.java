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


/**
 * A leveled entity reference is a lightweight object
 * which represents an entity reference and it's level
 * within it's entity hierarchy.  Typical uses are for
 * sending back a simplistic subset of a hierarchy (i.e.
 * immediate parents/children)
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLeveledEntityReference.class)
@JsonDeserialize(as = ImmutableLeveledEntityReference.class)
public abstract class LeveledEntityReference {

    public abstract EntityReference entityReference();
    public abstract int level();

}
