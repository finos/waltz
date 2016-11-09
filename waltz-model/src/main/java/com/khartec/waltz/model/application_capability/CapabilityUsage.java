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

package com.khartec.waltz.model.application_capability;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.capabilityrating.CapabilityRating;
import org.immutables.value.Value;

import java.util.List;


@Deprecated
@Value.Immutable
@JsonSerialize(as = ImmutableCapabilityUsage.class)
@JsonDeserialize(as = ImmutableCapabilityUsage.class)
public abstract class CapabilityUsage {

    public abstract Capability capability();
    public abstract List<ApplicationCapability> usages();
    public abstract List<CapabilityRating> ratings();

    public abstract List<CapabilityUsage> children();

    @Override
    public String toString() {
        return new StringBuffer("CapabilityUsage{")
                .append(" usages: ")
                .append(usages().size())
                .append(" ratings: ")
                .append(ratings().size())
                .append(", children: ")
                .append(children().size())
                .append(" }")
                .toString();
    }
}
