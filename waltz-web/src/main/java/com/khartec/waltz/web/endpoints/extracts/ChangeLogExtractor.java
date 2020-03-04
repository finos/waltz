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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;



import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.Tables.CHANGE_LOG;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static com.khartec.waltz.web.WebUtilities.*;
import static spark.Spark.post;


@Service
public class ChangeLogExtractor extends DirectQueryBasedDataExtractor {

    private final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory = new PhysicalFlowIdSelectorFactory();


    static {
    }


    @Autowired
    public ChangeLogExtractor(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public void register() {
        post(mkPath("data-extract", "change-log", ":kind", ":id"), (request, response) -> {
            EntityReference ref = getEntityReference(request);
            SelectConditionStep<?> qry = prepareQuery(ref);

            return writeExtract("change-log-" + ref.id(), qry, request, response);
        });

    }

    private SelectConditionStep<?> PFprepareQuery(EntityReference ref) {

        Condition isConsumes = LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()))
                .and(LOGICAL_NOT_REMOVED);

        return getQuery(isConsumes);
    }

    private SelectConditionStep<?> prepareQuery(EntityReference ref) {

        Condition isConsumes = CHANGE_LOG.PARENT_ID.eq(ref.id())
                .and(CHANGE_LOG.PARENT_KIND.eq(ref.kind().name()));

        return getQuery(isConsumes);
    }

    private SelectConditionStep<Record4<String, String, String, Timestamp>> getQuery(Condition condition) {

        return (SelectConditionStep<Record4<String, String, String, Timestamp>>) dsl.select(
                        CHANGE_LOG.SEVERITY.as("Severity"),
                        CHANGE_LOG.MESSAGE.as("Message"),
                        CHANGE_LOG.USER_ID.as("User"),
                        CHANGE_LOG.CREATED_AT.as("Timestamp")
                ).from(CHANGE_LOG)
                .where(condition)
                .orderBy(CHANGE_LOG.CREATED_AT.desc());

    }

    private SelectConditionStep<Record> PFgetQuery(List<Field> senderOrReceiverColumn,
                                                 Condition condition) {
        return dsl.select(PHYSICAL_SPECIFICATION.NAME.as("Name"),
                PHYSICAL_SPECIFICATION.EXTERNAL_ID.as("External Id"))
                .select(senderOrReceiverColumn)
                .select(
                        PHYSICAL_SPECIFICATION.FORMAT.as("Format"),
                        PHYSICAL_FLOW.TRANSPORT.as("Transport"),
                        PHYSICAL_FLOW.FREQUENCY.as("Frequency"),
                        PHYSICAL_FLOW.CRITICALITY.as("Criticality"),
                        PHYSICAL_FLOW.FRESHNESS_INDICATOR.as("Observed"),
                        PHYSICAL_SPECIFICATION.DESCRIPTION.as("Description")
                ).from(PHYSICAL_SPECIFICATION)
                .join(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .join(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(condition);
    }


}
