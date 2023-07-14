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

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectSeekStep3;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static org.finos.waltz.schema.Tables.*;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.get;


@Service
public class CustomEnvironmentUsageExtractor extends DirectQueryBasedDataExtractor {

    private static final Application DATABASE_OWNING_APP = APPLICATION.as("database_owning_app");
    private static final Application SERVER_OWNING_APP = APPLICATION.as("server_owning_app");
    
    @Autowired
    public CustomEnvironmentUsageExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String findByParentEntityRefPath = WebUtilities.mkPath("data-extract", "custom-environment", "kind", ":kind", "id", ":id");

        registerEnvironmentUsagesForParentRef(findByParentEntityRefPath);

    }


    private void registerEnvironmentUsagesForParentRef(String findByParentEntityRefPath) {
        get(findByParentEntityRefPath, (request, response) -> {

            EntityReference ref = WebUtilities.getEntityReference(request);

            SelectSeekStep3<Record, String, String, String> serverUsagesQuery = getServerUsagesQuery(ref);
            SelectSeekStep3<Record, String, String, String> databaseUsagesQuery = getDatabaseUsagesQuery(ref);

            return writeAsMultiSheetExcel(
                    dsl,
                    mkFilename(ref),
                    response,
                    tuple("Instructions", mkInstructions()),
                    tuple("Servers", serverUsagesQuery),
                    tuple("Databases", databaseUsagesQuery));
        });
    }


    private SelectSelectStep<Record1<String>> mkInstructions() {
        String instructions = "Separate tabs have been created for servers and databases associated to custom environments. Extracted on: ";
        return dsl
                .select(DSL.concat(
                        DSL.val(instructions),
                        DSL.currentLocalDateTime()).as("instructions"));
    }

    
    private SelectSeekStep3<Record, String, String, String> getServerUsagesQuery(EntityReference ref) {

        SelectSeekStep3<Record, String, String, String> qry = dsl
                .select(CUSTOM_ENVIRONMENT.GROUP_NAME,
                        CUSTOM_ENVIRONMENT.NAME.as("environment_name"),
                        CUSTOM_ENVIRONMENT.DESCRIPTION.as("environment_description"))
                .select(SERVER_OWNING_APP.NAME.as("application_name"))
                .select(SERVER_OWNING_APP.ASSET_CODE.as("asset_code"))
                .select(SERVER_INFORMATION.HOSTNAME,
                        SERVER_INFORMATION.EXTERNAL_ID.as("server_external_id"),
                        SERVER_INFORMATION.OPERATING_SYSTEM,
                        SERVER_INFORMATION.OPERATING_SYSTEM_VERSION,
                        SERVER_INFORMATION.COUNTRY,
                        SERVER_INFORMATION.LOCATION,
                        SERVER_INFORMATION.HW_END_OF_LIFE_DATE,
                        SERVER_INFORMATION.OS_END_OF_LIFE_DATE,
                        SERVER_INFORMATION.IS_VIRTUAL)
                .select(CUSTOM_ENVIRONMENT_USAGE.CREATED_AT,
                        CUSTOM_ENVIRONMENT_USAGE.CREATED_BY,
                        CUSTOM_ENVIRONMENT_USAGE.PROVENANCE)
                .from(CUSTOM_ENVIRONMENT)
                .leftJoin(CUSTOM_ENVIRONMENT_USAGE).on(CUSTOM_ENVIRONMENT_USAGE.CUSTOM_ENVIRONMENT_ID.eq(CUSTOM_ENVIRONMENT.ID))
                .leftJoin(SERVER_USAGE).on(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID.eq(SERVER_USAGE.ID)
                        .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.SERVER_USAGE.name())))
                .leftJoin(SERVER_INFORMATION).on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                .leftJoin(SERVER_OWNING_APP).on(SERVER_USAGE.ENTITY_ID.eq(SERVER_OWNING_APP.ID)
                        .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(CUSTOM_ENVIRONMENT.OWNING_ENTITY_KIND.eq(ref.kind().name())
                        .and(CUSTOM_ENVIRONMENT.OWNING_ENTITY_ID.eq(ref.id()))
                .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.SERVER_USAGE.name())))
                .orderBy(CUSTOM_ENVIRONMENT.GROUP_NAME, CUSTOM_ENVIRONMENT.NAME, SERVER_INFORMATION.HOSTNAME);
        return qry;
    }


    private SelectSeekStep3<Record, String, String, String> getDatabaseUsagesQuery(EntityReference ref) {

        SelectSeekStep3<Record, String, String, String> qry = dsl
                .select(CUSTOM_ENVIRONMENT.GROUP_NAME,
                        CUSTOM_ENVIRONMENT.NAME.as("environment_name"),
                        CUSTOM_ENVIRONMENT.DESCRIPTION.as("environment_description"))
                .select(DATABASE_OWNING_APP.NAME.as("application_name"))
                .select(DATABASE_OWNING_APP.ASSET_CODE.as("asset_code"))
                .select(DATABASE_INFORMATION.DATABASE_NAME,
                        DATABASE_INFORMATION.INSTANCE_NAME,
                        DATABASE_INFORMATION.EXTERNAL_ID.as("database_external_id"),
                        DATABASE_INFORMATION.DBMS_VENDOR,
                        DATABASE_INFORMATION.DBMS_NAME,
                        DATABASE_INFORMATION.DBMS_VERSION,
                        DATABASE_INFORMATION.END_OF_LIFE_DATE)
                .select(CUSTOM_ENVIRONMENT_USAGE.CREATED_AT,
                        CUSTOM_ENVIRONMENT_USAGE.CREATED_BY,
                        CUSTOM_ENVIRONMENT_USAGE.PROVENANCE)
                .from(CUSTOM_ENVIRONMENT)
                .leftJoin(CUSTOM_ENVIRONMENT_USAGE).on(CUSTOM_ENVIRONMENT_USAGE.CUSTOM_ENVIRONMENT_ID.eq(CUSTOM_ENVIRONMENT.ID))
                .leftJoin(DATABASE_INFORMATION).on(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID.eq(DATABASE_INFORMATION.ID)
                        .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.DATABASE.name())))
                .leftJoin(DATABASE_USAGE).on(DATABASE_USAGE.DATABASE_ID.eq(DATABASE_INFORMATION.ID)
                        .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.DATABASE.name())))
                .leftJoin(DATABASE_OWNING_APP).on(DATABASE_USAGE.ENTITY_ID.eq(DATABASE_OWNING_APP.ID)
                        .and(DATABASE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(CUSTOM_ENVIRONMENT.OWNING_ENTITY_KIND.eq(ref.kind().name())
                        .and(CUSTOM_ENVIRONMENT.OWNING_ENTITY_ID.eq(ref.id()))
                .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.DATABASE.name())))
                .orderBy(CUSTOM_ENVIRONMENT.GROUP_NAME, CUSTOM_ENVIRONMENT.NAME, DATABASE_INFORMATION.DATABASE_NAME);
        return qry;
    }


    private String mkFilename(EntityReference ref) {
        return format("%s-%d-custom-environment-usage-info", ref.kind(), ref.id());
    }

}
