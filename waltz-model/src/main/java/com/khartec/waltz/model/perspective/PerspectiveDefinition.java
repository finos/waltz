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

package com.khartec.waltz.model.perspective;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.RagNamesProvider;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.rating.RatingScheme;
import org.immutables.value.Value;

import java.util.List;


@Value.Immutable
@JsonSerialize(as = ImmutablePerspectiveDefinition.class)
@JsonDeserialize(as = ImmutablePerspectiveDefinition.class)
public abstract class PerspectiveDefinition implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        RagNamesProvider {

    public abstract long categoryX();

    public abstract long categoryY();

    public abstract long ratingSchemeId();

    @Value.Default
    @Deprecated
    public List<RagName> ragNames() {
        return RatingScheme.toList();
    }
}
