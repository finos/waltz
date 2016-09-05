/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableEntityReference.class)
@JsonDeserialize(as = ImmutableEntityReference.class)
public abstract class EntityReference {

    public abstract EntityKind kind();
    public abstract long id();
    public abstract Optional<String> name();


    public static EntityReference mkRef(EntityKind kind, long id) {
        return mkRef(kind, id, null);
    }


    public static EntityReference mkRef(EntityKind kind, long id, String name) {
        return ImmutableEntityReference.builder()
                .kind(kind)
                .id(id)
                .name(Optional.ofNullable(name))
                .build();
    }

}
