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

import org.finos.waltz.data.changelog.ChangeLogDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.tables.ChangeLog;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record4;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.SelectSeekStep1;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static java.lang.String.format;
import static org.finos.waltz.schema.Tables.CHANGE_LOG;
import static org.finos.waltz.schema.Tables.PERSON;
import static spark.Spark.get;
import static spark.Spark.post;


@Service
public class ChangeLogExtractor extends DirectQueryBasedDataExtractor {

    @Autowired
    public ChangeLogExtractor(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public void register() {
        registerExtractForApp(WebUtilities.mkPath("data-extract", "change-log", ":kind", ":id"));
        registerExtractUnattestedChangesForApp(WebUtilities.mkPath("data-extract", "change-log", "unattested-changes", ":childKind", ":kind", ":id"));
    }


    private void registerExtractForApp(String path) {
        post(path, (request, response) -> {

            EntityReference entityRef = WebUtilities.getEntityReference(request);

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


    private void registerExtractUnattestedChangesForApp(String path) {
        get(path, (request, response) -> {

            EntityReference entityRef = WebUtilities.getEntityReference(request);
            EntityKind childKind = WebUtilities.getKind(request, "childKind");

            SelectJoinStep<Record4<String, String, String, Timestamp>> qry = mkUnattestedChangesQuery(entityRef, childKind);

            String filename = format("unattested-changes-%s-%d", childKind, entityRef.id());

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });
    }


    private SelectJoinStep<Record4<String, String, String, Timestamp>> mkUnattestedChangesQuery(EntityReference entityRef, EntityKind childKind) {
        org.finos.waltz.schema.tables.ChangeLog cl = org.finos.waltz.schema.tables.ChangeLog.CHANGE_LOG.as("cl");

        SelectConditionStep<Record> qry = ChangeLogDao.mkUnattestedChangesQuery(entityRef);

        SelectQuery<Record> selectQuery = dsl
                .selectQuery(qry
                        .and(cl.CHILD_KIND.eq(childKind.name())));

        return dsl
                .select(
                        selectQuery.field(cl.SEVERITY).as("Severity"),
                        selectQuery.field(cl.MESSAGE).as("Message"),
                        selectQuery.field(cl.USER_ID).as("User"),
                        selectQuery.field(cl.CREATED_AT).as("Timestamp"))
                .from(selectQuery);
    }

}
