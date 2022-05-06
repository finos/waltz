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

import org.jooq.DSLContext;
import org.jooq.Record8;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static org.finos.waltz.schema.Tables.ACTOR;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class ActorExtractor extends DirectQueryBasedDataExtractor {


    @Autowired
    public ActorExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {

        String findAllPath = mkPath("data-extract", "actor", "all");

        get(findAllPath, (request, response) -> {

            SelectJoinStep<Record8<Long, String, String, String, String, Timestamp, String, String>> qry = prepareExtractQuery();

            return writeExtract("actors", qry, request, response);
        });
    }


    private SelectJoinStep<Record8<Long, String, String, String, String, Timestamp, String, String>> prepareExtractQuery() {
        return dsl
                .select(ACTOR.ID.as("Waltz ID"),
                        ACTOR.NAME.as("Name"),
                        ACTOR.DESCRIPTION.as("Description"),
                        DSL.when(ACTOR.IS_EXTERNAL.isTrue(), "External").otherwise("Internal").as("External/Internal"),
                        ACTOR.EXTERNAL_ID.as("External ID"),
                        ACTOR.LAST_UPDATED_AT.as("Last Updated At"),
                        ACTOR.LAST_UPDATED_BY.as("Last Updated By"),
                        ACTOR.PROVENANCE.as("Provenance"))
                .from(ACTOR);
    }
}
