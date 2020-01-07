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

package com.khartec.waltz.jobs.clients.c1.sc1;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.StreamUtilities.Siphon;
import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.jobs.Columns;
import com.khartec.waltz.jobs.WaltzUtilities;
import com.khartec.waltz.jobs.clients.c1.sc1.model.*;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.change_initiative.ChangeInitiativeKind;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.schema.tables.records.*;
import com.khartec.waltz.service.DIConfiguration;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.DSLContext;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.StreamUtilities.mkSiphon;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.jobs.WaltzUtilities.getOrCreateMeasurableCategory;
import static com.khartec.waltz.jobs.WaltzUtilities.toId;
import static com.khartec.waltz.jobs.XlsUtilities.strVal;
import static com.khartec.waltz.jobs.XlsUtilities.streamRows;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static java.util.stream.Collectors.toMap;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Component
public class XlsImporter {

    private static final String PROVENANCE = "C1_SC1";
    private final DSLContext dsl;


    @Autowired
    public XlsImporter(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public void load(String filename) throws IOException {
        InputStream inputStream = checkNotNull(
                this.getClass().getClassLoader().getResourceAsStream(filename),
                "Cannot find file [%s] on classpath",
                filename);

        Workbook workbook = new XSSFWorkbook(inputStream);

        Long componentCategoryId = makeComponentCategory();

        removeExistingCategories();
        makeOrgUnits(workbook);
        makeApps(workbook);
        updateAppsWithOrgUnits(workbook);
        makeDataTypes();
        makeFlows(workbook);
        makeDomainTaxonomies(workbook);
        makeAppToDomainMappings(workbook);
        makeMeasurableRelationships(workbook);
        makeComponentTaxonomy(workbook, componentCategoryId);
        makeAppToComponentMappings(workbook, componentCategoryId);
        makeProjects(workbook);
        makeAppToProjectMappings(workbook);

    }

    private int makeAppToProjectMappings(Workbook workbook) {
        removeAllAppToProjectMappings();
        Map<String, Long> changeExtToIdMap = loadChangeExtToIdMap();
        Map<String, Long> appExtToIdMap = loadAppExtToIdMap();
        List<EntityRelationshipRecord> records = streamRows(workbook, SheetDefinition.PROJECT)
                .skip(1)
                .map(ProjectRow::fromRow)
                .map(p -> tuple(appExtToIdMap.get(p.applicationId()), changeExtToIdMap.get(p.projectObjectId())))
                .filter(t -> t.v1 != null && t.v2 != null)
                .map(t -> {
                    EntityRelationshipRecord record = dsl.newRecord(ENTITY_RELATIONSHIP);
                    record.setKindA(EntityKind.APPLICATION.name());
                    record.setIdA(t.v1);
                    record.setRelationship(RelationshipKind.RELATES_TO.name());
                    record.setKindB(EntityKind.CHANGE_INITIATIVE.name());
                    record.setIdB(t.v2);
                    record.setLastUpdatedBy("admin");
                    record.setLastUpdatedAt(nowUtcTimestamp());
                    record.setProvenance(PROVENANCE);
                    return record;
                })
                .collect(Collectors.toList());

        int[] rc = dsl.batchInsert(records).execute();
        log("Created %d app relations to projects\n", rc.length);
        return rc.length;
    }


    private int makeProjects(Workbook workbook) {
        removeAllProjects();
        AtomicLong ctr = new AtomicLong(0);

        List<ChangeInitiativeRecord> records = streamRows(workbook, SheetDefinition.PROJECT)
                .skip(1)
                .map(ProjectRow::fromRow)
                .collect(toMap(p -> p.projectObjectId(), Function.identity(), (p1, p2) -> p1))
                .values()
                .stream()
                .map(p -> {
                    ChangeInitiativeRecord record = dsl.newRecord(CHANGE_INITIATIVE);
                    record.setId(ctr.incrementAndGet());
                    record.setName(p.projectName());
                    record.setExternalId(p.projectObjectId());
                    record.setDescription("Project: " + p.projectName());
                    record.setKind(ChangeInitiativeKind.PROJECT.name());
                    record.setProvenance(PROVENANCE);
                    record.setLifecyclePhase(p.lifecyclePhase().name());
                    Date startDate = DateTimeUtilities.toSqlDate(p.startDate());
                    Date endDate = DateTimeUtilities.toSqlDate(p.endDate());
                    record.setStartDate(startDate);
                    record.setEndDate(endDate);
                    return record;
                })
                .collect(Collectors.toList());

        int[] rc = dsl.batchInsert(records).execute();
        log("Created %d projects\n", rc.length);
        return rc.length;
    }


    private void removeExistingCategories() {
        dsl.deleteFrom(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.in(
                        "CAPABILITY",
                        "PROCESS",
                        "SERVICE",
                        "PRODUCT",
                        "REGION",
                        "BUSINESS",
                        "COMPONENT"))
                .execute();
    }


    private int makeAppToDomainMappings(Workbook workbook) {
        removeRatings();
        Map<String, Long> measurableExtToIdMap = loadMeasurableExtToIdMap();
        Map<String, Long> appObjectIdToIdMap = loadAppObjectIdToIdMap(workbook);

        List<MeasurableRatingRecord> records = streamRows(workbook, SheetDefinition.BUSINESS_SUPPORT)
                .skip(1)
                .map(BusinessSupportRow::fromRow)
                .map(r -> tuple(
                        appObjectIdToIdMap.get(r.applicationId()),
                        measurableExtToIdMap.get(r.domainObjectId())))
                .filter(t -> t.v1 != null && t.v2 != null)
                .distinct()
                .map(t -> {
                    MeasurableRatingRecord record = dsl.newRecord(MEASURABLE_RATING);
                    record.setEntityKind(EntityKind.APPLICATION.name());
                    record.setEntityId(t.v1);
                    record.setMeasurableId(t.v2);
                    record.setRating("G");
                    record.setLastUpdatedAt(nowUtcTimestamp());
                    record.setLastUpdatedBy("admin");
                    record.setProvenance(PROVENANCE);
                    return record;
                })
                .collect(Collectors.toList());

        int[] rc = dsl.batchInsert(records).execute();
        log("Created %d ratings\n", rc.length);
        return rc.length;
    }

    /**
     * NOTE: This doesn't work as we don't have the id's for the K's
     * @param workbook
     * @return
     */
    private int makeMeasurableRelationships(Workbook workbook) {

        Map<String, Long> measurableExtToIdMap = loadMeasurableExtToIdMap();
        List<EntityRelationshipRecord> records = streamRows(workbook, SheetDefinition.DOMAIN)
                .skip(1)
                .map(DomainRow::fromRow)
                .filter(r -> !isEmpty(r.crossReference()))
                .map(r -> tuple(
                        measurableExtToIdMap.get(r.domainObjectId()),
                        measurableExtToIdMap.get(r.crossReference())))
                .map(t -> {
                    EntityRelationshipRecord record = dsl.newRecord(ENTITY_RELATIONSHIP);
                    record.setKindA(EntityKind.MEASURABLE.name());
                    record.setKindB(EntityKind.MEASURABLE.name());
                    record.setIdA(t.v1);
                    record.setIdB(t.v2);
                    record.setDescription("Taken from Domain tab");
                    record.setLastUpdatedAt(nowUtcTimestamp());
                    record.setLastUpdatedBy("admin");
                    record.setProvenance(PROVENANCE);
                    return record;
                })
                .collect(Collectors.toList());

        int[] rc = dsl.batchInsert(records).execute();
        log("Created %d measurable relationships\n", rc.length);
        return rc.length;
    }

    private void makeDomainTaxonomies(Workbook workbook) {
        List<DomainRow> domainRows = streamRows(workbook, SheetDefinition.DOMAIN)
                .skip(1)
                .map(DomainRow::fromRow)
                .collect(Collectors.toList());

        List<FlatNode<DomainRow, String>> domainRowsAsFlatNodes = ListUtilities.map(domainRows, r -> r.toFlatNode());
        Forest<DomainRow, String> forest = HierarchyUtilities.toForest(domainRowsAsFlatNodes);
        mkCategories(forest.getRootNodes(), domainRows);

    }


    private void mkCategories(Set<Node<DomainRow, String>> roots, List<DomainRow> domainRows) {
        roots.forEach(c -> {
                String categoryCode = CollectionUtilities.first(c.getChildren()).getData().categoryCode();
                Long categoryId = getOrCreateMeasurableCategory(dsl, categoryCode, categoryCode);
                List<DomainRow> descendents = ListUtilities.filter(dr -> mkSafe(dr.categoryCode()).equals(categoryCode), domainRows);
                mkCategory(categoryId, categoryCode, c.getData(), descendents);
            });
    }


    private int mkCategory(Long categoryId, String categoryCode, DomainRow root, List<DomainRow> descendents) {
        removeCategory(categoryId);

        List<MeasurableRecord> records = new ArrayList<>();

        MeasurableRecord rootRecord = newMeasurableRecord(dsl, categoryId);
        rootRecord.setName(toDescription(categoryCode, root.domainName()));
        rootRecord.setDescription(toDescription(categoryCode, root.domainObjectId(), root.domainName()));
        rootRecord.setExternalId(root.domainObjectId());
        rootRecord.setConcrete(false);
        records.add(rootRecord);

        records.addAll(ListUtilities.map(descendents, d -> {
            MeasurableRecord record = newMeasurableRecord(dsl, categoryId);
            record.setName(toDescription(categoryCode, d.domainName(), d.parentExtId()));
            record.setDescription(toDescription(categoryCode, d.domainName(), d.parentExtId()));
            record.setExternalId(d.domainObjectId());
            record.setExternalParentId(d.parentDomainObjectId());
            record.setConcrete(true);
            return record;
        }));

        int[] rc = dsl.batchInsert(records).execute();
        log("Create category: %s (%d) with %d records\n", categoryCode, categoryId, rc.length);

        updateParentIds(categoryId);
        return rc.length;
    }


    private int updateAppsWithOrgUnits(Workbook workbook) {
        Map<String, Long> appObjectIdToIdMap = loadAppObjectIdToIdMap(workbook);
        Map<String, Long> orgExtToIdMap = loadOrgExtToIdMap();

        List<UpdateConditionStep<ApplicationRecord>> updates = streamRows(workbook, SheetDefinition.BUSINESS_SUPPORT)
                .skip(1)
                .map(BusinessSupportRow::fromRow)
                .map(r -> tuple(r.applicationId(), r.orgObjectId()))
                .distinct()
                .filter(t -> t.v1 != null && t.v2 != null)
                .map(t -> t.map((appName, orgExt) -> tuple(appObjectIdToIdMap.get(appName), orgExtToIdMap.get(orgExt))))
                .filter(t -> t.v1 != null && t.v2 != null)
                .map(t -> dsl
                        .update(APPLICATION)
                        .set(APPLICATION.ORGANISATIONAL_UNIT_ID, t.v2)
                        .where(APPLICATION.ID.eq(t.v1)))
                .collect(Collectors.toList());

        log("Updating: %d ou's\n", updates.size());

        int[] rc = dsl.batch(updates).execute();
        return rc.length;
    }


    private void makeDataTypes() {
        dsl.deleteFrom(DATA_TYPE).execute();

        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "STOCK")
                .set(DATA_TYPE.DESCRIPTION, "Stock Data")
                .set(DATA_TYPE.NAME, "Stock Data")
                .set(DATA_TYPE.ID, 1000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "PARTY")
                .set(DATA_TYPE.DESCRIPTION, "Party Data")
                .set(DATA_TYPE.NAME, "Counterparty etc.")
                .set(DATA_TYPE.ID, 2000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "PRICING")
                .set(DATA_TYPE.DESCRIPTION, "Pricing Data")
                .set(DATA_TYPE.NAME, "Pricing Data")
                .set(DATA_TYPE.ID, 3000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "PAYMENT")
                .set(DATA_TYPE.DESCRIPTION, "Payment Data")
                .set(DATA_TYPE.NAME, "Transactions etc.")
                .set(DATA_TYPE.ID, 4000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "UNKNOWN")
                .set(DATA_TYPE.DESCRIPTION, "Unknown")
                .set(DATA_TYPE.NAME, "Unknown")
                .set(DATA_TYPE.ID, 1L)
                .set(DATA_TYPE.UNKNOWN, true)
                .execute();
    }

