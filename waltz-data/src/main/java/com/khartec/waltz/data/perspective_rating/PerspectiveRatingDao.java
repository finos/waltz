/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.perspective_rating;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.perspective.ImmutablePerspectiveRating;
import com.khartec.waltz.model.perspective.PerspectiveRating;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.schema.tables.records.PerspectiveRatingRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.PerspectiveRating.PERSPECTIVE_RATING;

/**
 * Created by dwatkins on 01/02/2017.
 */
@Repository
public class PerspectiveRatingDao {

    private static final RecordMapper<PerspectiveRatingRecord, PerspectiveRating> TO_DOMAIN_MAPPER = r ->
            ImmutablePerspectiveRating.builder()
                    .entityReference(mkRef(
                            EntityKind.valueOf(r.getEntityKind()),
                            r.getEntityId()))
                    .categoryX(r.getCategoryX())
                    .categoryY(r.getCategoryY())
                    .measurableX(r.getMeasurableX())
                    .measurableY(r.getMeasurableY())
                    .rating(RagRating.valueOf(r.getRating()))
                    .lastUpdatedBy(r.getLastUpdatedBy())
                    .lastUpdatedAt(r.getLastUpdatedAt().toLocalDateTime())
                    .build();


    private final DSLContext dsl;


    @Autowired
    public PerspectiveRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PerspectiveRating> findForEntity(
            long categoryX,
            long categoryY,
            EntityReference ref) {

        Condition condition = PERSPECTIVE_RATING.CATEGORY_X.eq(categoryX)
                .and(PERSPECTIVE_RATING.CATEGORY_Y.eq(categoryY))
                .and(PERSPECTIVE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(PERSPECTIVE_RATING.ENTITY_ID.eq(ref.id()));

        return dsl.selectFrom(PERSPECTIVE_RATING)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }

}
