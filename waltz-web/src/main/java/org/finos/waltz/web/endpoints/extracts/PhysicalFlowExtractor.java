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

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.enum_value.EnumValueKind;
import org.finos.waltz.schema.tables.EnumValue;
import org.finos.waltz.schema.tables.PhysicalFlow;
import org.finos.waltz.web.WebUtilities;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.ListUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static org.finos.waltz.data.logical_flow.LogicalFlowDao.SPEC_NOT_REMOVED;
import static org.finos.waltz.data.physical_flow.PhysicalFlowDao.PHYSICAL_FLOW_NOT_REMOVED;
import static org.finos.waltz.schema.Tables.ENUM_VALUE;
import static org.finos.waltz.schema.Tables.PHYSICAL_FLOW;
import static org.finos.waltz.schema.Tables.TAG;
import static org.finos.waltz.schema.Tables.TAG_USAGE;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static spark.Spark.post;


@Service
public class PhysicalFlowExtractor extends CustomDataExtractor {

    private static final List<Field<String>> RECEIVER_NAME_AND_ASSET_CODE_FIELDS;
    private static final List<Field<String>> SOURCE_NAME_AND_ASSET_CODE_FIELDS;
    private static final List<Field<String>> SOURCE_AND_TARGET_NAME_AND_ASSET_CODE;
    private static final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory = new PhysicalFlowIdSelectorFactory();


    private final DSLContext dsl;


    static {
        Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                LOGICAL_FLOW.SOURCE_ENTITY_ID,
                LOGICAL_FLOW.SOURCE_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

        Field<String> sourceAssetCodeField = InlineSelectFieldFactory.mkExternalIdField(
                LOGICAL_FLOW.SOURCE_ENTITY_ID,
                LOGICAL_FLOW.SOURCE_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

        SOURCE_NAME_AND_ASSET_CODE_FIELDS = ListUtilities.asList(
                SOURCE_NAME_FIELD.as("Source"),
                sourceAssetCodeField.as("Source Asset Code"));

        Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                LOGICAL_FLOW.TARGET_ENTITY_ID,
                LOGICAL_FLOW.TARGET_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

        Field<String> targetAssetCodeField = InlineSelectFieldFactory.mkExternalIdField(
                LOGICAL_FLOW.TARGET_ENTITY_ID,
                LOGICAL_FLOW.TARGET_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

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
        post(WebUtilities.mkPath("data-extract", "physical-flows", "all", ":kind", ":id"), (request, response) -> {
            EntityReference ref = WebUtilities.getEntityReference(request);
            String fileName = "physical-flows-all-" + ref.id();
            return writeReportResults(
                    response,
                    preparePhysicalFlows(
                            prepareAllFlowsQuery(ref),
                            parseExtractFormat(request),
                            fileName,
                            getTagsMap()));
        });


        post(WebUtilities.mkPath("data-extract", "physical-flows", "produces", ":kind", ":id"), (request, response) -> {
            EntityReference ref = WebUtilities.getEntityReference(request);
            String fileName = "physical-flows-produces-" + ref.id();
            return writeReportResults(
                    response,
                    preparePhysicalFlows(
                            prepareProducesQuery(ref),
                            parseExtractFormat(request),
                            fileName,
                            getTagsMap()));
        });

        post(WebUtilities.mkPath("data-extract", "physical-flows", "consumes", ":kind", ":id"), (request, response) -> {
            EntityReference ref = WebUtilities.getEntityReference(request);
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

        post(WebUtilities.mkPath("data-extract", "physical-flows", "by-selector"), (request, response) -> {
            IdSelectionOptions idSelectionOptions = WebUtilities.readIdSelectionOptionsFromBody(request);
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
        EnumValue criticalityValue = ENUM_VALUE.as("criticality_value");
        EnumValue transportValue = ENUM_VALUE.as("transport_value");
        EnumValue frequencyValue = ENUM_VALUE.as("frequency_value");
        EnumValue dataFormatKindValue = ENUM_VALUE.as("data_format_kind_value");

        return dsl
                .select(
                        PHYSICAL_FLOW.ID,
                        PHYSICAL_SPECIFICATION.NAME.as("Name"),
                        PHYSICAL_FLOW.EXTERNAL_ID.as("External Id"))
                .select(SOURCE_AND_TARGET_NAME_AND_ASSET_CODE)
                .select(
                        dataFormatKindValue.DISPLAY_NAME.as("Format"),
                        transportValue.DISPLAY_NAME.as("Transport"),
                        frequencyValue.DISPLAY_NAME.as("Frequency"),
                        criticalityValue.DISPLAY_NAME.as("Criticality"),
                        PHYSICAL_SPECIFICATION.DESCRIPTION.as("Description"))
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .leftJoin(criticalityValue).on(PHYSICAL_FLOW.CRITICALITY.eq(criticalityValue.KEY)
                        .and(criticalityValue.TYPE.eq(EnumValueKind.PHYSICAL_FLOW_CRITICALITY.dbValue())))
                .leftJoin(transportValue).on(PHYSICAL_FLOW.TRANSPORT.eq(transportValue.KEY)
                        .and(transportValue.TYPE.eq(EnumValueKind.TRANSPORT_KIND.dbValue())))
                .leftJoin(frequencyValue).on(PHYSICAL_FLOW.FREQUENCY.eq(frequencyValue.KEY)
                        .and(frequencyValue.TYPE.eq(EnumValueKind.FREQUENCY.dbValue())))
                .leftJoin(dataFormatKindValue).on(PHYSICAL_SPECIFICATION.FORMAT.eq(dataFormatKindValue.KEY)
                        .and(dataFormatKindValue.TYPE.eq(EnumValueKind.DATA_FORMAT_KIND.dbValue())))
                .where(condition);
    }

    private Tuple3<ExtractFormat, String, byte[]> preparePhysicalFlows(SelectConditionStep<Record> query,
                                                                       ExtractFormat format,
                                                                       String reportName,
                                                                       Map<Long, List<String>> tags) throws IOException {

        List<List<Object>> reportRows = prepareReportRows(query, tags);

        List<String> headers = ListUtilities.map(
                query.getSelect(),
                Field::getName);

        return formatReport(
                format,
                reportName,
                reportRows,
                ListUtilities.append(headers, "Tags")
        );
    }

    private List<List<Object>> prepareReportRows(SelectConditionStep<Record> qry,
                                                 Map<Long, List<String>> tags) {
        Result<Record> results = qry.fetch();

        return results
                .stream()
                .map(row -> {
                    List<Object> reportRow = ListUtilities.map(
                            qry.getSelect(),
                            row::get);
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
