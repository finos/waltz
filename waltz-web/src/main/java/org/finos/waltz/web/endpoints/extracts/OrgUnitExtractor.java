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

package org.finos.waltz.web.endpoints.extracts;


import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static spark.Spark.get;


@Service
public class OrgUnitExtractor extends DirectQueryBasedDataExtractor
        implements SupportsJsonExtraction {


    @Autowired
    public OrgUnitExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        get(WebUtilities.mkPath("data-extract", "org-units"), (request, response) ->
                writeExtract(
                        "organisational-units",
                        prepareExtract(),
                        request,
                        response));
    }


    private SelectJoinStep<Record6<Long, Long, String, String, String, String>> prepareExtract() {
        return dsl
                .select(
                        ORGANISATIONAL_UNIT.ID.as("id"),
                        ORGANISATIONAL_UNIT.PARENT_ID.as("parentId"),
                        ORGANISATIONAL_UNIT.NAME.as("name"),
                        ORGANISATIONAL_UNIT.DESCRIPTION.as("description"),
                        ORGANISATIONAL_UNIT.EXTERNAL_ID.as("externalId"),
                        ORGANISATIONAL_UNIT.PROVENANCE.as("provenance"))
                .from(ORGANISATIONAL_UNIT);
    }

}
