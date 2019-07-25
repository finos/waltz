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
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.NOT_REMOVED;
import static com.khartec.waltz.schema.Tables.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.post;


@Service
public class PhysicalFlowExtractor extends BaseDataExtractor {

    @Autowired
    public PhysicalFlowExtractor(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public void register() {
        post(mkPath("data-extract", "physical-flows", "produces", ":kind", ":id"), (request, response) -> {
            EntityReference ref = getEntityReference(request);
            SelectConditionStep<?> qry = prepareProducesQuery(ref);
            return writeExtract("physical-flows-produces-" + ref.id(), qry, request, response);
        });

        post(mkPath("data-extract", "physical-flows", "consumes", ":kind", ":id"), (request, response) -> {
            EntityReference ref = getEntityReference(request);
            SelectConditionStep<?> qry = prepareConsumesQuery(ref);
            return writeExtract("physical-flows-consumes-" + ref.id(), qry, request, response);
        });
    }


    private SelectConditionStep<?> prepareProducesQuery(EntityReference ref) {

        Condition isOwnerCondition = PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(ref.id())
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(ref.kind().name()));

        Condition isSourceCondition = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name()))
                .and(NOT_REMOVED);

        Condition isProduces = isOwnerCondition.or(isSourceCondition);

        return getQuery(getReceiverNameAndNarColumn(), isProduces);
    }


    private SelectConditionStep<?> prepareConsumesQuery(EntityReference ref) {

        Condition isConsumes = LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()))
                .and(NOT_REMOVED);

        return getQuery(getSourceNameAndNarColumn(), isConsumes);
    }


    private SelectConditionStep<Record> getQuery(SelectFieldOrAsterisk[] senderOrReceiverColumn,
                                                 Condition condition) {

        return dsl.select(PHYSICAL_SPECIFICATION.NAME.as("Name"),
                PHYSICAL_SPECIFICATION.EXTERNAL_ID.as("External Id"))
                .select(senderOrReceiverColumn)
                .select(
                        PHYSICAL_SPECIFICATION.FORMAT.as("Format"),
                        PHYSICAL_FLOW.TRANSPORT.as("Transport"),
                        PHYSICAL_FLOW.FREQUENCY.as("Frequency"),
                        PHYSICAL_FLOW.CRITICALITY.as("Criticality"),
                        PHYSICAL_SPECIFICATION.DESCRIPTION.as("Description")
                ).from(PHYSICAL_SPECIFICATION)
                .join(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .join(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(condition);
    }

    private SelectFieldOrAsterisk[] getSourceNameAndNarColumn() {

        Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                LOGICAL_FLOW.SOURCE_ENTITY_ID,
                LOGICAL_FLOW.SOURCE_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

        Field<String> sourceAssetCodeField = DSL
                .when(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(APPLICATION.ASSET_CODE)
                                .from(APPLICATION)
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)));

        return new SelectFieldOrAsterisk[]{
                SOURCE_NAME_FIELD.as("Source"),
                sourceAssetCodeField.as("Source Asset Code")};
    }


    private SelectFieldOrAsterisk[] getReceiverNameAndNarColumn() {

        Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                LOGICAL_FLOW.TARGET_ENTITY_ID,
                LOGICAL_FLOW.TARGET_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

        Field<String> targetAssetCodeField = DSL
                .when(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(APPLICATION.ASSET_CODE)
                                .from(APPLICATION)
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)));

        return new SelectFieldOrAsterisk[]{
                TARGET_NAME_FIELD.as("Receiver"),
                targetAssetCodeField.as("Receiver Asset Code")};
    }

}