    private void makeFlows(Workbook workbook) {
        removeFlows();
        Map<String, Long> nameToIdMap = loadAppNameToIdMap(workbook);

        Set<FlowRow> flowRows = streamRows(workbook, SheetDefinition.FLOWS)
                .skip(1)
                .filter(r -> r.getCell(0) != null)
                .filter(r -> r.getCell(5) != null)
                .map(FlowRow::fromRow)
                .collect(Collectors.toSet());

        Siphon<Tuple2<String, String>> unknownSourceSiphon = mkSiphon(t -> !nameToIdMap.containsKey(t.v1));
        Siphon<Tuple2<String, String>> unknownTargetSiphon = mkSiphon(t -> !nameToIdMap.containsKey(t.v2));

        Set<LogicalFlowRecord> logicalFlowRecords = flowRows.stream()
                .filter(r -> r.status() != EntityLifecycleStatus.REMOVED)
                .map(r -> tuple(r.sourceAppName(), r.targetAppName()))
                .distinct()
                .filter(unknownSourceSiphon)
                .filter(unknownTargetSiphon)
                .map(t -> t.map((srcName, trgName) -> tuple(nameToIdMap.get(srcName), nameToIdMap.get(trgName))))
                .filter(t -> t.v1 != null && t.v2 != null)
                .map(t -> {
                    LogicalFlowRecord record = dsl.newRecord(LOGICAL_FLOW);
                    record.setSourceEntityKind(EntityKind.APPLICATION.name());
                    record.setSourceEntityId(t.v1);
                    record.setTargetEntityKind(EntityKind.APPLICATION.name());
                    record.setTargetEntityId(t.v2);
                    record.setLastUpdatedAt(nowUtcTimestamp());
                    record.setLastUpdatedBy("admin");
                    record.setProvenance(PROVENANCE);
                    return record;
                })
                .collect(Collectors.toSet());

        log("Unknown sources: %d\n", unknownSourceSiphon.getResults().size());
        log("Unknown targets: %d\n", unknownTargetSiphon.getResults().size());

        int[] rc = dsl.batchInsert(logicalFlowRecords).execute();

        dsl.insertInto(LOGICAL_FLOW_DECORATOR)
                .columns(
                        LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID,
                        LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND,
                        LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID,
                        LOGICAL_FLOW_DECORATOR.RATING,
                        LOGICAL_FLOW_DECORATOR.PROVENANCE,
                        LOGICAL_FLOW_DECORATOR.LAST_UPDATED_BY)
                .select(DSL
                        .select(
                                LOGICAL_FLOW.ID,
                                DSL.val(EntityKind.DATA_TYPE.name()),
                                DSL.val(1L),
                                DSL.val("Z"),
                                DSL.val(PROVENANCE),
                                DSL.val("admin"))
                        .from(LOGICAL_FLOW))
                .execute();

        log("Created: %d logical flows\n", rc.length);
    }


