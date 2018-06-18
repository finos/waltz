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

import com.khartec.waltz.common.DebugUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.measurable_rating.MeasurableRatingDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class MeasurableRatingHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        MeasurableRatingDao measurableRatingDao = ctx.getBean(MeasurableRatingDao.class);
        MeasurableIdSelectorFactory measurableIdSelectorFactory = ctx.getBean(MeasurableIdSelectorFactory.class);
        ApplicationIdSelectorFactory applicationIdSelectorFactory = ctx.getBean(ApplicationIdSelectorFactory.class);

        IdSelectionOptions mOptions = IdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.MEASURABLE, 1188L), HierarchyQueryScope.CHILDREN);
        Select<Record1<Long>> measurableSelector = measurableIdSelectorFactory.apply(mOptions);
        Select<Record1<Long>> applicationSelector = applicationIdSelectorFactory.apply(mOptions);

        Set<EntityLifecycleStatus> allStatuses = SetUtilities.fromArray(EntityLifecycleStatus.values());
        List<MeasurableRating> ratings = measurableRatingDao.findByMeasurableIdSelector(measurableSelector, mOptions.entityLifecycleStatuses()); //allStatuses);
//        List<MeasurableRating> ratings = measurableRatingDao.findForEntity(EntityReference.mkRef(EntityKind.ACTOR, 15));
//        Collection<MeasurableRating> ratings = measurableRatingDao.findByCategory(7L);
//        Collection<MeasurableRating> ratings = measurableRatingDao.findByApplicationIdSelector(applicationSelector);

        Map<EntityLifecycleStatus, Long> byStatus = MapUtilities.countBy(r -> r.entityReference().entityLifecycleStatus(), ratings);
        Map<EntityKind, Long> byKind = MapUtilities.countBy(r -> r.entityReference().kind(), ratings);
        DebugUtilities.dump(byStatus);
        System.out.println("------");
        DebugUtilities.dump(byKind);

        ratings.stream()
                .filter(r -> r.entityReference().kind().equals(EntityKind.ACTOR))
                .forEach(r -> System.out.println(r.entityReference().name()));
        ratings.stream()
                .filter(r -> r.entityReference().entityLifecycleStatus().equals(EntityLifecycleStatus.REMOVED))
                .forEach(r -> System.out.println(r.entityReference().name()));
    }

}
