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
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.schema.tables.Application;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static java.lang.String.format;
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
        String findByParentEntityRefPath = mkPath("data-extract", "custom-environment", "kind", ":kind", "id", ":id");

        registerEnvironmentUsagesForParentRef(findByParentEntityRefPath);

    }


    private void registerEnvironmentUsagesForParentRef(String findByParentEntityRefPath) {
        get(findByParentEntityRefPath, (request, response) -> {

            EntityReference ref = getEntityReference(request);

            SelectConditionStep<Record> qry = getEnvironmentAndUsageInfoQuery(ref);

            return writeExtract(
                    mkFilename(ref),
                    qry,
                    request,
                    response);
        });
    }

    
    private SelectConditionStep<Record> getEnvironmentAndUsageInfoQuery(EntityReference ref) {

        Condition isServer = CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.SERVER_USAGE.name());
        Field<String> assetName = DSL
                .when(isServer, SERVER_INFORMATION.HOSTNAME)
                .otherwise(DATABASE_INFORMATION.DATABASE_NAME).as("asset_name");

        Field<String> applicationName = DSL
                .when(isServer, SERVER_OWNING_APP.NAME)
                .otherwise(DATABASE_OWNING_APP.NAME).as("application_name");
        
        Field<String> assetCode = DSL
                .when(isServer, SERVER_OWNING_APP.ASSET_CODE)
                .otherwise(DATABASE_OWNING_APP.ASSET_CODE).as("asset_code");

        SelectConditionStep<Record> qry = dsl
                .select(CUSTOM_ENVIRONMENT.GROUP_NAME,
                        CUSTOM_ENVIRONMENT.NAME,
                        CUSTOM_ENVIRONMENT.DESCRIPTION
                )
                .select(applicationName)
                .select(assetCode)
                .select(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID.as("associated_entity_id"),
                        CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.as("associated_entity_kind"))
                .select(assetName.as("associated_asset"))
                .select(CUSTOM_ENVIRONMENT_USAGE.CREATED_AT,
                        CUSTOM_ENVIRONMENT_USAGE.CREATED_BY,
                        CUSTOM_ENVIRONMENT_USAGE.PROVENANCE)
                .from(CUSTOM_ENVIRONMENT)
                .leftJoin(CUSTOM_ENVIRONMENT_USAGE)
                .on(CUSTOM_ENVIRONMENT_USAGE.CUSTOM_ENVIRONMENT_ID.eq(CUSTOM_ENVIRONMENT.ID))
                .leftJoin(SERVER_USAGE).on(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID.eq(SERVER_USAGE.ID)
                        .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.SERVER_USAGE.name())))
                .leftJoin(SERVER_INFORMATION).on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                .leftJoin(SERVER_OWNING_APP).on(SERVER_USAGE.ENTITY_ID.eq(SERVER_OWNING_APP.ID)
                        .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .leftJoin(DATABASE_INFORMATION).on(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID.eq(DATABASE_INFORMATION.ID)
                        .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.DATABASE.name())))
                .leftJoin(DATABASE_OWNING_APP).on(DATABASE_INFORMATION.ASSET_CODE.eq(DATABASE_OWNING_APP.ASSET_CODE))
                .where(CUSTOM_ENVIRONMENT.OWNING_ENTITY_KIND.eq(ref.kind().name())
                        .and(CUSTOM_ENVIRONMENT.OWNING_ENTITY_ID.eq(ref.id())));
        return qry;
    }


    private String mkFilename(EntityReference ref) {
        return format("%s-%d-custom-environment-usage-info", ref.kind(), ref.id());
    }

}
