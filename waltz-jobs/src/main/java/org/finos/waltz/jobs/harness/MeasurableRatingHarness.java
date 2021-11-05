/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.jobs.harness;

import org.finos.waltz.data.measurable.MeasurableIdSelectorFactory;
import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.tally.MeasurableRatingTally;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.finos.waltz.model.EntityKind.MEASURABLE;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.HierarchyQueryScope.CHILDREN;


public class MeasurableRatingHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        MeasurableRatingDao measurableRatingDao = ctx.getBean(MeasurableRatingDao.class);
        MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();

        EntityReference direct = mkRef(MEASURABLE, 18310);
        EntityReference indirect = mkRef(MEASURABLE, 18064);

        IdSelectionOptions directOpts = IdSelectionOptions.mkOpts(direct, CHILDREN);
        IdSelectionOptions indirectOpts = IdSelectionOptions.mkOpts(indirect, CHILDREN);

        Select<Record1<Long>> directSelector = measurableIdSelectorFactory.apply(directOpts);
        Select<Record1<Long>> indirectSelector = measurableIdSelectorFactory.apply(indirectOpts);

        List<MeasurableRatingTally> directTallies = measurableRatingDao.statsForRelatedMeasurable(directSelector);
        List<MeasurableRatingTally> indirectTallies = measurableRatingDao.statsForRelatedMeasurable(indirectSelector);


        List<Tally<Long>> tallies = measurableRatingDao.tallyByMeasurableCategoryId(1L);
        System.out.println(tallies);
    }

}
