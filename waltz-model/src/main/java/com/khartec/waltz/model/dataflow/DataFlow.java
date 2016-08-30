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

package com.khartec.waltz.model.dataflow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableDataFlow.class)
@JsonDeserialize(as = ImmutableDataFlow.class)
public abstract class DataFlow implements IdProvider, ProvenanceProvider {

    public abstract EntityReference source();
    public abstract EntityReference target();

    @Value.Default
    public String provenance() {
        return "waltz";
    }

}
