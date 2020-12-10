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

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.Tables.LICENCE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
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
        String path = mkPath("data-extract", "licences", ":kind", ":id");
        get(path, (request, response) -> {

            EntityReference entityRef = getEntityReference(request);

            IdSelectionOptions selectionOptions = mkOpts(entityRef);
            Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(selectionOptions);

            SelectConditionStep<Record8<Long, String, String, String, String, Timestamp, String, String>> qry = dsl
                    .selectDistinct(LICENCE.ID.as("Licence Id"),
                            LICENCE.NAME.as("Licence Name"),
                            LICENCE.DESCRIPTION.as("Description"),
                            LICENCE.EXTERNAL_ID.as("External Id"),
                            LICENCE.APPROVAL_STATUS.as("Approval Status"),
                            LICENCE.LAST_UPDATED_AT.as("Last Updated At"),
                            LICENCE.LAST_UPDATED_BY.as("Last Updated By"),
                            LICENCE.PROVENANCE.as("Provenance"))
                    .from(LICENCE)
                    .innerJoin(ENTITY_RELATIONSHIP)
                    .on(LICENCE.ID.eq(ENTITY_RELATIONSHIP.ID_B).and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.LICENCE.name())))
                    .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(ENTITY_RELATIONSHIP.ID_A).and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APPLICATION.name())))
                    .where(APPLICATION.ID.in(appIdSelector))
                    .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.notEqual(EntityLifecycleStatus.REMOVED.name()));

            String filename = "licences";

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });
    }
}
