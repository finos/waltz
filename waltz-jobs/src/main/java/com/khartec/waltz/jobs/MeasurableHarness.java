/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;


public class MeasurableHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        MeasurableIdSelectorFactory factory = ctx.getBean(MeasurableIdSelectorFactory.class);
        ApplicationService applicationService = ctx.getBean(ApplicationService.class);
        MeasurableRatingService measurableRatingService = ctx.getBean(MeasurableRatingService.class);
        MeasurableService measurableService = ctx.getBean(MeasurableService.class);

        EntityReference ref = mkRef(
                EntityKind.MEASURABLE,
                481);

        IdSelectionOptions options = mkOpts(
                ref,
                HierarchyQueryScope.CHILDREN);

        Select<Record1<Long>> selector = factory.apply(options);

        System.out.println(selector);

        List<Application> apps = applicationService.findByAppIdSelector(options);
        apps.forEach(System.out::println);

        List<MeasurableRating> ratings = measurableRatingService.findByMeasurableIdSelector(options);
        ratings.forEach(System.out::println);

        List<Measurable> measurables = measurableService.findByMeasurableIdSelector(mkOpts(ref, HierarchyQueryScope.PARENTS));
        measurables.forEach(System.out::println);


    }

}
