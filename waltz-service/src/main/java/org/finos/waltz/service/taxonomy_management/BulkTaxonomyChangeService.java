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

package org.finos.waltz.service.taxonomy_management;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.common.hierarchy.FlatNode;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.ChangeOperation;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyApplyResult;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyItem;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyParseResult;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidatedItem;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidationResult;
import org.finos.waltz.model.bulk_upload.taxonomy.ChangedFieldType;
import org.finos.waltz.model.bulk_upload.taxonomy.ImmutableBulkTaxonomyApplyResult;
import org.finos.waltz.model.bulk_upload.taxonomy.ImmutableBulkTaxonomyValidatedItem;
import org.finos.waltz.model.bulk_upload.taxonomy.ImmutableBulkTaxonomyValidationResult;
import org.finos.waltz.model.bulk_upload.taxonomy.ValidationError;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeLifecycleStatus;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeType;
import org.finos.waltz.schema.tables.records.MeasurableRecord;
import org.finos.waltz.schema.tables.records.TaxonomyChangeRecord;
import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyItemParser.InputFormat;
import org.finos.waltz.service.user.UserRoleService;
import org.jooq.DSLContext;
import org.jooq.UpdateConditionStep;
import org.jooq.UpdateSetStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.countBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.compact;
import static org.finos.waltz.common.SetUtilities.filter;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.common.StringUtilities.limit;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.common.StringUtilities.safeEq;
import static org.finos.waltz.common.hierarchy.HierarchyUtilities.hasCycle;
import static org.finos.waltz.common.hierarchy.HierarchyUtilities.toForest;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.verifyUserHasPermissions;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class BulkTaxonomyChangeService {

    private static final Logger LOG = LoggerFactory.getLogger(BulkTaxonomyChangeService.class);

    private final MeasurableCategoryService measurableCategoryService;
    private final MeasurableService measurableService;
    private final UserRoleService userRoleService;
    private final EntityHierarchyService entityHierarchyService;
    private final DSLContext dsl;


    @Autowired
    public BulkTaxonomyChangeService(MeasurableCategoryService measurableCategoryService,
                                     MeasurableService measurableService,
                                     UserRoleService userRoleService,
                                     EntityHierarchyService entityHierarchyService,
                                     DSLContext dsl) {
        this.measurableCategoryService = measurableCategoryService;
        this.measurableService = measurableService;
        this.userRoleService = userRoleService;
        this.entityHierarchyService = entityHierarchyService;
        this.dsl = dsl;
    }


    public BulkTaxonomyValidationResult previewBulk(EntityReference taxonomyRef,
                                                    String inputStr,
                                                    InputFormat format,
                                                    BulkUpdateMode mode) {
        if (taxonomyRef.kind() != EntityKind.MEASURABLE_CATEGORY) {
            throw new UnsupportedOperationException("Only measurable category bulk updates supported");
        }

        LOG.debug(
                "Bulk preview - category:{}, format:{}, inputStr:{}",
                taxonomyRef,
                format,
                limit(inputStr, 40));

        MeasurableCategory category = measurableCategoryService.getById(taxonomyRef.id());
        checkNotNull(category, "Unknown category: %d", taxonomyRef);

        List<Measurable> existingMeasurables = measurableService.findByCategoryId(
                taxonomyRef.id(),
                asSet(EntityLifecycleStatus.values()));

        Map<String, Measurable> existingByExtId = indexBy(existingMeasurables, m -> m.externalId().orElse(null));

        LOG.debug(
                "category: {} = {}({})",
                taxonomyRef,
                category.name(),
                category.externalId());

        BulkTaxonomyParseResult result = new BulkTaxonomyItemParser().parse(inputStr, format);

        if (result.error() != null) {
            return ImmutableBulkTaxonomyValidationResult
                    .builder()
                    .error(result.error())
                    .build();
        }

        Map<String, BulkTaxonomyItem> givenByExtId = indexBy(result.parsedItems(), BulkTaxonomyItem::externalId);
        Set<String> allExtIds = union(existingByExtId.keySet(), givenByExtId.keySet());

         /*
          Validation checks:

          - unique external ids
          - all parent external id's exist either in file or in existing taxonomy
          - check for cycles by converting to forest
          - do a diff to determine
              - new
              - removed, if mode == replace
              - updated, match on external id
         */

        Map<String, Long> countByExtId = countBy(result.parsedItems(), BulkTaxonomyItem::externalId);

        Boolean hasCycle = determineIfTreeHasCycle(existingMeasurables, result);

        List<BulkTaxonomyValidatedItem> validatedItems = result
                .parsedItems()
                .stream()
                .map(d -> tuple(
                        d,
                        existingByExtId.get(d.externalId())))  // => (parsedItem, existingItem?)
                .map(t -> {
                    Tuple2<ChangeOperation, Set<ChangedFieldType>> op = determineOperation(t.v1, t.v2);
                    boolean isUnique = countByExtId.get(t.v1.externalId()) == 1;
                    boolean parentExists = StringUtilities.isEmpty(t.v1.parentExternalId())
                            || allExtIds.contains(t.v1.parentExternalId());
                    boolean isCyclical = hasCycle;
                    return ImmutableBulkTaxonomyValidatedItem
                            .builder()
                            .parsedItem(t.v1)
                            .changedFields(op.v2)
                            .changeOperation(op.v1)
                            .existingItemReference(ofNullable(t.v2)
                                    .map(Measurable::entityReference)
                                    .orElse(null))
                            .errors(compact(
                                    isUnique ? null : ValidationError.DUPLICATE_EXT_ID,
                                    parentExists ? null : ValidationError.PARENT_NOT_FOUND,
                                    isCyclical ? ValidationError.CYCLE_DETECTED : null))
                            .build();
                })
                .collect(Collectors.toList());

        Set<Measurable> toRemove = mode == BulkUpdateMode.ADD_ONLY
                ? emptySet()
                : filter(existingMeasurables, m -> ! givenByExtId.containsKey(m.externalId().get()));

        return ImmutableBulkTaxonomyValidationResult
                .builder()
                .plannedRemovals(toRemove)
                .validatedItems(validatedItems)
                .build();
    }


    public BulkTaxonomyApplyResult applyBulk(EntityReference taxonomyRef,
                                             BulkTaxonomyValidationResult bulkRequest,
                                             String userId) {
        if (taxonomyRef.kind() != EntityKind.MEASURABLE_CATEGORY) {
            throw new UnsupportedOperationException("Only measurable category bulk updates supported");
        }
        verifyUserHasPermissions(measurableCategoryService, userRoleService, userId, taxonomyRef);
        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        Set<MeasurableRecord> toAdd = bulkRequest
                .validatedItems()
                .stream()
                .filter(d -> d.changeOperation() == ChangeOperation.ADD)
                .map(BulkTaxonomyValidatedItem::parsedItem)
                .map(bulkTaxonomyItem -> {
                    MeasurableRecord r = new MeasurableRecord();
                    r.setMeasurableCategoryId(taxonomyRef.id());
                    r.setConcrete(bulkTaxonomyItem.concrete());
                    r.setName(bulkTaxonomyItem.name());
                    r.setExternalId(bulkTaxonomyItem.externalId());
                    r.setExternalParentId(bulkTaxonomyItem.parentExternalId());
                    r.setDescription(bulkTaxonomyItem.description());
                    r.setLastUpdatedAt(now);
                    r.setLastUpdatedBy(userId);
                    r.setProvenance("waltz");
                    return r;
                })
                .collect(Collectors.toSet());

        Set<UpdateConditionStep<MeasurableRecord>> toRestore = bulkRequest
                .validatedItems()
                .stream()
                .filter(d -> d.changeOperation() == ChangeOperation.RESTORE)
                .map(d -> DSL
                        .update(org.finos.waltz.schema.tables.Measurable.MEASURABLE)
                        .set(org.finos.waltz.schema.tables.Measurable.MEASURABLE.ENTITY_LIFECYCLE_STATUS, EntityLifecycleStatus.ACTIVE.name())
                        .where(org.finos.waltz.schema.tables.Measurable.MEASURABLE.MEASURABLE_CATEGORY_ID.eq(taxonomyRef.id()))
                        .and(org.finos.waltz.schema.tables.Measurable.MEASURABLE.ID.eq(d.existingItemReference().id())))
                .collect(Collectors.toSet());

        Set<UpdateConditionStep<MeasurableRecord>> toUpdate = bulkRequest
                .validatedItems()
                .stream()
                .filter(d -> d.changeOperation() == ChangeOperation.UPDATE  || d.changeOperation() == ChangeOperation.RESTORE)
                .map(d -> {
                    BulkTaxonomyItem item = d.parsedItem();
                    UpdateSetStep<MeasurableRecord> upd = DSL.update(org.finos.waltz.schema.tables.Measurable.MEASURABLE);
                    if (d.changedFields().contains(ChangedFieldType.NAME)) {
                        upd = upd.set(org.finos.waltz.schema.tables.Measurable.MEASURABLE.NAME, item.name());
                    }
                    if (d.changedFields().contains(ChangedFieldType.PARENT_EXTERNAL_ID)) {
                        upd = StringUtilities.isEmpty(item.parentExternalId())
                                ? upd.setNull(org.finos.waltz.schema.tables.Measurable.MEASURABLE.EXTERNAL_PARENT_ID)
                                : upd.set(org.finos.waltz.schema.tables.Measurable.MEASURABLE.EXTERNAL_PARENT_ID, item.parentExternalId());
                    }
                    if (d.changedFields().contains(ChangedFieldType.DESCRIPTION)) {
                        upd = upd.set(org.finos.waltz.schema.tables.Measurable.MEASURABLE.DESCRIPTION, item.description());
                    }
                    if (d.changedFields().contains(ChangedFieldType.CONCRETE)) {
                        upd = upd.set(org.finos.waltz.schema.tables.Measurable.MEASURABLE.CONCRETE, item.concrete());
                    }
                    return upd
                            .set(org.finos.waltz.schema.tables.Measurable.MEASURABLE.LAST_UPDATED_AT, now)
                            .set(org.finos.waltz.schema.tables.Measurable.MEASURABLE.LAST_UPDATED_BY, userId)
                            .where(org.finos.waltz.schema.tables.Measurable.MEASURABLE.MEASURABLE_CATEGORY_ID.eq(taxonomyRef.id()))
                            .and(org.finos.waltz.schema.tables.Measurable.MEASURABLE.ID.eq(d.existingItemReference().id()));
                })
                .collect(Collectors.toSet());


        boolean requiresRebuild = requiresHierarchyRebuild(bulkRequest.validatedItems());

        BulkTaxonomyApplyResult changeResult = dsl
            .transactionResult(ctx -> {
                DSLContext tx = ctx.dsl();
                int insertCount = summarizeResults(tx.batchInsert(toAdd).execute());
                int restoreCount = summarizeResults(tx.batch(toRestore).execute());
                int updateCount = summarizeResults(tx.batch(toUpdate).execute());

                Set<TaxonomyChangeRecord> changeRecords = mkTaxonomyChangeRecords(tx, taxonomyRef, bulkRequest, userId);

                int insertedChangeRecordCount = summarizeResults(tx.batchInsert(changeRecords).execute());

                LOG.debug("Added {} new change record entries", insertedChangeRecordCount);

                if (requiresRebuild) {
                    int updatedParents = updateParentIdsFromExternalIds(tx, taxonomyRef.id());
                    LOG.debug("Updated parents: {}", updatedParents);
                }

                return ImmutableBulkTaxonomyApplyResult
                        .builder()
                        .recordsAdded(insertCount)
                        .recordsUpdated(updateCount)
                        .recordsRestored(restoreCount)
                        .recordsRemoved(0)//TODO: add removeCount
                        .hierarchyRebuilt(requiresRebuild)
                        .build();
            });

        LOG.debug("Result of apply changes: {}", changeResult);
        if (requiresRebuild) {
            int entriesCreated = entityHierarchyService.buildForMeasurableByCategory(taxonomyRef.id());
            LOG.debug("Recreated hierarchy with {} new entries", entriesCreated);
        }

        return changeResult;
    }


    private Set<TaxonomyChangeRecord> mkTaxonomyChangeRecords(DSLContext tx,
                                                              EntityReference taxonomyRef,
                                                              BulkTaxonomyValidationResult bulkRequest,
                                                              String user) {

        Map<String, Tuple2<Long, String>> extIdToIdMap = tx
                .select(MEASURABLE.EXTERNAL_ID, MEASURABLE.ID, MEASURABLE.NAME)
                .from(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(taxonomyRef.id()))
                .fetchMap(
                        MEASURABLE.EXTERNAL_ID,
                        r -> tuple(
                                r.get(MEASURABLE.ID),
                                r.get(MEASURABLE.NAME)));

        return bulkRequest
                .validatedItems()
                .stream()
                .flatMap(d -> toTaxonomyChangeRecords(
                        d,
                        taxonomyRef,
                        extIdToIdMap.get(d.parsedItem().externalId()),
                        extIdToIdMap.get(d.parsedItem().parentExternalId()),
                        user))
                .collect(Collectors.toSet());
    }


    private Stream<TaxonomyChangeRecord> toTaxonomyChangeRecords(BulkTaxonomyValidatedItem validatedItem,
                                                                 EntityReference taxonomyReference,
                                                                 Tuple2<Long, String> item,
                                                                 Tuple2<Long, String> parent, // may be null
                                                                 String user) {

        if (validatedItem.changeOperation() == ChangeOperation.ADD) {

            TaxonomyChangeRecord r = mkBaseTaxonomyChangeRecord(taxonomyReference, item.v1, user);
            r.setChangeType(TaxonomyChangeType.BULK_ADD.name());
            return Stream.of(r);

        } else if (validatedItem.changeOperation() == ChangeOperation.RESTORE || validatedItem.changeOperation() == ChangeOperation.UPDATE) {

            Stream<TaxonomyChangeRecord> fieldStream = validatedItem
                    .changedFields()
                    .stream()
                    .map(f -> {
                        TaxonomyChangeRecord r = mkBaseTaxonomyChangeRecord(taxonomyReference, item.v1, user);
                        switch (f) {
                            case NAME:
                                r.setChangeType(TaxonomyChangeType.UPDATE_NAME.name());
                                r.setParams(format(
                                        "{ \"name\": \"%s\", \"originalValue\": \"\" }",
                                        validatedItem.parsedItem().name()));
                                break;
                            case DESCRIPTION:
                                r.setChangeType(TaxonomyChangeType.UPDATE_DESCRIPTION.name());
                                r.setParams(format(
                                        "{ \"description\": \"%s\", \"originalValue\": \"\" }",
                                        validatedItem.parsedItem().description()));
                                break;
                            case CONCRETE:
                                r.setChangeType(TaxonomyChangeType.UPDATE_CONCRETENESS.name());
                                r.setParams(format(
                                        "{ \"concrete\": \"%s\" }",
                                        validatedItem.parsedItem().concrete()));
                                break;
                            case PARENT_EXTERNAL_ID:
                                r.setChangeType(TaxonomyChangeType.MOVE.name());
                                r.setParams(format(
                                        "{ \"destinationId\": \"%d\", \"destinationName\": \"%s\", \"originalValue\": \"\" }",
                                        parent == null ? 0 : parent.v1,
                                        parent == null ? "root" : parent.v2));
                                break;
                            default:
                                r.setChangeType(TaxonomyChangeType.BULK_UPDATE.name());
                        }
                        return r;
                    });

            return Stream.concat(
                    validatedItem.changeOperation() == ChangeOperation.RESTORE
                        ? Stream.of(mkRestoreRecord(taxonomyReference, item.v1, user))
                        : Stream.empty(),
                    fieldStream);

        } else {
            return Stream.empty();
        }
    }


    private TaxonomyChangeRecord mkRestoreRecord(EntityReference taxonomyReference,
                                                 Long itemId,
                                                 String user) {
        TaxonomyChangeRecord r = mkBaseTaxonomyChangeRecord(taxonomyReference, itemId, user);
        r.setChangeType(TaxonomyChangeType.BULK_RESTORE.name());
        return r;
    }


    private TaxonomyChangeRecord mkBaseTaxonomyChangeRecord(EntityReference taxonomyReference,
                                                         Long itemId,
                                                         String user) {
        TaxonomyChangeRecord r = new TaxonomyChangeRecord();
        r.setDomainKind(taxonomyReference.kind().name());
        r.setDomainId(taxonomyReference.id());
        r.setPrimaryReferenceKind(EntityKind.MEASURABLE.name());
        r.setPrimaryReferenceId(itemId);
        r.setStatus(TaxonomyChangeLifecycleStatus.EXECUTED.name());
        r.setParams("{}");
        r.setCreatedBy(user);
        r.setLastUpdatedBy(user);
        return r;
    }


    // --- HELPERS ----

    /**
     * This is achieved by doing a self join on the measurable table to find any matching parent and updating
     * the child parent_id with the matching parent's id
     */
    private int updateParentIdsFromExternalIds(DSLContext tx,
                                               long categoryId) {
        org.finos.waltz.schema.tables.Measurable parent = MEASURABLE.as("p");

        return tx
                .update(MEASURABLE)
                .set(MEASURABLE.PARENT_ID, DSL
                        .select(parent.ID)
                        .from(parent)
                        .where(parent.EXTERNAL_ID.eq(MEASURABLE.EXTERNAL_PARENT_ID)
                                .and(parent.MEASURABLE_CATEGORY_ID.eq(categoryId))))
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .execute();
    }


    public static boolean requiresHierarchyRebuild(Collection<BulkTaxonomyValidatedItem> items) {
        Set<ChangeOperation> opsThatNeedRebuild = asSet(
                ChangeOperation.ADD,
                ChangeOperation.REMOVE,
                ChangeOperation.RESTORE);

        Predicate<BulkTaxonomyValidatedItem> requiresRebuild = d -> {
            boolean deffoNeedsRebuild = opsThatNeedRebuild.contains(d.changeOperation());
            boolean opIsUpdate = d.changeOperation() == ChangeOperation.UPDATE;
            boolean parentIdChanged = d.changedFields().contains(ChangedFieldType.PARENT_EXTERNAL_ID);

            return deffoNeedsRebuild || (opIsUpdate && parentIdChanged);
        };

        return items
                .stream()
                .anyMatch(requiresRebuild);
    }


    private boolean determineIfTreeHasCycle(List<Measurable> existingMeasurables,
                               BulkTaxonomyParseResult result) {
        return Stream
                .concat(
                        existingMeasurables.stream().map(d -> new FlatNode<String, String>(
                                d.externalId().orElse(null),
                                d.externalParentId(),
                                null)),
                        result.parsedItems().stream().map(d -> new FlatNode<String, String>(
                                d.externalId(),
                                ofNullable(d.parentExternalId()),
                                null)))
                .collect(Collectors.collectingAndThen(toList(), xs -> hasCycle(toForest(xs))));
    }


    private Tuple2<ChangeOperation, Set<ChangedFieldType>> determineOperation(BulkTaxonomyItem requiredItem,
                                                                              Measurable existingItem) {
        if (existingItem == null) {
            return tuple(ChangeOperation.ADD, emptySet());
        }

        boolean isRestore = existingItem.entityLifecycleStatus() != EntityLifecycleStatus.ACTIVE;
        boolean nameMatches = safeEq(requiredItem.name(), existingItem.name());
        boolean descMatches = safeEq(requiredItem.description(), existingItem.description());
        boolean parentExtIdMatches = safeEq(mkSafe(requiredItem.parentExternalId()), mkSafe(existingItem.externalParentId().orElse(null)));
        boolean concreteMatches = requiredItem.concrete() == existingItem.concrete();

        Set<ChangedFieldType> changedFields = new HashSet<>();
        if (!nameMatches) { changedFields.add(ChangedFieldType.NAME); }
        if (!descMatches) { changedFields.add(ChangedFieldType.DESCRIPTION); }
        if (!parentExtIdMatches) { changedFields.add(ChangedFieldType.PARENT_EXTERNAL_ID); }
        if (!concreteMatches) { changedFields.add(ChangedFieldType.CONCRETE); }

        ChangeOperation op = isRestore
                ? ChangeOperation.RESTORE
                : changedFields.isEmpty()
                    ? ChangeOperation.NONE
                    : ChangeOperation.UPDATE;

        return tuple(op, changedFields);

    }


}
