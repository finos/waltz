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

import com.khartec.waltz.model.EntityReference;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static com.khartec.waltz.schema.Tables.CHANGE_LOG;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.post;


@Service
public class ChangeLogExtractor extends DirectQueryBasedDataExtractor {

    @Autowired
    public ChangeLogExtractor(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public void register() {
        registerExtractForApp( mkPath("data-extract", "change-log", ":kind", ":id"));
    }

    private void registerExtractForApp(String path) { post(path, (request, response) -> {

            EntityReference entityRef = getEntityReference(request);
            Condition condition = CHANGE_LOG.PARENT_ID.eq(entityRef.id())
                    .and(CHANGE_LOG.PARENT_KIND.eq(entityRef.kind().name()));

            SelectSeekStep1<Record4<String, String, String, Timestamp>, Timestamp> qry = dsl
                    .select(
                    CHANGE_LOG.SEVERITY.as("Severity"),
                    CHANGE_LOG.MESSAGE.as("Message"),
                    CHANGE_LOG.USER_ID.as("User"),
                    CHANGE_LOG.CREATED_AT.as("Timestamp")
                    )
                    .from(CHANGE_LOG)
                    .where(condition)
                    .orderBy(CHANGE_LOG.CREATED_AT.desc());

            return writeExtract(
                    "change-log-" + entityRef.id(),
                    qry,
                    request,
                    response);
        });
    }

}
