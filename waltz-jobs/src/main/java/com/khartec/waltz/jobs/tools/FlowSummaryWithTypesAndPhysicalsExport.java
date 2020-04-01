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

package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple7;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.SetUtilities.fromCollection;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.utils.IdUtilities.indexByOptId;
import static com.khartec.waltz.model.utils.IdUtilities.toIds;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static java.util.Collections.emptyList;
import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 **/
public class FlowSummaryWithTypesAndPhysicalsExport {

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        ApplicationIdSelectorFactory appIdSelectorFactory = new ApplicationIdSelectorFactory();
        ApplicationDao applicationDao = ctx.getBean(ApplicationDao.class);
        OrganisationalUnitDao organisationalUnitDao = ctx.getBean(OrganisationalUnitDao.class);
        LogicalFlowDao logicalFlowDao = ctx.getBean(LogicalFlowDao.class);
        LogicalFlowDecoratorDao decoratorDao = ctx.getBean(LogicalFlowDecoratorDao.class);
        DataTypeDao dataTypeDao = ctx.getBean(DataTypeDao.class);

        Select<Record1<Long>> appSelector = mkAppIdSelector(appIdSelectorFactory);
        Select<Record1<Long>> logicalFlowSelector = mkLogicalFlowSelectorFromAppSelector(appSelector);

        System.out.println("Loading apps");
        Set<Application> allApps = fromCollection(applicationDao.findAll());
        System.out.println("Loading in scope apps");
        Set<Long> inScopeAppIds = toIds(applicationDao.findByAppIdSelector(appSelector));
        System.out.println("Loading OUs");
        List<OrganisationalUnit> allOUs = organisationalUnitDao.findAll();
        System.out.println("Loading DTs");
        List<DataType> allDataTypes = dataTypeDao.findAll();

        System.out.println("Loading Logical Flows");
        List<LogicalFlow> logicalFlows = logicalFlowDao.findBySelector(logicalFlowSelector);
        System.out.println("Loading decorators");
        List<DataTypeDecorator> decorators = decoratorDao.findByAppIdSelector(appSelector);
        System.out.println("Loading phys flows");
        Map<Long, Collection<Tuple7<Long, String, String, String, String, String, String>>> physicalsByLogical = loadPhysicalsByLogical(dsl, logicalFlowSelector);

        System.out.println("Indexing");
        Map<Optional<Long>, Application> appsById = indexByOptId(allApps);
        Map<Optional<Long>, DataType> dataTypesById = indexByOptId(allDataTypes);
        Map<Optional<Long>, OrganisationalUnit> ousById = indexByOptId(allOUs);

        Map<Long, Collection<DataTypeDecorator>> decoratorsByLogicalFlowId = groupBy(DataTypeDecorator::dataFlowId, decorators);

        System.out.println("Processing");
        CsvListWriter csvWriter = setupCSVWriter();

