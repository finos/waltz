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

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.measurable_rating.MeasurableRatingDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.tally.MeasurableRatingTally;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.model.EntityKind.MEASURABLE;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.HierarchyQueryScope.CHILDREN;


public class MeasurableRatingHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        MeasurableRatingDao measurableRatingDao = ctx.getBean(MeasurableRatingDao.class);
        MeasurableIdSelectorFactory measurableIdSelectorFactory = ctx.getBean(MeasurableIdSelectorFactory.class);

        EntityReference direct = mkRef(MEASURABLE, 18310);
        EntityReference indirect = mkRef(MEASURABLE, 18064);

        IdSelectionOptions directOpts = IdSelectionOptions.mkOpts(direct, CHILDREN);
        IdSelectionOptions indirectOpts = IdSelectionOptions.mkOpts(indirect, CHILDREN);

        Select<Record1<Long>> directSelector = measurableIdSelectorFactory.apply(directOpts);
        Select<Record1<Long>> indirectSelector = measurableIdSelectorFactory.apply(indirectOpts);

        List<MeasurableRatingTally> directTallies = measurableRatingDao.statsForRelatedMeasurable(directSelector);
        List<MeasurableRatingTally> indirectTallies = measurableRatingDao.statsForRelatedMeasurable(indirectSelector);

        System.out.printf("Direct / %d\n", directTallies.size());
        System.out.printf("Indirect / %d\n", indirectTallies.size());
    }

}