    private Long makeComponentCategory() {
        Long componentCategoryId = getOrCreateMeasurableCategory(dsl, "COMPONENT", "Components");
        log("Component taxonomy category_id: %d\n", componentCategoryId);
        return componentCategoryId;
    }


    private void makeAppToComponentMappings(Workbook workbook, Long categoryId) {

        removeRatings();

        Map<String, Long> componentExtToIdMap = loadMeasurableExtToIdMap();
        Map<String, Long> appExtToIdMap = loadAppExtToIdMap();

        Siphon<Tuple2<String, ComponentRow>> unknownAppSiphon = mkSiphon(t -> !appExtToIdMap.containsKey(t.v1));
        Siphon<Tuple2<Long, String>> unknownComponentSiphon = mkSiphon(t -> !componentExtToIdMap.containsKey(t.v2));

        List<MeasurableRatingRecord> ratingRecords = streamRows(workbook, SheetDefinition.COMPONENT)
                .skip(1)
                .map(r -> tuple(strVal(r, 0), ComponentRow.fromRow(r)))
                .filter(unknownAppSiphon)
                .map(t -> t.map1(appExtId -> appExtToIdMap.get(appExtId)))
                .map(t -> t.map2(component -> toId(component.internalId(), component.tier(), component.layer())))
                .filter(unknownComponentSiphon)
                .map(t -> t.map2(componentExtId -> componentExtToIdMap.get(componentExtId)))
                .filter(t -> t.v1 != null && t.v2 != null)
                .map(t -> {
                    MeasurableRatingRecord record = dsl.newRecord(MEASURABLE_RATING);
                    record.setMeasurableId(t.v2);
                    record.setEntityId(t.v1);
                    record.setEntityKind(EntityKind.APPLICATION.name());
                    record.setRating("G");
                    record.setLastUpdatedAt(nowUtcTimestamp());
                    record.setLastUpdatedBy("admin");
                    record.setProvenance(PROVENANCE);
                    return record;
                })
                .collect(Collectors.toList());

        System.out.printf("Unknown Apps: %d\n", unknownAppSiphon.getResults().size());
        System.out.printf("Unknown Components: %d\n", unknownComponentSiphon.getResults().size());

        int[] rc = dsl.batchStore(ratingRecords).execute();

        log("Created: %d app mappings to components\n", rc.length);
    }

