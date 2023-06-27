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

package org.finos.waltz.integration_test.inmem.dao;

import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.*;
import org.finos.waltz.model.measurable_rating.ImmutableSaveMeasurableRatingCommand;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.MeasurableHelper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MeasurableRatingDaoTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private MeasurableRatingDao dao;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DSLContext dsl;


    @Test
    public void ratingsAreEmptyIfNoneAreAssociatedToAnApp() {
        List<MeasurableRating> ratingsForBrandNewEntity = dao.findForEntity(mkNewAppRef());
        assertTrue(ratingsForBrandNewEntity.isEmpty());
    }


    @Test
    public void ratingsCanBeSaved() {
        long categoryId = measurableHelper.createMeasurableCategory(mkName("mc"));
        long m1Id = measurableHelper.createMeasurable(mkName("m1"), categoryId);

        EntityReference appRef = mkNewAppRef();
        mkRatings(appRef, m1Id);

        assertEquals("Can find rating via app ref", 1, dao.findForEntity(appRef).size());
        assertTrue("Rating is only for the referenced app-ref", dao.findForEntity(mkNewAppRef()).isEmpty());
    }



    @Test
    public void readOnlyRatingsCannotBeSaved() {
        long categoryId = measurableHelper.createMeasurableCategory(mkName("mc"));
        long m1Id = measurableHelper.createMeasurable(mkName("m1"), categoryId);

        EntityReference appRef = mkNewAppRef();
        mkRatings(appRef, m1Id);

        // make the rating read-only
        dsl.update(MEASURABLE_RATING)
                .set(MEASURABLE_RATING.IS_READONLY, true)
                .where(MEASURABLE_RATING.ENTITY_ID.eq(appRef.id()))
                .and(MEASURABLE_RATING.MEASURABLE_ID.eq(m1Id))
                .execute();

        assertThrows(
                NotFoundException.class,
                () -> dao.save(
                    ImmutableSaveMeasurableRatingCommand
                        .builder()
                        .measurableId(m1Id)
                        .previousRating('G')
                        .rating('R')
                        .entityReference(appRef)
                        .lastUpdate(UserTimestamp.mkForUser(LAST_UPDATE_USER))
                        .build(),
                    false));

        assertEquals("Rating should not have changed",'G', first(dao.findForEntity(appRef)).rating());
    }


    @Test
    public void multipleRatingsCanBeSaved() {
        long categoryId = measurableHelper.createMeasurableCategory(mkName("mc"));
        long m1Id = measurableHelper.createMeasurable(mkName("m1"), categoryId);
        long m2Id = measurableHelper.createMeasurable(mkName("m2"), categoryId);

        EntityReference app1Ref = mkNewAppRef();
        mkRatings(app1Ref, m1Id, m2Id);

        EntityReference app2Ref = mkNewAppRef();
        mkRatings(app2Ref, m1Id);

        assertEquals("Can find rating via app ref", 2, dao.findForEntity(app1Ref).size());
        assertEquals(1, dao.findForEntity(app2Ref).size());
    }


    @Test
    public void multipleRatingsCanRetrievedBySelectors() {

        long categoryId = measurableHelper.createMeasurableCategory(mkName("mc"));
        long m1Id = measurableHelper.createMeasurable(mkName("m1"), categoryId);
        long m2Id = measurableHelper.createMeasurable(mkName("m2"), categoryId);

        EntityReference app1Ref = appHelper.createNewApp(mkName("a1"), null);
        mkRatings(app1Ref, m1Id, m2Id);

        EntityReference app2Ref = appHelper.createNewApp(mkName("a2"), null);
        mkRatings(app2Ref, m1Id);

        rebuildHierarchy(EntityKind.MEASURABLE);

        IdSelectionOptions catOpts = mkOpts(mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId), HierarchyQueryScope.EXACT);
        IdSelectionOptions m1Opts = mkOpts(mkRef(EntityKind.MEASURABLE, m1Id));
        IdSelectionOptions m2Opts = mkOpts(mkRef(EntityKind.MEASURABLE, m2Id));

        assertEquals(
                "Find by category selector gives everything",
                3,
                dao.findByMeasurableIdSelector(measurableIdSelectorFactory.apply(catOpts), catOpts).size());

        assertEquals(
                "Find by category id gives everything",
                3,
                dao.findByCategory(categoryId).size());

        assertEquals(
                "Find by specific measurable (m1) gives subset",
                2,
                dao.findByMeasurableIdSelector(measurableIdSelectorFactory.apply(m1Opts), m1Opts).size());

        assertEquals(
                "Find by specific measurable (m2) gives subset",
                1,
                dao.findByMeasurableIdSelector(measurableIdSelectorFactory.apply(m2Opts), m2Opts).size());
    }


    @Test
    public void ratingsCanBeBulkRemovedByCategoryForAGivenApp() {
        long categoryId = measurableHelper.createMeasurableCategory(mkName("mc"));
        long m1Id = measurableHelper.createMeasurable(mkName("m1"), categoryId);
        long m2Id = measurableHelper.createMeasurable(mkName("m2"), categoryId);

        EntityReference app1Ref = appHelper.createNewApp(mkName("a1"), null);
        mkRatings(app1Ref, m1Id, m2Id);

        assertEquals(
                "Expecting two ratings for app 1",
                2,
                dao.findForEntity(app1Ref).size());

        dao.removeForCategory(app1Ref, categoryId);

        assertTrue(
                "Expecting no ratings for app 1",
                dao.findForEntity(app1Ref).isEmpty());
    }


    private void mkRatings(EntityReference appRef, long... measurableIds) {
        for (long measurableId : measurableIds) {
            dao.save(ImmutableSaveMeasurableRatingCommand.builder()
                    .entityReference(appRef)
                    .measurableId(measurableId)
                    .rating('G')
                    .provenance(PROVENANCE)
                    .lastUpdate(UserTimestamp.mkForUser(LAST_UPDATE_USER))
                    .description("test")
                    .isPrimary(false)
                    .build(), false);
        }
    }

}