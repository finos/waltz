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

import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.ORGANISATIONAL_UNIT;
import static org.finos.waltz.schema.Tables.SERVER_INFORMATION;
import static org.finos.waltz.schema.Tables.SERVER_USAGE;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static spark.Spark.get;


@Service
public class TechnologyEOLServerExtractor extends DirectQueryBasedDataExtractor {

    private final EntityReferenceNameResolver nameResolver;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public TechnologyEOLServerExtractor(DSLContext dsl,
                                        EntityReferenceNameResolver nameResolver) {
        super(dsl);
        checkNotNull(nameResolver, "nameResolver cannot be null");
        this.nameResolver = nameResolver;
    }


    @Override
    public void register() {
        String path = WebUtilities.mkPath("data-extract", "technology-server", ":kind", ":id");
        get(path, (request, response) -> {
            EntityReference ref = getReference(request);
            Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(mkOpts(ref));

            SelectConditionStep<Record> qry = dsl
                    .selectDistinct(ORGANISATIONAL_UNIT.NAME.as("Org Unit"))
                    .select(APPLICATION.NAME.as("Application Name"), APPLICATION.ASSET_CODE.as("Asset Code"))
                    .select(SERVER_INFORMATION.HOSTNAME.as("Host Name"),
                            SERVER_USAGE.ENVIRONMENT.as("Environment"),
                            SERVER_INFORMATION.COUNTRY.as("Country"),
                            SERVER_INFORMATION.LOCATION.as("Location"),
                            SERVER_INFORMATION.OPERATING_SYSTEM.as("Operating System"),
                            SERVER_INFORMATION.OS_END_OF_LIFE_DATE.as("Operating System EOL"),
                            SERVER_INFORMATION.HW_END_OF_LIFE_DATE.as("Hardware EOL"),
                            SERVER_INFORMATION.LIFECYCLE_STATUS.as("Lifecycle"))
                    .from(SERVER_INFORMATION)
                    .join(SERVER_USAGE)
                    .on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                    .join(APPLICATION)
                    .on(APPLICATION.ID.eq(SERVER_USAGE.ENTITY_ID))
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
        EntityReference origRef = WebUtilities.getEntityReference(request);
        return nameResolver
                .resolve(origRef)
                .orElse(origRef);
    }


    private String mkFilename(EntityReference ref) {
        return sanitizeName(ref.name().orElse(ref.kind().name()))
                        + "-technology-server-eol";
    }


    private String sanitizeName(String str) {
        return str
                .replace(".", "-")
                .replace(" ", "-")
                .replace(",", "-");
    }
}