    private void removeRatings() {
        dsl.deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.PROVENANCE.eq(PROVENANCE))
                .execute();
    }


    private int makeOrgUnits(Workbook workbook) {
        final long l1Groups = 10;
        final long l1Offset = 10;
        final long l2Offset = 100L;

        int delCount = removeOrgUnits();
        log("OrgUnits deleted: %d\n", delCount);

        List<OrganisationalUnitRecord> records = new ArrayList();

        records.add(mkOrgUnitRecord(1L, null, "Group", "ROOT"));

        for (int i = 0; i < l1Groups; i++) {
            records.add(mkOrgUnitRecord(i + l1Offset, 1L, "Org Unit " + i, "OU_" + i));
        }

        AtomicLong idProvider = new AtomicLong(l2Offset);

        records.addAll(streamRows(workbook, SheetDefinition.BUSINESS_SUPPORT)
                .map(BusinessSupportRow::fromRow)
                .map(r -> tuple(r.orgObjectId(), r.orgObjectName()))
                .distinct()
                .filter(t -> t.v1 != null && t.v2 != null)
                .map(t -> {
                    long id = idProvider.getAndIncrement();
                    return mkOrgUnitRecord(id, l1Offset + (id % l1Groups), t.v2, t.v1);
                })
                .collect(Collectors.toList()));

        int[] rc = dsl
                .batchInsert(records)
                .execute();

        log("OrgUnits created: %d\n", rc.length);

        return rc.length;
    }


    private OrganisationalUnitRecord mkOrgUnitRecord(long id, Long parentId, String name, String extId) {
        OrganisationalUnitRecord record = dsl.newRecord(ORGANISATIONAL_UNIT);
        record.setId(id);
        record.setParentId(parentId);
        record.setExternalId(extId);
        record.setDescription("Sample OrgUnit: "+ name);
        record.setName(name);
        record.setCreatedAt(nowUtcTimestamp());
        record.setLastUpdatedAt(nowUtcTimestamp());
        return record;
    }


    private void makeComponentTaxonomy(Workbook workbook, Long categoryId) {
        int delCount = removeCategory(categoryId);
        log("Components deleted: %d\n", delCount);

        List<ComponentRow> componentRows = streamRows(workbook, SheetDefinition.COMPONENT)
                .skip(1)
                .map(ComponentRow::fromRow)
                .collect(Collectors.toList());

        mkTiers(componentRows, categoryId);
        mkLayers(componentRows, categoryId);
        mkComponents(componentRows, categoryId);
        updateParentIds(categoryId);
    }


    private void makeApps(Workbook workbook) {

        removeApps();

        List<ApplicationRecord> records = streamRows(workbook, SheetDefinition.APPLICATION)
                .skip(1)
                .map(ApplicationRow::fromRow)
                .map(ar -> {
                    Timestamp plannedRetirement = isEmpty(ar.endDate())
                        ? null
                        : Timestamp.valueOf(ar.endDate());

                    ApplicationRecord appRecord = dsl.newRecord(APPLICATION);
                    appRecord.setName("(" + ar.externalId() + ") " + ar.name());
                    appRecord.setAssetCode(ar.externalId());
                    appRecord.setKind(ApplicationKind.IN_HOUSE.name());
                    appRecord.setDescription(toDescription(ar.name(), ar.externalId(), ar.internalId()));
                    appRecord.setOverallRating("A");
                    appRecord.setOrganisationalUnitId(100L);
                    appRecord.setPlannedRetirementDate(plannedRetirement);
                    appRecord.setIsRemoved(ar.lifecyclePhase() == LifecyclePhase.RETIRED);
                    appRecord.setLifecyclePhase(ar.lifecyclePhase().name());
                    appRecord.setCreatedAt(nowUtcTimestamp());
                    appRecord.setUpdatedAt(nowUtcTimestamp());
                    appRecord.setProvenance(PROVENANCE);

                    return appRecord;
                })
                .collect(Collectors.toList());

        int[] rc = dsl.batchStore(records).execute();
        log("Apps: %d\n", rc.length);
    }


    // --- util ----


    private Map<String, Long> loadAppNameToIdMap(Workbook workbook) {
        Map<String, String> appNameToExtIdMap = streamRows(workbook, SheetDefinition.APPLICATION)
                .skip(1)
                .collect(toMap(
                        r -> strVal(r, Columns.C),
                        r -> strVal(r, Columns.A)));

        return MapUtilities.compose(appNameToExtIdMap, loadAppExtToIdMap());
    }


    private Map<String, Long> loadAppObjectIdToIdMap(Workbook workbook) {
        Map<String, String> appObjectIdToExtIdMap = streamRows(workbook, SheetDefinition.APPLICATION)
                .skip(1)
                .collect(toMap(
                        r -> strVal(r, Columns.B),
                        r -> strVal(r, Columns.A)));

        return MapUtilities.compose(appObjectIdToExtIdMap, loadAppExtToIdMap());
    }

    private MeasurableRecord newMeasurableRecord(DSLContext dsl, Long categoryId) {
        MeasurableRecord record = dsl.newRecord(MEASURABLE);
        record.setProvenance(PROVENANCE);
        record.setLastUpdatedBy("admin");
        record.setLastUpdatedAt(nowUtcTimestamp());
        record.setMeasurableCategoryId(categoryId);
        record.setConcrete(false);
        return record;
    }


    private Map<String, Long> makeExtToIdMap(List<Tuple2<Long, String>> xs) {
        return indexBy(
                t -> t.v2,
                t -> t.v1,
                xs);
    }


    private Map<String, Long> loadOrgExtToIdMap() {
        return dsl.select(ORGANISATIONAL_UNIT.EXTERNAL_ID, ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .stream()
                .collect(Collectors.toMap(r -> r.value1(), r -> r.value2()));

    }


    private Map<String, Long> loadChangeExtToIdMap() {
        return makeExtToIdMap(dsl
                .select(CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.EXTERNAL_ID)
                .from(CHANGE_INITIATIVE)
                .fetch(r -> tuple(
                        r.get(CHANGE_INITIATIVE.ID),
                        r.get(CHANGE_INITIATIVE.EXTERNAL_ID))));
    }


    private Map<String, Long> loadAppExtToIdMap() {
        return makeExtToIdMap(dsl
                .select(APPLICATION.ID, APPLICATION.ASSET_CODE)
                .from(APPLICATION)
                .fetch(r -> tuple(
                        r.get(APPLICATION.ID),
                        r.get(APPLICATION.ASSET_CODE))));
    }


    private Map<String, Long> loadMeasurableExtToIdMap() {
        return makeExtToIdMap(dsl
                .select(MEASURABLE.ID, MEASURABLE.EXTERNAL_ID)
                .from(MEASURABLE)
                .fetch(r -> tuple(
                        r.get(MEASURABLE.ID),
                        r.get(MEASURABLE.EXTERNAL_ID))));
    }


    private int updateParentIds(Long categoryId) {
        List<Tuple3<Long, String, String>> rs = dsl
                .select(MEASURABLE.ID, MEASURABLE.EXTERNAL_ID, MEASURABLE.EXTERNAL_PARENT_ID)
                .from(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .fetch(r -> tuple(
                        r.get(MEASURABLE.ID),
                        r.get(MEASURABLE.EXTERNAL_ID),
                        r.get(MEASURABLE.EXTERNAL_PARENT_ID)));

        Map<String, Long> extToId = indexBy(
                t -> t.v2,
                t -> t.v1,
                rs);

        Set<UpdateConditionStep<MeasurableRecord>> updates = rs
                .stream()
                .filter(t -> !isEmpty(t.v3))
                .map(t -> tuple(t.v1, extToId.get(t.v3)))
                .filter(t -> t.v2 != null)
                .map(t -> dsl
                        .update(MEASURABLE)
                        .set(MEASURABLE.PARENT_ID, t.v2)
                        .where(MEASURABLE.ID.eq(t.v1))
                        .and(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId)))
                .collect(Collectors.toSet());

        int[] rc = dsl
                .batch(updates)
                .execute();

        log("Updated parent ids for: %d records in category: %d\n", rc.length, categoryId);

        return rc.length;
    }


    private String toDescription(String... bits) {
        return String.join(" / ", bits);
    }


    private void log(String template, Object... args) {
        System.out.printf(template, args);
    }


    private <X> boolean all(Predicate<X> p, X... xs) {
        return Stream
                .of(xs)
                .allMatch(p);
    }

    private int removeCategory(Long categoryId) {
        return dsl
                .deleteFrom(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .execute();
    }


    private int removeAllAppToProjectMappings() {
        int rc = dsl.deleteFrom(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.PROVENANCE.eq(PROVENANCE))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name())
                        .or(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .execute();

        return rc;
    }


    private int removeAllProjects() {
        return dsl
                .deleteFrom(CHANGE_INITIATIVE)
                .execute();
    }


    private int removeApps() {
        return dsl
                .deleteFrom(APPLICATION)
                .where(APPLICATION.PROVENANCE.eq(PROVENANCE))
                .execute();
    }


    private int removeOrgUnits() {
        return dsl
                .deleteFrom(ORGANISATIONAL_UNIT)
                .execute();
    }


    private int removeFlows() {
        return dsl
                .deleteFrom(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.PROVENANCE.eq(PROVENANCE))
                .execute();
    }


    private Set<MeasurableRecord> mkComponents(List<ComponentRow> componentRows, Long categoryId) {
        Set<MeasurableRecord> tiers = componentRows
                .stream()
                .distinct()
                .filter(r -> all(x -> !isEmpty(x), r.tier(), r.layer(), r.name()))
                .map(r -> {
                    String componentName = String.format("(%s) %s", r.internalId(), r.name());
                    MeasurableRecord record = newMeasurableRecord(dsl, categoryId);
                    record.setName(componentName);
                    record.setConcrete(true);
                    record.setDescription(toDescription(r.tier(), r.layer(), " ( " + r.category() + " )"));
                    record.setExternalId(toId(r.internalId(), r.tier(), r.layer()));
                    record.setExternalParentId(WaltzUtilities.toId(r.tier(), r.layer()));
                    return record;
                })
                .collect(Collectors.toSet());

        int[] inserts = dsl.batchInsert(tiers).execute();
        log("Leaf Components inserted: %d\n", inserts.length);
        return tiers;
    }


    private Set<MeasurableRecord> mkLayers(List<ComponentRow> componentRows, Long categoryId) {
        Set<MeasurableRecord> tiers = componentRows
                .stream()
                .map(r -> tuple(r.tier(), r.layer()))
                .distinct()
                .filter(t -> all(x -> !isEmpty(x), t.v1, t.v2))
                .map(t -> {
                    MeasurableRecord record = newMeasurableRecord(dsl, categoryId);
                    record.setName(t.v2);
                    record.setDescription(toDescription(t.v1, t.v2));
                    record.setExternalId(WaltzUtilities.toId(t.v1, t.v2));
                    record.setExternalParentId(toId(t.v1));
                    return record;
                })
                .collect(Collectors.toSet());

        int[] inserts = dsl.batchInsert(tiers).execute();
        log("Tiers inserted: %d\n", inserts.length);
        return tiers;
    }


    private Set<MeasurableRecord> mkTiers(List<ComponentRow> componentRows, Long categoryId) {
        Set<MeasurableRecord> tiers = componentRows
                .stream()
                .map(r -> r.tier())
                .distinct()
                .filter(t -> !isEmpty(t))
                .map(t -> {
                    MeasurableRecord record = newMeasurableRecord(dsl, categoryId);
                    record.setName(t);
                    record.setDescription(t);
                    record.setExternalId(toId(t));
                    return record;
                })
                .collect(Collectors.toSet());

        int[] inserts = dsl.batchInsert(tiers).execute();
        log("Layers inserted: %d\n", inserts.length);
        return tiers;
    }

    // --- main ----


    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        XlsImporter importer = ctx.getBean(XlsImporter.class);

        String filename = "clients\\c1\\sc1\\full_anonymous_data_v1.0.xlsx";
        importer.load(filename);
    }

}