        logicalFlows
                .stream()
                .filter(lf -> lf.source().kind() == EntityKind.APPLICATION && lf.target().kind() == EntityKind.APPLICATION)
                .map(Tuple::tuple)
                .map(t -> t.concat(appsById.get(Optional.of(t.v1.source().id()))))
                .map(t -> t.concat(appsById.get(Optional.of(t.v1.target().id()))))
                .filter(t -> t.v2 != null && t.v3 != null)
                .map(t -> t.concat(ousById.get(Optional.of(t.v2.organisationalUnitId()))))
                .map(t -> t.concat(ousById.get(Optional.of(t.v3.organisationalUnitId()))))
                .map(t -> t.concat(decoratorsByLogicalFlowId
                        .getOrDefault(
                                t.v1.id().orElse(-1L),
                                emptyList())
                        .stream()
                        .filter(d -> d.decoratorEntity().kind() == EntityKind.DATA_TYPE)
                        .map(d -> dataTypesById.get(Optional.of(d.decoratorEntity().id())))
                        .sorted(Comparator.comparing(NameProvider::name))
                        .collect(Collectors.toList())))
                .map(t -> t.concat(inScopeAppIds.contains(t.v2.id().get())))
                .map(t -> t.concat(inScopeAppIds.contains(t.v3.id().get())))
                // (lf:1, src:2, trg:3, srcOu:4, trgOU:5, dataType[]:6, srcInScope: 7, trgInScope: 8)
                .flatMap(t -> physicalsByLogical
                            .getOrDefault(
                                    t.v1.id().orElse(-1L),
                                    newArrayList(tuple(-1L, "-", "-", "-", "-", "-", "-")))
                            .stream()
                            .map(p -> t.concat(p.skip1())))
                .map(t -> newArrayList(
                        t.v2.name(),  // src
                        t.v2.assetCode().orElse(""),
                        t.v2.applicationKind().name(),
                        t.v2.entityLifecycleStatus().name(),
                        Optional.ofNullable(t.v4).map(NameProvider::name).orElse("?"), // src OU
                        t.v7.toString(),
                        t.v3.name(),  // trg
                        t.v3.assetCode().orElse(""),
                        t.v3.applicationKind().name(),
                        t.v3.entityLifecycleStatus().name(),
                        Optional.ofNullable(t.v5).map(NameProvider::name).orElse("?"), // trg OU
                        t.v8.toString(),
                        StringUtilities.joinUsing(t.v6, NameProvider::name, ","),
                        t.v9,
                        t.v10,
                        t.v11,
                        t.v12,
                        t.v13,
                        t.v14))
                .forEach(Unchecked.consumer(csvWriter::write));
    }


    /**
     *
     * @param dsl
     * @param logicalFlowSelector
     * @return logicalFlowId -> [ (lfId, specname, external_id, transport, format, freq, criticality) ]
     */
    private static Map<Long, Collection<Tuple7<Long, String, String, String, String, String, String>>> loadPhysicalsByLogical(DSLContext dsl, Select<Record1<Long>> logicalFlowSelector) {
        List<Tuple7<Long, String, String, String, String, String, String>> physicals = dsl
                .select(PHYSICAL_FLOW.LOGICAL_FLOW_ID,
                        PHYSICAL_FLOW.TRANSPORT,
                        PHYSICAL_FLOW.EXTERNAL_ID,
                        PHYSICAL_FLOW.FREQUENCY,
                        PHYSICAL_FLOW.CRITICALITY)
                .select(PHYSICAL_SPECIFICATION.NAME,
                        PHYSICAL_SPECIFICATION.FORMAT)
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .where(PHYSICAL_FLOW.LOGICAL_FLOW_ID.in(logicalFlowSelector))
                .and(PHYSICAL_FLOW.IS_REMOVED.isFalse())
                .fetch(r -> tuple(
                        r.get(PHYSICAL_FLOW.LOGICAL_FLOW_ID),
                        r.get(PHYSICAL_SPECIFICATION.NAME),
                        r.get(PHYSICAL_FLOW.EXTERNAL_ID),
                        r.get(PHYSICAL_FLOW.TRANSPORT),
                        r.get(PHYSICAL_SPECIFICATION.FORMAT),
                        r.get(PHYSICAL_FLOW.FREQUENCY),
                        r.get(PHYSICAL_FLOW.CRITICALITY)));
        System.out.println("Num physicals: "+physicals.size());
        return groupBy(p -> p.v1, physicals);
    }


    private static CsvListWriter setupCSVWriter() throws IOException {
        CsvListWriter csvWriter = new CsvListWriter(new OutputStreamWriter(new FileOutputStream("/temp/flows.csv")), CsvPreference.EXCEL_PREFERENCE);
        csvWriter.write(
                "Source App",
                "Source Asset Code",
                "Source App Kind",
                "Source App Status",
                "Source App Org Unit", // src OU
                "Source In Scope",
                "Target App",
                "Target Asset Code",
                "Target App Kind",
                "Target App Status",
                "Target App Org Unit", // src OU
                "Target In Scope",
                "Data Types",
                "Physical Name",
                "Physical ExtId",
                "Physical Transport",
                "Physical Format",
                "Physical Frequency",
                "Criticality");
        return csvWriter;
    }


    private static Select<Record1<Long>> mkAppIdSelector(ApplicationIdSelectorFactory appIdSelectorFactory) {
        EntityReference infraRef = mkRef(EntityKind.ORG_UNIT, 6811);
        EntityReference entRiskRef = mkRef(EntityKind.ORG_UNIT, 3125);
        EntityReference regCtrlRef = mkRef(EntityKind.ORG_UNIT, 2761);

        Function<EntityReference, Select<Record1<Long>>> mkOrgUnitSelector = (ref) -> DSL
                .select(ENTITY_HIERARCHY.ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(ref.id()))
                .and(ENTITY_HIERARCHY.KIND.eq(ref.kind().name()));

        Select<Record1<Long>> ouSelector = DSL.selectFrom(
                mkOrgUnitSelector.apply(infraRef)
                    .unionAll(mkOrgUnitSelector.apply(entRiskRef))
                    .unionAll(mkOrgUnitSelector.apply(regCtrlRef)).asTable());

        return DSL
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.in(ouSelector))
                .and(APPLICATION.LIFECYCLE_PHASE.notEqual(EntityLifecycleStatus.REMOVED.name()))
                .and(APPLICATION.IS_REMOVED.isFalse());

    }


    private static Select<Record1<Long>> mkLogicalFlowSelectorFromAppSelector(Select<Record1<Long>> appIdSelector) {
        Condition sourceCondition = LOGICAL_FLOW.SOURCE_ENTITY_ID.in(appIdSelector)
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Condition targetCondition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(appIdSelector)
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(sourceCondition.or(targetCondition))
                .and(LOGICAL_NOT_REMOVED);
    }

}
