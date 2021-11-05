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

import org.finos.waltz.common.ListUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.PhysicalFlow;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.finos.waltz.common.ListUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.*;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static com.khartec.waltz.web.WebUtilities.*;
import static java.util.stream.Collectors.toList;
import static spark.Spark.post;


@Service
public class PhysicalFlowExtractor extends CustomDataExtractor {

    private DSLContext dsl;
    private final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory = new PhysicalFlowIdSelectorFactory();

    private static List<Field> RECEIVER_NAME_AND_ASSET_CODE_FIELDS;
    private static List<Field> SOURCE_NAME_AND_ASSET_CODE_FIELDS;
    private static List<Field> SOURCE_AND_TARGET_NAME_AND_ASSET_CODE;
    private static List<String> staticHeaders = newArrayList(
            "Name",
            "External Id",
            "Source",
            "Source Asset Code",
            "Receiver",
            "Receiver Asset Code",
            "Format",
            "Transport",
            "Frequency",
            "Criticality",
            "Freshness Indicator",
            "Description");

    static {
        Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                LOGICAL_FLOW.SOURCE_ENTITY_ID,
                LOGICAL_FLOW.SOURCE_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

        Field<String> sourceAssetCodeField = DSL
                .when(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(APPLICATION.ASSET_CODE)
                                .from(APPLICATION)
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)));

        SOURCE_NAME_AND_ASSET_CODE_FIELDS = ListUtilities.asList(
                SOURCE_NAME_FIELD.as("Source"),
                sourceAssetCodeField.as("Source Asset Code"));

        Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                LOGICAL_FLOW.TARGET_ENTITY_ID,
                LOGICAL_FLOW.TARGET_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

        Field<String> targetAssetCodeField = DSL
                .when(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(APPLICATION.ASSET_CODE)
                                .from(APPLICATION)
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)));

        RECEIVER_NAME_AND_ASSET_CODE_FIELDS = ListUtilities.asList(
                TARGET_NAME_FIELD.as("Receiver"),
                targetAssetCodeField.as("Receiver Asset Code"));

        SOURCE_AND_TARGET_NAME_AND_ASSET_CODE = ListUtilities.concat(
                SOURCE_NAME_AND_ASSET_CODE_FIELDS,
                RECEIVER_NAME_AND_ASSET_CODE_FIELDS);
    }


    @Autowired
    public PhysicalFlowExtractor(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void register() {
        post(mkPath("data-extract", "physical-flows", "all", ":kind", ":id"), (request, response) -> {
            EntityReference ref = getEntityReference(request);
            String fileName = "physical-flows-all-" + ref.id();
            return writeReportResults(
                    response,
                    preparePhysicalFlows(
                            prepareAllFlowsQuery(ref),
                            parseExtractFormat(request),
                            fileName,
                            getTagsMap()));
        });


        post(mkPath("data-extract", "physical-flows", "produces", ":kind", ":id"), (request, response) -> {
            EntityReference ref = getEntityReference(request);
            String fileName = "physical-flows-produces-" + ref.id();
            return writeReportResults(
                    response,
                    preparePhysicalFlows(
                            prepareProducesQuery(ref),
                            parseExtractFormat(request),
                            fileName,
                            getTagsMap()));
        });

        post(mkPath("data-extract", "physical-flows", "consumes", ":kind", ":id"), (request, response) -> {
            EntityReference ref = getEntityReference(request);
            String fileName = "physical-flows-consumes-" + ref.id();
            SelectConditionStep<Record> qry = prepareConsumesQuery(ref);
            Map<Long, List<String>> tags = getTagsMap();
            return writeReportResults(
                    response,
                    preparePhysicalFlows(
                            qry,
                            parseExtractFormat(request),
                            fileName,
                            tags));
        });

        post(mkPath("data-extract", "physical-flows", "by-selector"), (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> idSelector = physicalFlowIdSelectorFactory.apply(idSelectionOptions);
            Condition condition =
                    PhysicalFlow.PHYSICAL_FLOW.ID.in(idSelector)
                            .and(physicalFlowIdSelectorFactory.getLifecycleCondition(idSelectionOptions));
            SelectConditionStep<Record> qry = getQuery(condition);
            Map<Long, List<String>> tags = getTagsMap();

            String fileName = String.format("physical-flows-for-%s-%s",
                    idSelectionOptions.entityReference().kind().name().toLowerCase(),
                    idSelectionOptions.entityReference().id());

            return writeReportResults(
                    response,
                    preparePhysicalFlows(
                            qry,
                            parseExtractFormat(request),
                            fileName,
                            tags));
        });
    }

    private Map<Long, List<String>> getTagsMap() {
        return dsl.select(TAG_USAGE.ENTITY_ID, TAG.NAME)
                .from(TAG_USAGE)
                .leftOuterJoin(TAG)
                .on(TAG.ID.eq(TAG_USAGE.TAG_ID))
                .where(TAG_USAGE.ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name()))
                .fetchGroups(TAG_USAGE.ENTITY_ID, TAG.NAME);
    }


    private SelectConditionStep<Record> prepareAllFlowsQuery(EntityReference ref) {
        return (SelectConditionStep<Record>) prepareProducesQuery(ref).union(prepareConsumesQuery(ref));
    }


    private SelectConditionStep<Record> prepareProducesQuery(EntityReference ref) {

        Condition isOwnerCondition = PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(ref.id())
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(ref.kind().name()));

        Condition isSourceCondition = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name()));

        Condition isProduces = isOwnerCondition.or(isSourceCondition)
                .and(LOGICAL_NOT_REMOVED)
                .and(PHYSICAL_FLOW_NOT_REMOVED)
                .and(SPEC_NOT_REMOVED);

        return getQuery(isProduces);
    }


    private SelectConditionStep<Record> prepareConsumesQuery(EntityReference ref) {

        Condition isConsumes = LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()))
                .and(LOGICAL_NOT_REMOVED)
                .and(PHYSICAL_FLOW_NOT_REMOVED)
                .and(SPEC_NOT_REMOVED);

        return getQuery(isConsumes);
    }


    private SelectConditionStep<Record> getQuery(Condition condition) {
        return dsl
                .select(
                        PHYSICAL_FLOW.ID,
                        PHYSICAL_SPECIFICATION.NAME.as("Name"),
                        PHYSICAL_FLOW.EXTERNAL_ID.as("External Id"))
                .select(SOURCE_AND_TARGET_NAME_AND_ASSET_CODE)
                .select(
                        PHYSICAL_SPECIFICATION.FORMAT.as("Format"),
                        PHYSICAL_FLOW.TRANSPORT.as("Transport"),
                        PHYSICAL_FLOW.FREQUENCY.as("Frequency"),
                        PHYSICAL_FLOW.CRITICALITY.as("Criticality"),
                        PHYSICAL_FLOW.FRESHNESS_INDICATOR.as("Freshness Indicator"),
                        PHYSICAL_SPECIFICATION.DESCRIPTION.as("Description")
                ).from(PHYSICAL_SPECIFICATION)
                .join(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .join(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(condition);
    }

    private Tuple3<ExtractFormat, String, byte[]> preparePhysicalFlows(SelectConditionStep<Record> query,
                                                                       ExtractFormat format,
                                                                       String reportName,
                                                                       Map<Long, List<String>> tags) throws IOException {

        List<List<Object>> reportRows = prepareReportRows(query, tags);

        return formatReport(
                format,
                reportName,
                reportRows,
                ListUtilities.append(staticHeaders, "Tags")
        );
    }

    private List<List<Object>> prepareReportRows(SelectConditionStep<Record> qry,
                                                 Map<Long, List<String>> tags) {
        Result<Record> results = qry.fetch();

        return results
                .stream()
                .map(row -> {
                    ArrayList<Object> reportRow = new ArrayList<>();
                    staticHeaders.forEach(h -> reportRow.add(row.get(h)));
                    Long physicalFlowId = row.get(PHYSICAL_FLOW.ID);
                    List<String> physicalFlowTags = tags.get(physicalFlowId);
                    reportRow.add(isEmpty(physicalFlowTags)
                            ? ""
                            : String.join(",", physicalFlowTags));

                    return reportRow;
                })
                .collect(toList());
    }
}
