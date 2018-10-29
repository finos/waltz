/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.function.Supplier;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.*;

/**
 * This application will remove a measurable and all associated data.
 * Modify the program to :
 *    - specify a `category external id`
 *    - (after testing) remove the explicitly thrown exception at the end to commit the tx
 */
public class RemoveTaxonomy {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveTaxonomy.class);

    public static void main(String[] args) {
        final String categoryExtId = "FUNCTION_3_1";

        LOG.debug("Starting removal process for taxonomy {}", categoryExtId);

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ctx.getBean(DSLContext.class).transaction(tx -> {

            DSLContext dsl = DSL.using(tx);
            Long categoryId = dsl
                    .select(MEASURABLE_CATEGORY.ID)
                    .from(MEASURABLE_CATEGORY)
                    .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(categoryExtId))
                    .fetchOne(MEASURABLE_CATEGORY.ID);

            if (categoryId == null) {
                LOG.error("Could not find taxonomy with external id: {}", categoryExtId);
                return;
            }

            MeasurableIdSelectorFactory selectorFactory = ctx.getBean(MeasurableIdSelectorFactory.class);
            Select<Record1<Long>> measurableIdSelector = selectorFactory
                    .apply(mkOpts(
                        mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId),
                        HierarchyQueryScope.EXACT));

            removeAssociatedRatings(dsl, measurableIdSelector);
            removeBookmarks(dsl, measurableIdSelector);
            removeEntityRelationships(dsl, measurableIdSelector);
            removeFlowDiagramLinks(dsl, measurableIdSelector);
            removeEntitySvgDiagram(dsl, measurableIdSelector);
            removeInvolvements(dsl, measurableIdSelector);
            removeRatingScheme(dsl, categoryId);
            removeMeasurables(dsl, measurableIdSelector);
            removeCategory(dsl, categoryId);

            throw new IllegalArgumentException("Aborting, comment this line if you really mean to execute this removal");
         });
    }


    private static void removeCategory(DSLContext dsl, Long categoryId) {
        doDelete("Measurable Category", () -> dsl
                .deleteFrom(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.ID.eq(categoryId))
                .execute());
    }


    private static void removeMeasurables(DSLContext dsl, Select<Record1<Long>> measurableIdSelector) {
        doDelete("Measurables", () -> dsl
                .deleteFrom(MEASURABLE)
                .where(MEASURABLE.ID.in(measurableIdSelector))
                .execute());
    }


    private static void removeRatingScheme(DSLContext dsl, Long categoryId) {
        doDelete("Rating Scheme", () -> {
            Long ratingSchemeId = dsl.select(MEASURABLE_CATEGORY.RATING_SCHEME_ID)
                    .from(MEASURABLE_CATEGORY)
                    .where(MEASURABLE_CATEGORY.ID.eq(categoryId))
                    .fetchOne(MEASURABLE_CATEGORY.RATING_SCHEME_ID);

            int usageCount = dsl.fetchCount(dsl
                    .select()
                    .from(MEASURABLE_CATEGORY)
                    .where(MEASURABLE_CATEGORY.RATING_SCHEME_ID.eq(ratingSchemeId)));

            if (usageCount > 1) {
                LOG.debug("Leaving rating scheme ({}) untouched as has other usages {}", ratingSchemeId, usageCount);
                return 0;
            }

            doDelete("Rating Scheme Items", () -> dsl
                    .deleteFrom(RATING_SCHEME_ITEM)
                    .where(RATING_SCHEME_ITEM.SCHEME_ID.eq(ratingSchemeId))
                    .execute());

            return dsl
                    .deleteFrom(RATING_SCHEME)
                    .where(RATING_SCHEME.ID.eq(ratingSchemeId))
                    .execute();
        });
    }


    private static void removeInvolvements(DSLContext dsl, Select<Record1<Long>> measurableIdSelector) {
        doDelete("Involvements", () -> dsl
                .deleteFrom(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.MEASURABLE.name()))
                .and(INVOLVEMENT.ENTITY_ID.in(measurableIdSelector))
                .execute());
    }


    private static void removeEntitySvgDiagram(DSLContext dsl, Select<Record1<Long>> measurableIdSelector) {
        doDelete("Entity SVG Diagrams", () -> dsl
                .deleteFrom(ENTITY_SVG_DIAGRAM)
                .where(ENTITY_SVG_DIAGRAM.ENTITY_KIND.eq(EntityKind.MEASURABLE.name()))
                .and(ENTITY_SVG_DIAGRAM.ENTITY_ID.in(measurableIdSelector))
                .execute());
    }


    private static void removeFlowDiagramLinks(DSLContext dsl, Select<Record1<Long>> measurableIdSelector) {
        doDelete("Flow Diagram References", () -> dsl
                .deleteFrom(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.MEASURABLE.name()))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_ID.in(measurableIdSelector))
                .execute());
    }


    private static void removeEntityRelationships(DSLContext dsl, Select<Record1<Long>> measurableIdSelector) {
        doDelete("Entity Relationships", () ->  dsl
                .deleteFrom(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.MEASURABLE.name())
                        .and(ENTITY_RELATIONSHIP.ID_A.in(measurableIdSelector)))
                .or(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.MEASURABLE.name())
                        .and(ENTITY_RELATIONSHIP.ID_B.in((measurableIdSelector))))
                .execute());
    }


    private static void removeAssociatedRatings(DSLContext dsl, Select<Record1<Long>> measurableIdSelector) {
        doDelete("Measurable Ratings", () ->  dsl
                .deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.MEASURABLE_ID.in(measurableIdSelector))
                .execute());
    }


    private static void removeBookmarks(DSLContext dsl, Select<Record1<Long>> measurableIdSelector) {
        doDelete("Bookmarks", () -> dsl
                .deleteFrom(BOOKMARK)
                .where(BOOKMARK.PARENT_KIND.eq(EntityKind.MEASURABLE.name()))
                .and(BOOKMARK.PARENT_ID.in(measurableIdSelector))
                .execute());
    }


    private static void doDelete(String name, Supplier<Integer> fn) {
        Integer count = fn.get();
        LOG.info("{}: Deleted - {} rows", name, count);
    }
}
