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

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Record8;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static java.lang.String.format;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.LICENCE;
import static org.finos.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static org.finos.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;
import static spark.Spark.get;


@Service
public class LicencesExtractor extends DirectQueryBasedDataExtractor {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

    @Autowired
    public LicencesExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String path = WebUtilities.mkPath("data-extract", "licences", ":kind", ":id");
        get(path, (request, response) -> {

            EntityReference entityRef = WebUtilities.getEntityReference(request);

            IdSelectionOptions selectionOptions = mkOpts(entityRef);
            Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(selectionOptions);

            SelectConditionStep<Record7<Long, String, String, String, Timestamp, String, String>> qry = dsl
                    .selectDistinct(
                            LICENCE.ID.as("Licence Id"),
                            LICENCE.NAME.as("Licence Name"),
                            LICENCE.DESCRIPTION.as("Description"),
                            LICENCE.EXTERNAL_ID.as("External Id"),
                            LICENCE.LAST_UPDATED_AT.as("Last Updated At"),
                            LICENCE.LAST_UPDATED_BY.as("Last Updated By"),
                            LICENCE.PROVENANCE.as("Provenance"))
                    .from(SOFTWARE_USAGE)
                    .innerJoin(SOFTWARE_VERSION_LICENCE)
                    .on(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID.eq(SOFTWARE_USAGE.SOFTWARE_VERSION_ID))
                    .innerJoin(LICENCE)
                    .on(LICENCE.ID.eq(SOFTWARE_VERSION_LICENCE.LICENCE_ID))
                    .where(dsl.renderInlined(SOFTWARE_USAGE.APPLICATION_ID.in(appIdSelector)));

            String filename = format("licences-%s/%s", entityRef.kind(), entityRef.id());

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });
    }
}
