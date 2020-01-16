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

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class PersonApplicationExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(PersonApplicationExtractor.class);


    public PersonApplicationExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "application", "person", ":empId");
        get(path, (request, response) -> {
            String empId = request.params("empId");

            String personName = dsl
                    .select(PERSON.DISPLAY_NAME)
                    .from(PERSON)
                    .where(PERSON.EMPLOYEE_ID.eq(empId))
                    .fetchOne(PERSON.DISPLAY_NAME);

            checkNotNull(personName, "Cannot find person with empId: %s", empId);

            String suggestedFilename = "Applications-associated-with-" + personName
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-");

            return writeExtract(
                    suggestedFilename,
                    prepareExtractQuery(empId),
                    request,
                    response);
        });
    }


    private SelectConditionStep<Record9<Long, String, String, String, String, String, String, String, String>> prepareExtractQuery(String empId) {

        Condition isDirect = DSL.exists(DSL
                .select(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.EMPLOYEE_ID.eq(empId)
                        .and(INVOLVEMENT.ENTITY_ID.eq(APPLICATION.ID))));

        Field<String> directOrOversightField = DSL
                .when(isDirect, "Direct")
                .otherwise("Oversight");

        return dsl
                .selectDistinct(
                        APPLICATION.ID.as("Waltz Id"),
                        APPLICATION.NAME.as("Name"),
                        APPLICATION.ASSET_CODE.as("Asset Code"),
                        ORGANISATIONAL_UNIT.NAME.as("Org Unit"),
                        APPLICATION.KIND.as("Application Kind"),
                        APPLICATION.OVERALL_RATING.as("Overall Rating"),
                        APPLICATION.BUSINESS_CRITICALITY.as("Business Criticality"),
                        APPLICATION.LIFECYCLE_PHASE.as("Lifecycle Phase"),
                        directOrOversightField.as("Direct or Oversight"))
                .from(APPLICATION)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.eq(APPLICATION.ID)
                        .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                .innerJoin(PERSON_HIERARCHY)
                .on(PERSON_HIERARCHY.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(empId)
                        .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.notEqual(EntityLifecycleStatus.REMOVED.name())));

    }
}
