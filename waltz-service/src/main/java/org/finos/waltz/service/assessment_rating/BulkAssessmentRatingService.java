package org.finos.waltz.service.assessment_rating;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.CommonTableFieldsRegistry;
import org.finos.waltz.model.*;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.assessment_rating.ImmutableAssessmentRating;
import org.finos.waltz.model.assessment_rating.bulk_upload.*;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.measurable_rating.ImmutableBulkMeasurableRatingApplyResult;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.finos.waltz.schema.tables.records.ChangeLogRecord;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.service.user.UserRoleService;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.StringUtilities.sanitizeCharacters;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.data.SelectorUtilities.getAllDistinctByKeyWithExclusion;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class BulkAssessmentRatingService {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurableRatingService.class);
    private static final String PROVENANCE = "bulkAssessmentDefinitionUpdate";
    private static final String DUMMY_USER = "test";

    private final AssessmentDefinitionService assessmentDefinitionService;

    private final AssessmentRatingService assessmentRatingService;
    
    private final RatingSchemeService ratingSchemeService;

    private UserRoleService userRoleService;

    private final DSLContext dsl;

    private final org.finos.waltz.schema.tables.AssessmentRating ar = Tables.ASSESSMENT_RATING;


    @Autowired
    public BulkAssessmentRatingService(AssessmentDefinitionService assessmentDefinitionService, AssessmentRatingService assessmentRatingService, RatingSchemeService ratingSchemeService, UserRoleService userRoleService, DSLContext dsl) {
        this.assessmentDefinitionService = assessmentDefinitionService;
        this.assessmentRatingService = assessmentRatingService;
        this.ratingSchemeService = ratingSchemeService;
        this.userRoleService = userRoleService;
        this.dsl = dsl;
    }

    public AssessmentRatingValidationResult bulkPreview(EntityReference assessmentReference,
                                                        String inputStr,
                                                        BulkAssessmentRatingItemParser.InputFormat format,
                                                        BulkUpdateMode mode) {

        AssessmentRatingParsedResult result = new BulkAssessmentRatingItemParser().parse(inputStr, format);
        if (result.error() != null) {
            return ImmutableAssessmentRatingValidationResult
                    .builder()
                    .error(result.error())
                    .build();
        }

        AssessmentDefinition assessmentDefinition = assessmentDefinitionService.getById(assessmentReference.id());
        Set<RatingSchemeItem> ratingSchemeItemsBySchemeIds = ratingSchemeService.findRatingSchemeItemsBySchemeIds(asSet(assessmentDefinition.ratingSchemeId()));
        Map<String, RatingSchemeItem> ratingSchemeItemsByCode = indexBy(ratingSchemeItemsBySchemeIds, RatingSchemeItem::rating);


        CommonTableFields<?> ctf = CommonTableFieldsRegistry.determineCommonTableFields(assessmentDefinition.entityKind(), "target");
        Set<ImmutableEntityReference> immutableEntityKinds = dsl.select(ctf.nameField(), ctf.idField(), ctf.externalIdField())
                .from(ctf.table())
                .where(ctf.isActiveCondition()
                .and(ctf.externalIdField().isNotNull()))
                .fetch()
                .stream()
                .map(m -> ImmutableEntityReference.builder()
                        .kind(ctf.entityKind())
                        .id(m.get(ctf.idField()))
                        .name(Optional.ofNullable(m.get(ctf.nameField())))
                        .externalId(m.get(ctf.externalIdField()))
                        .build())
                .collect(Collectors.toSet());


        Map<String, ImmutableEntityReference> kindsByExternalIds = indexBy(immutableEntityKinds, k -> k.externalId().get());
        boolean isCardinalityZeroToOne = assessmentDefinition.cardinality().equals(Cardinality.ZERO_ONE);
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        List<Tuple4<AssessmentRatingParsedItem, ImmutableEntityReference, RatingSchemeItem, Set<ValidationError>>> validatedEntries = result
                .parsedItems()
                .stream()
                .map(d -> {
                    ImmutableEntityReference immutableEntityReference = kindsByExternalIds.get(d.externalId());
                    RatingSchemeItem ratingSchemeItem = ratingSchemeItemsByCode.get(String.valueOf(d.ratingCode()));
                    return tuple(d, immutableEntityReference, ratingSchemeItem);
                })
                .map(t -> {
                    Set<ValidationError> validationErrors = new HashSet<>();
                    if(isCardinalityZeroToOne) {
                        if(!seen.contains(t.v1.externalId())) {
                            seen.add(t.v1.externalId());
                        } else {
                            validationErrors.add(ValidationError.DUPLICATE);
                        }
                    }
                    if (t.v2() == null) {
                        validationErrors.add(ValidationError.ENTITY_KIND_NOT_FOUND);
                    }
                    if (t.v3() == null) {
                        validationErrors.add(ValidationError.RATING_NOT_FOUND);
                    }
                    if (t.v3 != null && !t.v3.userSelectable()) {
                        validationErrors.add(ValidationError.RATING_NOT_USER_SELECTABLE);
                    }
                    return t.concat(validationErrors);
                })
                .collect(Collectors.toList());

        List<AssessmentRating> requiredAssessmentRatings = validatedEntries
                .stream()
                .filter(t -> t.v2 != null && t.v3 != null)
                .map(t -> ImmutableAssessmentRating
                        .builder()
                        .entityReference(mkRef(t.v2.kind(), t.v2.id()))
                        .assessmentDefinitionId(assessmentReference.id())
                        .ratingId(t.v3.id().get())
                        .comment(t.v1.comment())
                        .lastUpdatedBy(DUMMY_USER)
                        .provenance(PROVENANCE)
                        .isReadOnly(t.v1.isReadOnly())
                        .build())
                .collect(Collectors.toList());

        List<AssessmentRating> existingAssessmentRatings = assessmentRatingService.findByDefinitionId(assessmentReference.id());

        DiffResult<AssessmentRating> assessmentRatingDiffResult = DiffResult
                .mkDiff(
                        existingAssessmentRatings,
                        requiredAssessmentRatings,
                        d -> tuple(d.entityReference(), d.assessmentDefinitionId()),
                        (a, b) -> StringUtilities.safeEq(a.comment(), b.comment())
                                && a.ratingId() == b.ratingId());

        Set<Tuple2<EntityReference, Long>> toAdd = SetUtilities.map(assessmentRatingDiffResult.otherOnly(), d -> tuple(d.entityReference(), d.ratingId()));
        Set<Tuple2<EntityReference, Long>> toUpdate = SetUtilities.map(assessmentRatingDiffResult.differingIntersection(), d -> tuple(d.entityReference(), d.ratingId()));
        Set<Tuple2<EntityReference, Long>> toRemove = SetUtilities.map(assessmentRatingDiffResult.waltzOnly(), d -> tuple(d.entityReference(), d.ratingId()));


        List<AssessmentRatingValidatedItem> validatedItems = validatedEntries
                .stream()
                .map(t -> {
                    boolean isInValid = t.v2 == null || t.v3 == null;

                    if (!isInValid) {
                        Tuple2<EntityReference, Long> recordKey = tuple(mkRef(t.v2.kind(), t.v2.id()), t.v3.id().get());

                        if (toAdd.contains(recordKey)) {
                            return t.concat(ChangeOperation.ADD);
                        }
                        if (toUpdate.contains(recordKey)) {
                            return t.concat(ChangeOperation.UPDATE);
                        }
                    }
                    return t.concat(ChangeOperation.NONE);
                })
                .map(t -> ImmutableAssessmentRatingValidatedItem
                        .builder()
                        .changeOperation(t.v5)
                        .errors(t.v4)
                        .entityKindReference(t.v2)
                        .ratingSchemeItem(t.v3)
                        .parsedItem(t.v1)
                        .build())
                .collect(Collectors.toList());

        return ImmutableAssessmentRatingValidationResult
                .builder()
                .validatedItems(validatedItems)
                .removals(mode == BulkUpdateMode.REPLACE
                        ? toRemove
                        : emptySet())
                .build();
    }

    public BulkAssessmentRatingApplyResult apply(EntityReference assessmentRef,
                                                 AssessmentRatingValidationResult preview,
                                                 BulkUpdateMode mode,
                                                 String userId) {

        verifyUserHasPermissions(userId);

        if (preview.error() != null) {
            throw new IllegalStateException("Cannot apply changes with formatting errors");
        }
        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        AssessmentDefinition assessmentDefinition = assessmentDefinitionService.getById(assessmentRef.id());

        Set<AssessmentRatingRecord> toAdd = preview
                .validatedItems()
                .stream()
                .filter(d -> d.changeOperation() == ChangeOperation.ADD && d.errors().isEmpty())
                .map(d -> {
                    AssessmentRatingRecord record = new AssessmentRatingRecord();
                    record.setEntityKind(d.entityKindReference().kind().name());
                    record.setAssessmentDefinitionId(assessmentRef.id());
                    record.setEntityId(d.entityKindReference().id());
                    record.setRatingId(d.ratingSchemeItem().id().get());
                    record.setDescription(sanitizeCharacters(d.parsedItem().comment()));
                    record.setLastUpdatedBy(userId);
                    record.setLastUpdatedAt(now);
                    record.setProvenance(PROVENANCE);
                    record.setIsReadonly(d.parsedItem().isReadOnly());
                    return record;
                })
                .collect(Collectors.toSet());

        Set<UpdateConditionStep<AssessmentRatingRecord>> toUpdate = preview
                    .validatedItems()
                    .stream()
                    .filter(d -> d.changeOperation() == ChangeOperation.UPDATE && d.errors().isEmpty())
                    .map(d ->
                            DSL
                            .update(ar)
                            .set(ar.RATING_ID, d.ratingSchemeItem().id().get())
                            .set(ar.DESCRIPTION, sanitizeCharacters(d.parsedItem().comment()))
                            .where(ar.ASSESSMENT_DEFINITION_ID.eq(assessmentRef.id())
                                    .and(ar.ENTITY_KIND.eq(d.entityKindReference().kind().name())
                                            .and(ar.ENTITY_ID.eq(d.entityKindReference().id()))
                                            .and(ar.RATING_ID.eq(d.ratingSchemeItem().id().get())))))
                    .collect(Collectors.toSet());

        Set<DeleteConditionStep<AssessmentRatingRecord>> toRemove = preview
                .removals()
                .stream()
                .map(d ->
                        DSL
                        .deleteFrom(ar)
                        .where(ar.ENTITY_KIND.eq(d.v1.kind().name()))
                        .and(ar.ENTITY_ID.eq(d.v1.id()))
                        .and(ar.ASSESSMENT_DEFINITION_ID.eq(assessmentRef.id())
                                .and(ar.RATING_ID.eq(d.v2))))
                .collect(Collectors.toSet());

        Map<Long, RatingSchemeItem> ratingItemsById = indexBy(
                ratingSchemeService.findRatingSchemeItemsByAssessmentDefinition(assessmentRef.id()),
                r -> r.id().orElse(0L));

        Set<ChangeLogRecord> auditLogs = Stream.concat(
                        preview
                                .removals()
                                .stream()
                                .map(t -> {
                                    RatingSchemeItem rsi = ratingItemsById.get(t.v2);
                                    ChangeLogRecord r = new ChangeLogRecord();
                                    r.setMessage(format(
                                            "Bulk Rating Update - Removed assessment rating for: %s/%s (%d)",
                                            rsi == null ? "?" : rsi.name(),
                                            rsi == null ? "?" : rsi.externalId().orElse("-"),
                                            t.v2));
                                    r.setOperation(Operation.REMOVE.name());
                                    r.setParentKind(t.v1.kind().name());
                                    r.setParentId(t.v1.id());
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
                                    r.setMessage(mkChangeMessage(d.ratingSchemeItem(), d.changeOperation()));
                                    r.setOperation(toChangeLogOperation(d.changeOperation()).name());
                                    r.setParentKind(EntityKind.APPLICATION.name());
                                    r.setParentId(d.entityKindReference().id());
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

        return dsl
                .transactionResult(ctx -> {
                    DSLContext tx = ctx.dsl();
                    int insertCount = summarizeResults(tx.batchInsert(toAdd).execute());
                    int updateCount = summarizeResults(tx.batch(toUpdate).execute());
                    int removalCount = mode == BulkUpdateMode.REPLACE
                            ? summarizeResults(tx.batch(toRemove).execute())
                            : 0;
                    int changeLogCount = summarizeResults(tx.batchInsert(auditLogs).execute());

                    LOG.info(
                            "Batch assessment rating: {} adds, {} updates, {} removes, {} changeLogs",
                            insertCount,
                            updateCount,
                            removalCount,
                            changeLogCount);

//                    throw new RuntimeException("Boooomrahhh!!!");
                    return ImmutableBulkAssessmentRatingApplyResult
                            .builder()
                            .recordsAdded(insertCount)
                            .recordsUpdated(updateCount)
                            .recordsRemoved(removalCount)
                            .skippedRows((int) skipCount)
                            .build();
                });
    }

    private String mkChangeMessage(RatingSchemeItem ratingSchemeItem,
                                   ChangeOperation changeOperation) {
        return format(
                "Bulk Rating Update - Operation: %s, assessment rating for: %s/%s",
                changeOperation,
                ratingSchemeItem.name(),
                ratingSchemeItem.externalId().orElse("?"));
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
        if (!userRoleService.hasRole(userId, SystemRole.ASSESSMENT_DEFINITION_ADMIN.name())) {
            throw new NotAuthorizedException();
        }
    }
}


/**
 * Preview:
 * Check for user selectable for rating
 */