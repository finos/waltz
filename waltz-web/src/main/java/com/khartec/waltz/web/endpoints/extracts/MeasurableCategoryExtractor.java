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
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
                    .replace(",", "-")
                    + ".csv";

            return writeFile(
                    suggestedFilename,
                    extract(categoryId),
                    response);
        });
    }


    private CSVSerializer extract(long categoryId) {
        return csvWriter -> {
            csvWriter.writeHeader(
                    "Id",
                    "Parent Id",
                    "External Id",
                    "Name",
                    "Description",
                    "Role",
                    "Person",
                    "Email");

            SelectConditionStep<Record> qry = dsl
                    .select(MEASURABLE.NAME, MEASURABLE.DESCRIPTION, MEASURABLE.ID, MEASURABLE.PARENT_ID, MEASURABLE.EXTERNAL_ID)
                    .select(INVOLVEMENT_KIND.NAME)
                    .select(PERSON.DISPLAY_NAME, PERSON.EMAIL)
                    .from(MEASURABLE)
                    .leftJoin(INVOLVEMENT)
                    .on(INVOLVEMENT.ENTITY_ID.eq(MEASURABLE.ID).and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                    .leftJoin(INVOLVEMENT_KIND)
                    .on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT.KIND_ID))
                    .leftJoin(PERSON)
                    .on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                    .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId));

            qry.fetch()
                    .forEach(r -> {
                        try {
                            csvWriter.write(
                                    r.get(MEASURABLE.ID),
                                    r.get(MEASURABLE.PARENT_ID),
                                    r.get(MEASURABLE.EXTERNAL_ID),
                                    r.get(MEASURABLE.NAME),
                                    r.get(MEASURABLE.DESCRIPTION),
                                    r.get(INVOLVEMENT_KIND.NAME),
                                    r.get(PERSON.DISPLAY_NAME),
                                    r.get(PERSON.EMAIL));
                        } catch (IOException ioe) {
                            LOG.warn("Failed to write row: " + r, ioe);
                        }
                    });
        };
    }

}
