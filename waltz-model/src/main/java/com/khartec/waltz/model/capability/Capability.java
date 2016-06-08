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

package com.khartec.waltz.model.capability;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableCapability.class)
@JsonDeserialize(as = ImmutableCapability.class)
public abstract class Capability implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ParentIdProvider,
        ProvenanceProvider {

    public abstract int level();

    public abstract Optional<Long> level1();
    public abstract Optional<Long> level2();
    public abstract Optional<Long> level3();
    public abstract Optional<Long> level4();
    public abstract Optional<Long> level5();

}
