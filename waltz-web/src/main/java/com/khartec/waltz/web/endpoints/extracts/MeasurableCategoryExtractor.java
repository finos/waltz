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

package com.khartec.waltz.web.endpoints.extracts;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class MeasurableCategoryExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurableCategoryExtractor.class);


    @Autowired
    public MeasurableCategoryExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {

        String path = mkPath("data-extract", "measurable-category", ":id");
        get(path, (request, response) -> {
            long categoryId = getId(request);
            String categoryName = dsl
                    .select(MEASURABLE_CATEGORY.NAME)
                    .from(MEASURABLE_CATEGORY)
                    .where(MEASURABLE_CATEGORY.ID.eq(categoryId))
                    .fetchOne(MEASURABLE_CATEGORY.NAME);

            checkNotNull(categoryName, "category cannot be null");
            String suggestedFilename = categoryName
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-");

            SelectConditionStep<Record> data = dsl
                    .select(
                            MEASURABLE.ID.as("Id"),
                            MEASURABLE.PARENT_ID.as("Parent Id"),
                            MEASURABLE.EXTERNAL_ID.as("External Id"),
                            MEASURABLE.NAME.as("Name"),
                            MEASURABLE.DESCRIPTION.as("Description"))
                    .select(INVOLVEMENT_KIND.NAME.as("Role"))
                    .select(PERSON.DISPLAY_NAME.as("Person"),
                            PERSON.EMAIL.as("Email"))
                    .from(MEASURABLE)
                    .leftJoin(INVOLVEMENT)
                        .on(INVOLVEMENT.ENTITY_ID.eq(MEASURABLE.ID).and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                    .leftJoin(INVOLVEMENT_KIND)
                        .on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT.KIND_ID))
                    .leftJoin(PERSON)
                        .on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                    .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                    .and(MEASURABLE.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()));

            return writeExtract(
                    suggestedFilename,
                    data,
                    request,
                    response);
        });
    }

}
