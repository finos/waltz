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
import com.khartec.waltz.schema.tables.ChangeLog;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static com.khartec.waltz.schema.Tables.CHANGE_LOG;
import static com.khartec.waltz.schema.Tables.PERSON;
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
        registerExtractForApp(mkPath("data-extract", "change-log", ":kind", ":id"));
    }


    private void registerExtractForApp(String path) {
        post(path, (request, response) -> {

            EntityReference entityRef = getEntityReference(request);

            Select<Record> select = mkQuery(entityRef);

            SelectSeekStep1<Record4<String, String, String, Timestamp>, Timestamp> qry = dsl
                    .select(select.field(CHANGE_LOG.SEVERITY).as("Severity"),
                            select.field(CHANGE_LOG.MESSAGE).as("Message"),
                            select.field(CHANGE_LOG.USER_ID).as("User"),
                            select.field(CHANGE_LOG.CREATED_AT).as("Timestamp"))
                    .from(select)
                    .orderBy(select.field(CHANGE_LOG.CREATED_AT).desc());

            return writeExtract(
                    "change-log-" + entityRef.id(),
                    qry,
                    request,
                    response);
        });
    }


    private Select<Record> mkQuery(EntityReference entityRef) {
        Select<Record> byParentRef = DSL
                .select(CHANGE_LOG.fields())
                .from(ChangeLog.CHANGE_LOG)
                .where(ChangeLog.CHANGE_LOG.PARENT_ID.eq(entityRef.id()))
                .and(ChangeLog.CHANGE_LOG.PARENT_KIND.eq(entityRef.kind().name()));

        switch (entityRef.kind()) {
            case PERSON:
                SelectConditionStep<Record> byUserId = DSL
                        .select(CHANGE_LOG.fields())
                        .from(ChangeLog.CHANGE_LOG)
                        .innerJoin(PERSON).on(PERSON.EMAIL.eq(ChangeLog.CHANGE_LOG.USER_ID))
                        .where(PERSON.ID.eq(entityRef.id()));
                return byParentRef.unionAll(byUserId);
            default:
                return byParentRef;
        }
    }

}
