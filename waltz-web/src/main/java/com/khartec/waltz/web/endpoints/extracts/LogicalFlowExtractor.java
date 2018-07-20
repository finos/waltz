/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static spark.Spark.post;


@Service
public class LogicalFlowExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowExtractor.class);

    private static final Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

    private static final Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.TARGET_ENTITY_ID,
            LOGICAL_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;

    @Autowired
    public LogicalFlowExtractor(DSLContext dsl,
                                ApplicationIdSelectorFactory applicationIdSelectorFactory) {
        super(dsl);
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");

        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
    }


    @Override
    public void register() {
        post(mkPath("data-extract", "logical-flows"), (request, response) -> {
            IdSelectionOptions options = readIdSelectionOptionsFromBody(request);
            CSVSerializer serializer = extract(options);
            return writeFile("logical-flows.csv", serializer, response);
        });
    }


    private CSVSerializer extract(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(options);

        Field<Long> sourceFlowId = LOGICAL_FLOW.ID.as("sourceFlowId");
        Field<Long> targetFlowId = LOGICAL_FLOW.ID.as("targetFlowId");

        Select<Record1<Long>> sourceAppFlows = DSL.select(sourceFlowId)
                .from(LOGICAL_FLOW)
                .innerJoin(APPLICATION)
                    .on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(APPLICATION.ID))
                .where(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(APPLICATION.ID.in(appIdSelector));

        Select<Record1<Long>> targetAppFlows = DSL.select(targetFlowId)
                .from(LOGICAL_FLOW)
                .innerJoin(APPLICATION)
                    .on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(APPLICATION.ID))
                .where(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(APPLICATION.ID.in(appIdSelector));

        Field<String> sourceAssetCodeField = DSL
                .when(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(APPLICATION.ASSET_CODE)
                                .from(APPLICATION)
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)));

        Field<String> sourceOrgUnitNameField = DSL
                .when(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(ORGANISATIONAL_UNIT.NAME)
                                .from(APPLICATION)
                                .innerJoin(ORGANISATIONAL_UNIT)
                                    .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)));

        Field<String> targetAssetCodeField = DSL
                .when(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(APPLICATION.ASSET_CODE)
                                .from(APPLICATION)
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)));

        Field<String> targetOrgUnitNameField = DSL
                .when(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(ORGANISATIONAL_UNIT.NAME)
                                .from(APPLICATION)
                                .innerJoin(ORGANISATIONAL_UNIT)
                                    .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)));

        Result<Record> data = dsl
                .select(SOURCE_NAME_FIELD, TARGET_NAME_FIELD)
                .select(sourceAssetCodeField)
                .select(targetAssetCodeField)
                .select(sourceOrgUnitNameField)
                .select(targetOrgUnitNameField)
                .select(DATA_TYPE.NAME)
                .select(ENUM_VALUE.DISPLAY_NAME)
                .from(LOGICAL_FLOW)
                .leftJoin(sourceAppFlows)
                    .on(sourceFlowId.eq(LOGICAL_FLOW.ID))
                .leftJoin(targetAppFlows)
                    .on(targetFlowId.eq(LOGICAL_FLOW.ID))
                .join(LOGICAL_FLOW_DECORATOR)
                    .on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID)
                        .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .join(DATA_TYPE)
                    .on(DATA_TYPE.ID.eq(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)
                        .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .join(ENUM_VALUE)
                    .on(ENUM_VALUE.KEY.eq(LOGICAL_FLOW_DECORATOR.RATING)
                            .and(ENUM_VALUE.TYPE.eq("AuthoritativenessRating")))
                .where(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()))
                .and(sourceFlowId.isNotNull()
                        .or(targetFlowId.isNotNull()))
                .fetch();

        CSVSerializer serializer = csvWriter -> {
            csvWriter.writeHeader(
                    "Source",
                    "Source Asset Code",
                    "Source Org Unit",
                    "Target",
                    "Target Asset Code",
                    "Target Org Unit",
                    "Data Type",
                    "Authoritativeness");

            data.forEach(r -> {
                try {
                    csvWriter.write(
                            r.get(SOURCE_NAME_FIELD),
                            r.get(sourceAssetCodeField),
                            r.get(sourceOrgUnitNameField),
                            r.get(TARGET_NAME_FIELD),
                            r.get(targetAssetCodeField),
                            r.get(targetOrgUnitNameField),
                            r.get(DATA_TYPE.NAME),
                            r.get(ENUM_VALUE.DISPLAY_NAME));
                } catch (IOException ioe) {
                    LOG.warn("Failed to write logical flow: " + r, ioe);
                }
            });
        };

        return serializer;
    }

}
