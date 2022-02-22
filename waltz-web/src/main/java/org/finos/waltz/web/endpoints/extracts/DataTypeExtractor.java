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
import org.jooq.Record7;
import org.jooq.SelectJoinStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.schema.Tables.DATA_TYPE;
import static spark.Spark.get;


@Service
public class DataTypeExtractor extends DirectQueryBasedDataExtractor {


    @Autowired
    public DataTypeExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        get(WebUtilities.mkPath("data-extract", "data-types"), (request, response) ->
                writeExtract(
                        "data-types",
                        prepareExtract(),
                        request,
                        response));
    }


    private SelectJoinStep<Record7<Long, Long, String, Boolean, String, Boolean, String>> prepareExtract() {
        return dsl
                .select(
                    DATA_TYPE.ID.as("id"),
                    DATA_TYPE.PARENT_ID.as("parentId"),
                    DATA_TYPE.NAME.as("name"),
                    DATA_TYPE.DEPRECATED.as("deprecated"),
                    DATA_TYPE.CODE.as("externalId"),
                    DATA_TYPE.CONCRETE.as("concrete"),
                    DATA_TYPE.DESCRIPTION.as("description"))
                .from(DATA_TYPE);
    }

}
