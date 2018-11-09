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

package com.khartec.waltz.model.measurable_category;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.rating.RatingScheme;
import org.immutables.value.Value;

import java.util.List;

/**
 * A measurable category represents a classifier for a hierarchy of
 * {@link com.khartec.waltz.model.measurable.Measurable} items.
 * Together they can be thought of as a taxonomy.  Common categories include:
 *
 * <ul>
 *     <li>Service</li>
 *     <li>Function</li>
 *     <li>Product</li>
 * </ul>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableCategory.class)
@JsonDeserialize(as = ImmutableMeasurableCategory.class)
public abstract class MeasurableCategory implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ExternalIdProvider,
        LastUpdatedProvider,
        RagNamesProvider,
        EntityKindProvider {


    @Value.Default
    public EntityKind kind() { return EntityKind.MEASURABLE_CATEGORY; }

    /**
     * Indicates if the measurables in the category may be edited from within the tool.
     * This should only be enabled for taxonomies which are entirely managed from within
     * Waltz.
     *
     * @return true if this measurables in this category can be edited
     */
    @Value.Default
    public boolean editable() {
        return false;
    }

    /**
     * A category is linked to a Rating Scheme which provides a mechanism to describe
     * application alignments to this category.  These schemes are typically variants
     * of RAG ratings, or investment ratings.
     *
     * @return id which links to a {@link RatingScheme}
     */
    public abstract long ratingSchemeId();

}
