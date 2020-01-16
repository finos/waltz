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

import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.DATABASE_INFORMATION;
import static com.khartec.waltz.schema.Tables.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class TechnologyEOLDatabaseExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyEOLDatabaseExtractor.class);
    private final EntityReferenceNameResolver nameResolver;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

    @Autowired
    public TechnologyEOLDatabaseExtractor(DSLContext dsl,
                                          EntityReferenceNameResolver nameResolver) {
        super(dsl);
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(applicationIdSelectorFactory, "appIdSelectorFactory cannot be null");
        this.nameResolver = nameResolver;
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "technology-database", ":kind", ":id");
        get(path, (request, response) -> {
            EntityReference ref = getReference(request);
            Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(mkOpts(ref));

            SelectConditionStep<Record> qry = dsl
                    .selectDistinct(ORGANISATIONAL_UNIT.NAME.as("Org Unit"))
                    .select(APPLICATION.NAME.as("Application Name"), APPLICATION.ASSET_CODE.as("Asset Code"))
                    .select(DATABASE_INFORMATION.DATABASE_NAME.as("Database Name"),
                            DATABASE_INFORMATION.INSTANCE_NAME.as("Instance Name"),
                            DATABASE_INFORMATION.ENVIRONMENT.as("DBMS Environment"),
                            DATABASE_INFORMATION.DBMS_VENDOR.as("DBMS Vendor"),
                            DATABASE_INFORMATION.DBMS_NAME.as("DBMS Name"),
                            DATABASE_INFORMATION.END_OF_LIFE_DATE.as("End of Life date"),
                            DATABASE_INFORMATION.LIFECYCLE_STATUS.as("Lifecycle"))
                    .from(DATABASE_INFORMATION)
                    .join(APPLICATION)
                    .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                    .join(ORGANISATIONAL_UNIT)
                    .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                    .where(APPLICATION.ID.in(appIdSelector))
                    .and(APPLICATION.LIFECYCLE_PHASE.notEqual("RETIRED"));

            return writeExtract(
                    mkFilename(ref),
                    qry,
                    request,
                    response);
        });
    }


    private EntityReference getReference(Request request) {
        EntityReference origRef = getEntityReference(request);
        return nameResolver
                .resolve(origRef)
                .orElse(origRef);
    }


    private String mkFilename(EntityReference ref) {
        return sanitizeName(ref.name().orElse(ref.kind().name()))
                + "-technology-database-eol";
    }


    private String sanitizeName(String str) {
        return str
                .replace(".", "-")
                .replace(" ", "-")
                .replace(",", "-");

    }
}
