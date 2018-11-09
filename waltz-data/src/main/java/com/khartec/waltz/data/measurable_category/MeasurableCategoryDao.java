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

package com.khartec.waltz.data.measurable_category;

import com.khartec.waltz.model.measurable_category.ImmutableMeasurableCategory;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.model.rating.RatingScheme;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;


@Repository
public class MeasurableCategoryDao {

    private static final RecordMapper<MeasurableCategoryRecord, MeasurableCategory> TO_DOMAIN_MAPPER = r -> {

        return ImmutableMeasurableCategory.builder()
                .ratingSchemeId(r.getRatingSchemeId())
                .id(r.getId())
                .name(r.getName())
                .externalId(r.getExternalId())
                .description(r.getDescription())
                .lastUpdatedBy(r.getLastUpdatedBy())
                .lastUpdatedAt(r.getLastUpdatedAt().toLocalDateTime())
                .editable(r.getEditable())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public MeasurableCategoryDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Collection<MeasurableCategory> findAll() {
        return dsl
                .selectFrom(MEASURABLE_CATEGORY)
                .orderBy(MEASURABLE_CATEGORY.NAME)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public MeasurableCategory getById(long id) {
        return dsl
                .selectFrom(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

}
