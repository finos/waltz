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
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.schema.tables.Measurable;
import org.jooq.DSLContext;
import org.jooq.Record12;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class ProcessDiagramExtractor extends DirectQueryBasedDataExtractor {


    private static final Measurable parent_measurable = MEASURABLE.as("parent_measurable");
    private static final Measurable child_measurable = MEASURABLE.as("child_measurable");

    @Autowired
    public ProcessDiagramExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        get(mkPath("data-extract", "process-diagram", ":id"),
                (request, response) -> writeExtract(
                        "process-diagram",
                        prepareExtract(getId(request)),
                        request,
                        response));
    }


    private SelectConditionStep<Record12<Long, String, String, String, Long, String, String, String, Long, String, String, String>> prepareExtract(Long diagramId) {

        return dsl
                .select(parent_measurable.ID.as("Process Diagram Measurable Id"),
                        parent_measurable.NAME.as("Process Diagram Measurable Name"),
                        parent_measurable.DESCRIPTION.as("Process Diagram Measurable Description"),
                        parent_measurable.EXTERNAL_ID.as("Process Diagram Measurable External Id"),
                        APPLICATION.ID.as("Mapped Application Id"),
                        APPLICATION.NAME.as("Mapped Application Name"),
                        APPLICATION.DESCRIPTION.as("Mapped Application Description"),
                        APPLICATION.ASSET_CODE.as("Mapped Application External Id"),
                        child_measurable.ID.as("Mapped Measurable Id"),
                        child_measurable.NAME.as("Mapped Measurable Name"),
                        child_measurable.DESCRIPTION.as("Mapped Measurable Description"),
                        child_measurable.EXTERNAL_ID.as("Mapped Measurable External Id"))
                .from(PROCESS_DIAGRAM_ENTITY)
                .innerJoin(ENTITY_HIERARCHY).on(PROCESS_DIAGRAM_ENTITY.ENTITY_ID.eq(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name()))
                        .and(PROCESS_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                .innerJoin(parent_measurable).on(PROCESS_DIAGRAM_ENTITY.ENTITY_ID.eq(parent_measurable.ID))
                .innerJoin(child_measurable).on(ENTITY_HIERARCHY.ID.eq(child_measurable.ID))
                .leftJoin(MEASURABLE_RATING).on(child_measurable.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .leftJoin(APPLICATION).on(MEASURABLE_RATING.ENTITY_ID.eq(APPLICATION.ID)
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()))
                        .and(APPLICATION.IS_REMOVED.isFalse()))
                .where(PROCESS_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId));
    }
}
