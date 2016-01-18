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

package com.khartec.waltz.model.authoritativesource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableAuthoritativeSource.class)
@JsonDeserialize(as = ImmutableAuthoritativeSource.class)
public abstract class AuthoritativeSource implements IdProvider {

    public abstract EntityReference applicationReference();
    public abstract EntityReference appOrgUnitReference();
    public abstract EntityReference parentReference();
    public abstract String dataType();
    public abstract Rating rating();


}
