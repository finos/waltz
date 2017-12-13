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

package com.khartec.waltz.data.tour;

import com.khartec.waltz.model.tour.ImmutableTourStep;
import com.khartec.waltz.model.tour.TourStep;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Tour.TOUR;

/**
 * Created by dwatkins on 20/09/2016.
 */
@Repository
public class TourDao {

    private static final RecordMapper<Record, TourStep> TO_DOMAIN_MAPPER = r -> ImmutableTourStep.builder()
            .key(r.getValue(TOUR.TOUR_KEY))
            .id(r.getValue(TOUR.STEP_ID))
            .position(r.getValue(TOUR.POSITION))
            .description(r.getValue(TOUR.DESCRIPTION))
            .selector(r.getValue(TOUR.SELECTOR))
            .build();


    private final DSLContext dsl;


    @Autowired
    public TourDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<TourStep> findByKey(String key) {
        return dsl.select(TOUR.fields())
                .from(TOUR)
                .where(TOUR.TOUR_KEY.eq(key))
                .orderBy(TOUR.STEP_ID)
                .fetch(TO_DOMAIN_MAPPER);
    }

}
