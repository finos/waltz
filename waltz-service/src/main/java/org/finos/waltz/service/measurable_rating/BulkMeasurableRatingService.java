package org.finos.waltz.service.measurable_rating;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.model.DiffResult;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.measurable_rating.*;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.AllocationRecord;
import org.finos.waltz.schema.tables.records.ChangeLogRecord;
import org.finos.waltz.schema.tables.records.MeasurableRatingRecord;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.service.user.UserRoleService;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.StringUtilities.parseInteger;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.schema.Tables.ALLOCATION;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.finos.waltz.service.allocation.AllocationUtilities.mkBasicLogEntry;

@Service
public class BulkMeasurableRatingService {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurableRatingService.class);
    private static final String PROVENANCE = "bulkMeasurableRatingUpdate";
    private static final String DUMMY_USER = "test";

    private final UserRoleService userRoleService;
    private final MeasurableRatingDao measurableRatingDao;
    private final RatingSchemeService ratingSchemeService;
    private final MeasurableService measurableService;
    private final ChangeLogService changeLogService;
    private final MeasurableCategoryService measurableCategoryService;
    private final ApplicationDao applicationDao;
    private final DSLContext dsl;

    private final org.finos.waltz.schema.tables.MeasurableRating mr = Tables.MEASURABLE_RATING;

    @Autowired
    public BulkMeasurableRatingService(UserRoleService userRoleService,
                                       MeasurableRatingDao measurableRatingDao,
                                       RatingSchemeService ratingSchemeService,
                                       MeasurableService measurableService,
                                       ChangeLogService changeLogService, MeasurableCategoryService measurableCategoryService,
                                       ApplicationDao applicationDao,
                                       DSLContext dsl) {
        this.userRoleService = userRoleService;
        this.measurableRatingDao = measurableRatingDao;
        this.ratingSchemeService = ratingSchemeService;
        this.measurableService = measurableService;
        this.changeLogService = changeLogService;
        this.measurableCategoryService = measurableCategoryService;
        this.applicationDao = applicationDao;
        this.dsl = dsl;
    }


    public BulkMeasurableRatingValidationResult bulkPreview(EntityReference categoryRef,
                                                            String inputStr,
                                                            BulkMeasurableItemParser.InputFormat format,
                                                            BulkUpdateMode mode) {

        BulkMeasurableRatingParseResult result = new BulkMeasurableItemParser().parse(inputStr, format);
        if (result.error() != null) {
            return ImmutableBulkMeasurableRatingValidationResult
                    .builder()
                    .error(result.error())
                    .build();
        }
        MeasurableCategory category = measurableCategoryService.getById(categoryRef.id());
        List<Measurable> existingMeasurables = measurableService.findByCategoryId(categoryRef.id());
        Map<String, Measurable> existingByExtId = indexBy(existingMeasurables, m -> m.externalId().orElse(null));

        List<Application> allApplications = applicationDao.findAll();

        Map<String, Application> allApplicationsByAssetCode = indexBy(allApplications, a -> a.assetCode()
                .map(ExternalIdValue::value)
                .map(StringUtilities::lower)
                .orElse(""));

        Set<RatingSchemeItem> ratingSchemeItemsBySchemeIds = ratingSchemeService.findRatingSchemeItemsBySchemeIds(asSet(category.ratingSchemeId()));
        Map<String, RatingSchemeItem> ratingSchemeItemsByCode = indexBy(ratingSchemeItemsBySchemeIds, RatingSchemeItem::rating);

        Map<Optional<ExternalIdValue>, Integer> allocationMap = new HashMap<>();
        List<BulkMeasurableRatingItem> parsedResult = result
                .parsedItems()
                .stream()
                .map(d -> {
                    Application application = allApplicationsByAssetCode.get(d.assetCode().toLowerCase());
                    if (application != null && application.assetCode().isPresent()) {
                        allocationMap.put(
                                application.assetCode(),
                                allocationMap.getOrDefault(application.assetCode(), 0) + d.allocation());
                    }
                    return d;
                }).collect(Collectors.toList());

        List<Tuple5<BulkMeasurableRatingItem, Application, Measurable, RatingSchemeItem, Set<ValidationError>>> validatedEntries = parsedResult
                .stream()
                .map(d -> {
                    Application application = allApplicationsByAssetCode.get(d.assetCode().toLowerCase());
                    Measurable measurable = existingByExtId.get(d.taxonomyExternalId());
                    RatingSchemeItem ratingSchemeItem = ratingSchemeItemsByCode.get(String.valueOf(d.ratingCode()));
                    return tuple(d, application, measurable, ratingSchemeItem);
                })
                .map(t -> {
                    Set<ValidationError> validationErrors = new HashSet<>();
                    if (t.v2 == null) {
                        validationErrors.add(ValidationError.APPLICATION_NOT_FOUND);
                    }
                    if (t.v3 == null) {
                        validationErrors.add(ValidationError.MEASURABLE_NOT_FOUND);
                    }
                    if (t.v4 == null) {
                        validationErrors.add(ValidationError.RATING_NOT_FOUND);
                    }
                    if (t.v3 != null && !t.v3.concrete()) {
                        validationErrors.add(ValidationError.MEASURABLE_NOT_CONCRETE);
                    }
                    if (t.v4 != null && !t.v4.userSelectable()) {
                        validationErrors.add(ValidationError.RATING_NOT_USER_SELECTABLE);
                    }
                    if(parseInteger(t.v1.allocation().toString(), -1) < 0) {
                        validationErrors.add(ValidationError.ALLOCATION_NOT_VALID);
                    }
                    if(t.v2 != null && allocationMap.get(t.v2.assetCode()) > 100) {
                        validationErrors.add(ValidationError.ALLOCATION_EXCEEDING);
                    }

                    return t.concat(validationErrors);
                })
                .collect(Collectors.toList());

        Collection<MeasurableRating> existingRatings = measurableRatingDao.findByCategory(category.id().get());

        List<MeasurableRating> requiredRatings = validatedEntries
                .stream()
                .filter(t -> t.v2 != null && t.v3 != null && t.v4 != null)
                .map(t -> ImmutableMeasurableRating
                        .builder()
                        .entityReference(t.v2.entityReference())
                        .measurableId(t.v3.id().get())
                        .description(t.v1.comment())
                        .rating(t.v1.ratingCode())
                        .isPrimary(t.v1.isPrimary())
                        .lastUpdatedBy(DUMMY_USER)
                        .provenance(PROVENANCE)
                        .build())
                .collect(Collectors.toList());

        DiffResult<MeasurableRating> diff = DiffResult
                .mkDiff(
                        existingRatings,
                        requiredRatings,
                        d -> tuple(d.entityReference(), d.measurableId()),
                        (a, b) -> a.isPrimary() == b.isPrimary()
                                && StringUtilities.safeEq(a.description(), b.description())
                                && a.rating() == b.rating());

        Set<Tuple2<EntityReference, Long>> toAdd = SetUtilities.map(diff.otherOnly(), d -> tuple(d.entityReference(), d.measurableId()));
        Set<Tuple2<EntityReference, Long>> toUpdate = SetUtilities.map(diff.differingIntersection(), d -> tuple(d.entityReference(), d.measurableId()));
        Set<Tuple2<EntityReference, Long>> toRemove = SetUtilities.map(diff.waltzOnly(), d -> tuple(d.entityReference(), d.measurableId()));

        //group similar applications together
        Map<String, List<Tuple5<BulkMeasurableRatingItem, Application, Measurable, RatingSchemeItem, Set<ValidationError>>>> groupedValidatedEntries = validatedEntries
                .stream()
                .collect(Collectors.groupingBy(t -> t.v1.assetCode()));

        List<BulkMeasurableRatingValidatedItem> validatedItems = groupedValidatedEntries
            .values()
            .stream()
            .flatMap(List::stream)
            //.filter(t -> t.v2 != null && t.v3 != null)
            .map(t -> {
                boolean eitherAppOrMeasurableIsMissing = t.v2 == null || t.v3 == null;

                if (eitherAppOrMeasurableIsMissing) {
                    return t.concat(ChangeOperation.NONE);
                } else {
                    Tuple2<EntityReference, Long> recordKey = tuple(t.v2.entityReference(), t.v3.id().get());
                    if (toAdd.contains(recordKey)) {
                        return t.concat(ChangeOperation.ADD);
                    }
                    if (toUpdate.contains(recordKey)) {
                        return t.concat(ChangeOperation.UPDATE);
                    }
                    return t.concat(ChangeOperation.NONE);
                }
            })
            .map(t -> ImmutableBulkMeasurableRatingValidatedItem
                    .builder()
                    .changeOperation(t.v6)
                    .errors(t.v5)
                    .application(t.v2)
                    .measurable(t.v3)
                    .ratingSchemeItem(t.v4)
                    .parsedItem(t.v1)
                    .build())
            .collect(Collectors.toList());


        return ImmutableBulkMeasurableRatingValidationResult
                .builder()
                .validatedItems(validatedItems)
                .removals(mode == BulkUpdateMode.REPLACE
                        ? toRemove
                        : emptySet())
                .build();
    }


    public BulkMeasurableRatingApplyResult apply(EntityReference categoryRef,
                                                 BulkMeasurableRatingValidationResult preview,
                                                 BulkUpdateMode mode,
                                                 String userId) {

        verifyUserHasPermissions(userId);

        if (preview.error() != null) {
            throw new IllegalStateException("Cannot apply changes with formatting errors");
        }

        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        Set<MeasurableRatingRecord> toInsert = preview
                .validatedItems()
                .stream()
                .filter(d -> d.changeOperation() == ChangeOperation.ADD && d.errors().isEmpty())
                .map(d -> {
                    MeasurableRatingRecord r = new MeasurableRatingRecord();
                    r.setEntityKind(EntityKind.APPLICATION.name());
                    r.setEntityId(d.application().id().get());
                    r.setMeasurableId(d.measurable().id().get());
                    r.setRating(d.ratingSchemeItem().rating());
                    r.setDescription(d.parsedItem().comment());
                    r.setIsPrimary(d.parsedItem().isPrimary());
                    r.setLastUpdatedBy(userId);
                    r.setLastUpdatedAt(now);
                    r.setProvenance(PROVENANCE);
                    return r;
                })
                .collect(Collectors.toSet());

        Set<UpdateConditionStep<MeasurableRatingRecord>> toUpdate = preview
                .validatedItems()
                .stream()
                .filter(d -> d.changeOperation() == ChangeOperation.UPDATE && d.errors().isEmpty())
                .map(d -> DSL
                        .update(mr)
                        .set(mr.RATING, d.ratingSchemeItem().rating())
                        .set(mr.DESCRIPTION, d.parsedItem().comment())
                        .set(mr.IS_PRIMARY, d.parsedItem().isPrimary())
                        .set(mr.LAST_UPDATED_AT, now)
                        .set(mr.LAST_UPDATED_BY, userId)
                        .where(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                                .and(mr.ENTITY_ID.eq(d.application().id().get()))
                                .and(mr.MEASURABLE_ID.eq(d.measurable().id().get()))))
                .collect(Collectors.toSet());

        Set<DeleteConditionStep<MeasurableRatingRecord>> toRemove = preview
                .removals()
                .stream()
                .map(d -> DSL
                        .delete(mr)
                        .where(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                                .and(mr.ENTITY_ID.eq(d.v1.id()))
                                .and(mr.MEASURABLE_ID.eq(d.v2))))
                .collect(Collectors.toSet());

        Map<Long, Measurable> measurablesById = indexBy(
                measurableService.findByCategoryId(categoryRef.id()),
                m -> m.id().get());

        Set<ChangeLogRecord> auditLogs = Stream.concat(
                preview
                    .removals()
                    .stream()
                    .map(t -> {
                        Measurable m = measurablesById.get(t.v2);
                        ChangeLogRecord r = new ChangeLogRecord();
                        r.setMessage(format(
                                "Bulk Rating Update - Removed measurable rating for: %s/%s (%d)",
                                m == null ? "?" : m.name(),
                                m == null ? "?" : m.externalId().orElse("-"),
                                t.v2));
                        r.setOperation(Operation.REMOVE.name());
                        r.setParentKind(EntityKind.APPLICATION.name());
                        r.setParentId(t.v1().id());
                        r.setCreatedAt(now);
                        r.setUserId(userId);
                        r.setSeverity(Severity.INFORMATION.name());
                        return r;
                    }),
                preview
                    .validatedItems()
                    .stream()
                    .filter(d -> d.changeOperation() != ChangeOperation.NONE)
                    .map(d -> {
                        ChangeLogRecord r = new ChangeLogRecord();
                        r.setMessage(mkChangeMessage(d.measurable(), d.changeOperation()));
                        r.setOperation(toChangeLogOperation(d.changeOperation()).name());
                        r.setParentKind(EntityKind.APPLICATION.name());
                        r.setParentId(d.application().id().get());
                        r.setCreatedAt(now);
                        r.setUserId(userId);
                        r.setSeverity(Severity.INFORMATION.name());
                        return r;
                    }))
                .collect(Collectors.toSet());

        long skipCount = preview
                .validatedItems()
                .stream()
                .filter(d -> d.changeOperation() == ChangeOperation.NONE || !d.errors().isEmpty())
                .count();

        updateAllocation(dsl, categoryRef, preview, userId);

        return dsl
                .transactionResult(ctx -> {
                    DSLContext tx = ctx.dsl();
                    int insertCount = summarizeResults(tx.batchInsert(toInsert).execute());
                    int updateCount = summarizeResults(tx.batch(toUpdate).execute());
                    int removalCount = mode == BulkUpdateMode.REPLACE
                            ? summarizeResults(tx.batch(toRemove).execute())
                            : 0;
                    int changeLogCount = summarizeResults(tx.batchInsert(auditLogs).execute());

                    LOG.info(
                            "Batch measurable rating: {} adds, {} updates, {} removes, {} changeLogs",
                            insertCount,
                            updateCount,
                            removalCount,
                            changeLogCount);

                    return ImmutableBulkMeasurableRatingApplyResult
                            .builder()
                            .recordsAdded(insertCount)
                            .recordsUpdated(updateCount)
                            .recordsRemoved(removalCount)
                            .skippedRows((int) skipCount)
                            .build();
                });

//        throw new RuntimeException("Boooom");
    }

    /**
     * This method will remove the existing allocations and add new validated allocations.
     *
     * @param dsl
     * @param categoryRef
     * @param ratings
     * @param userId
     */
    private void updateAllocation(DSLContext dsl, EntityReference categoryRef, BulkMeasurableRatingValidationResult ratings, String userId) {

        LOG.info("Initiating updating allocations");
        Collection<Measurable> existingMeasurables = measurableService.findByCategoryId(categoryRef.id());
        Map<Long, Measurable> existingById = indexBy(existingMeasurables, m -> m.id().get());

        //Remove all existing allocations
        Map<Optional<Long>, List<BulkMeasurableRatingValidatedItem>> appToRatingsMap = ratings
                .validatedItems()
                .stream()
                .collect(Collectors.groupingBy(t -> t.application().id()));

        dsl.transaction(tx -> {
            DSLContext txDsl = tx.dsl();

            List<DeleteConditionStep<AllocationRecord>> allocationsToRemove = appToRatingsMap
                    .keySet()
                    .stream()
                    .map(entityId -> mkExistingMeasurableAllocationRemovals(dsl,
                            2,
                            entityId.orElse(-1L)))
                    .collect(Collectors.toList());


            //Add validated allocations
            List<AllocationRecord> allocationsToAdd = ratings
                    .validatedItems()
                    .stream()
                    .map(d -> mkMeasurableRatingAllocationRecordsToAdd(dsl,
                            2,
                            d.measurable().id().orElse(-1L),
                            d.parsedItem().allocation(),
                            userId))
                    .collect(Collectors.toList());

            if(!allocationsToRemove.isEmpty()) {
                List<Long> allocationRemovedForMeasurables = appToRatingsMap
                        .keySet()
                        .stream()
                        .map(entityId -> getExistingMeasurableAllocations(txDsl, entityId.orElse(-1L)))
                        .flatMap(Set::stream)
                        .collect(Collectors.toList());

                int[] removed = txDsl.batch(allocationsToRemove).execute();
                LOG.info(format("Removed %d allocations", removed));

                List<ChangeLog> removalChangeLogs = allocationRemovedForMeasurables
                        .stream()
                        .map(ratingId -> {
                            Measurable measurable = existingById.get(ratingId);
                            String removalMessage = format(
                                    "Unallocated measurable '%s'",
                                    measurable.kind().name());
                            return mkBasicLogEntry(measurable.entityReference(), removalMessage, userId);
                        })
                        .collect(Collectors.toList());
                if(removalChangeLogs.size() > 0) {
                    changeLogService.write(removalChangeLogs);
                }
            }

            if(!allocationsToAdd.isEmpty()) {
                List<ChangeLog> insertChangeLogs = ratings
                        .validatedItems()
                        .stream()
                        .map(d -> {
                            Measurable measurable = d.measurable();
                            String insertMessage = format(
                                    "Set allocation for measurable '%s' to %d%% ",
                                    measurable.kind().name(),
                                    d.parsedItem().allocation());
                            return mkBasicLogEntry(measurable.entityReference(), insertMessage, userId);
                        })
                        .collect(Collectors.toList());
                LOG.info(String.valueOf(insertChangeLogs));

                txDsl.batchInsert(allocationsToAdd)
                        .execute();
                changeLogService.write(insertChangeLogs);
            }
        });
    }

    private DeleteConditionStep<AllocationRecord> mkExistingMeasurableAllocationRemovals(DSLContext dsl,
                                                                                         long scheme,
                                                                                         Long entityId) {

        Set<Long> existingMeasurableRatings = dsl
                .select(MEASURABLE_RATING.ID)
                .from(MEASURABLE_RATING)
                .innerJoin(ALLOCATION)
                .on(ALLOCATION.MEASURABLE_RATING_ID.eq(MEASURABLE_RATING.ID))
                .where(MEASURABLE_RATING.ENTITY_ID.eq(entityId))
                .fetchSet(MEASURABLE_RATING.ID);

        return dsl
                .deleteFrom(ALLOCATION)
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(scheme)
                        .and(ALLOCATION.MEASURABLE_RATING_ID.in(existingMeasurableRatings)));
    }

    private Set<Long> getExistingMeasurableAllocations(DSLContext dsl, Long entityId) {
        Set<Long> ratingIds = dsl
                .select(MEASURABLE_RATING.MEASURABLE_ID)
                .from(MEASURABLE_RATING)
                .innerJoin(ALLOCATION)
                .on(ALLOCATION.MEASURABLE_RATING_ID.eq(MEASURABLE_RATING.ID))
                .where(MEASURABLE_RATING.ENTITY_ID.eq(entityId))
                .fetchSet(MEASURABLE_RATING.MEASURABLE_ID);
        return ratingIds;
    }

    private AllocationRecord mkMeasurableRatingAllocationRecordsToAdd(DSLContext dsl,
                                                                      long scheme,
                                                                      long measurableRatingId,
                                                                      Integer allocation,
                                                                      String userId) {

        AllocationRecord record = dsl.newRecord(ALLOCATION);
        record.setAllocationSchemeId(scheme);
        record.setMeasurableRatingId(measurableRatingId);
        record.setAllocationPercentage(allocation);
        record.setLastUpdatedBy(userId);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setProvenance("waltz");
        return record;
    }


    private String mkChangeMessage(Measurable measurable,
                                   ChangeOperation changeOperation) {
        return format(
                "Bulk Rating Update - Operation: %s, measurable rating for: %s/%s",
                changeOperation,
                measurable.name(),
                measurable.externalId().orElse("?"));
    }


    private Operation toChangeLogOperation(ChangeOperation changeOperation) {
        switch (changeOperation) {
            case ADD:
                return Operation.ADD;
            case UPDATE:
                return Operation.UPDATE;
            case REMOVE:
                return Operation.REMOVE;
            default:
                return Operation.UNKNOWN;
        }
    }


    private void verifyUserHasPermissions(String userId) {
        if (!userRoleService.hasRole(userId, SystemRole.TAXONOMY_EDITOR.name())) {
            throw new NotAuthorizedException();
        }
    }

}