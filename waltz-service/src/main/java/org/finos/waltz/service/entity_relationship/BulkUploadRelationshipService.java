package org.finos.waltz.service.entity_relationship;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.*;
import org.finos.waltz.model.bulk_upload.entity_relationship.*;
import org.finos.waltz.model.bulk_upload.measurable_rating.ChangeOperation;
import org.finos.waltz.model.entity_relationship.EntityRelationship;
import org.finos.waltz.model.entity_relationship.ImmutableEntityRelationship;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.rel.RelationshipKind;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.ChangeLogRecord;
import org.finos.waltz.schema.tables.records.EntityRelationshipRecord;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.relationship_kind.RelationshipKindService;
import org.finos.waltz.service.user.UserRoleService;
import org.jooq.DSLContext;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.data.JooqUtilities.loadExternalIdToEntityRefMap;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class BulkUploadRelationshipService {

    private static final Logger LOG = LoggerFactory.getLogger(BulkUploadRelationshipService.class);

    private final RelationshipKindService relationshipKindService;

    private final  org.finos.waltz.schema.tables.EntityRelationship ENTITY_RELATIONSHIP = Tables.ENTITY_RELATIONSHIP;

    private static final String PROVENANCE = "bulkUploadRelationships";

    private final EntityRelationshipService entityRelationshipService;

    private final UserRoleService userRoleService;

    private final DSLContext dsl;


    public BulkUploadRelationshipService(RelationshipKindService relationshipKindService,
                                         EntityRelationshipService entityRelationshipService,
                                         UserRoleService userRoleService,
                                         DSLContext dsl) {
        this.relationshipKindService = relationshipKindService;
        this.entityRelationshipService = entityRelationshipService;
        this.userRoleService = userRoleService;
        this.dsl = dsl;
    }

    public BulkUploadRelationshipValidationResult bulkPreview(String input, Long relationshipKindId) {

        RelationshipKind relationshipKind = relationshipKindService.getById(relationshipKindId);
        BulkUploadRelationshipParsedResult parsedResult = new BulkUploadRelationshipItemParser().parse(input, BulkUploadRelationshipItemParser.InputFormat.TSV);
        Map<String, EntityReference> sourceEntityRefMap = new HashMap<>();
        Map<String, EntityReference> targetEntityRefMap = new HashMap<>();

        if (parsedResult.error() != null) {
            return ImmutableBulkUploadRelationshipValidationResult
                    .builder()
                    .parseError(parsedResult.error())
                    .build();
        } else {
            // load source entity ref map
            if (relationshipKind.categoryA() != null) {
                sourceEntityRefMap = loadExternalIdToEntityRefMap(dsl, relationshipKind.kindA(), mkRef(EntityKind.MEASURABLE_CATEGORY, relationshipKind.categoryA()));
            } else {
                sourceEntityRefMap = loadExternalIdToEntityRefMap(dsl, relationshipKind.kindA());
            }

            // load target entity ref map
            if (relationshipKind.categoryB() != null) {
                targetEntityRefMap = loadExternalIdToEntityRefMap(dsl, relationshipKind.kindB(), mkRef(EntityKind.MEASURABLE_CATEGORY, relationshipKind.categoryB()));
            } else {
                targetEntityRefMap = loadExternalIdToEntityRefMap(dsl, relationshipKind.kindB());
            }

            // enrich each row to a tuple
            final Map<String, EntityReference> finalSourceEntityRefMap = sourceEntityRefMap;
            final Map<String, EntityReference> finalTargetEntityRefMap = targetEntityRefMap;
            final List<Tuple4<BulkUploadRelationshipItem, EntityReference, EntityReference, Set<ValidationError>>> listOfRows = parsedResult.parsedItems().stream()
                    .map(r -> tuple(
                            r,
                            finalSourceEntityRefMap.get(r.sourceExternalId()),
                            finalTargetEntityRefMap.get(r.targetExternalId())
                    ))
                    .map(t -> {
                        Set<ValidationError> validationErrors = new HashSet<>();
                        if (t.v2 == null) {
                            validationErrors.add(ValidationError.SOURCE_NOT_FOUND);
                        }
                        if (t.v3 == null) {
                            validationErrors.add(ValidationError.TARGET_NOT_FOUND);
                        }
                        return t.concat(validationErrors);
                    })
                    .collect(Collectors.toList());

            // load all entity relationships for the given kind
            final Collection<EntityRelationship> relationshipsForRelationshipKind = entityRelationshipService.getEntityRelationshipsByKind(relationshipKind);
            List<EntityRelationship> filteredRelationshipsForKind = relationshipsForRelationshipKind
                    .stream()
                    .filter(r -> r.relationship().equals(relationshipKind.code()) && r.a().kind().equals(relationshipKind.kindA()) && r.b().kind().equals(relationshipKind.kindB()))
                    .collect(Collectors.toList());

            List<EntityRelationship> requiredRelationship = listOfRows.stream()
                    .filter(t -> t.v4.isEmpty())
                    .map(t -> ImmutableEntityRelationship
                            .builder()
                            .a(t.v2)
                            .b(t.v3)
                            .relationship(relationshipKind.code())
                            .description(t.v1.description())
                            .lastUpdatedBy("abc.xyz")
                            .provenance("dummy")
                            .lastUpdatedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            // create a diff b/w required and existing relationships
            DiffResult<EntityRelationship> diffResult = DiffResult
                    .mkDiff(
                            filteredRelationshipsForKind,
                            requiredRelationship,
                            r -> tuple(r.a(), r.b()),
                            (a, b) -> StringUtilities.safeEq(b.description(), a.description())
                    );

            Set<Tuple2<EntityReference, EntityReference>> toAdd = SetUtilities.map(diffResult.otherOnly(), d -> tuple(d.a(), d.b()));
            Set<Tuple2<EntityReference, EntityReference>> toUpdate = SetUtilities.map(diffResult.differingIntersection(), d -> tuple(d.a(), d.b()));

            List<BulkUploadRelationshipValidatedItem> validatedItemsList = listOfRows
                    .stream()
                    .map(t -> {
                        if (toAdd.contains(tuple(t.v2, t.v3))) {
                            return t.concat(UploadOperation.ADD);
                        } else if (toUpdate.contains(tuple(t.v2, t.v3))) {
                            return t.concat(UploadOperation.UPDATE);
                        }
                        return t.concat(UploadOperation.NONE);
                    })
                    .map(t -> ImmutableBulkUploadRelationshipValidatedItem
                            .builder()
                            .parsedItem(t.v1)
                            .sourceEntityRef(t.v2)
                            .targetEntityRef(t.v3)
                            .description(t.v1.description())
                            .error(t.v4)
                            .uploadOperation(t.v5)
                            .build())
                    .collect(Collectors.toList());
            return ImmutableBulkUploadRelationshipValidationResult
                    .builder()
                    .validatedItems(validatedItemsList)
                    .build();
        }
    }

    public BulkUploadRelationshipApplyResult bulkApply(BulkUploadRelationshipValidationResult preview, Long relationshipKindId, String user) {
        verifyUserHasPermissions(user);

        if (preview.parseError() != null) {
            throw new IllegalStateException("Cannot apply changes with formatting errors");
        }

        RelationshipKind relationshipKind = relationshipKindService.getById(relationshipKindId);

        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        final Set<EntityRelationshipRecord> toInsert = preview
                .validatedItems()
                .stream()
                .filter(d -> d.uploadOperation() == UploadOperation.ADD && d.error().isEmpty())
                .map(d -> {
                    EntityRelationshipRecord r = new EntityRelationshipRecord();
                    r.setIdA(d.sourceEntityRef().id());
                    r.setIdB(d.targetEntityRef().id());
                    r.setKindA(d.sourceEntityRef().kind().name());
                    r.setKindB(d.targetEntityRef().kind().name());
                    r.setRelationship(relationshipKind.code());
                    r.setDescription(d.description());
                    r.setProvenance(PROVENANCE);
                    r.setLastUpdatedAt(now);
                    r.setLastUpdatedBy(user);
                    return r;

                })
                .collect(Collectors.toSet());

        final Set<UpdateConditionStep<EntityRelationshipRecord>> toUpdate = preview
                .validatedItems()
                .stream()
                .filter(d -> d.uploadOperation() == UploadOperation.UPDATE && d.error().isEmpty())
                .map(d -> DSL
                        .update(ENTITY_RELATIONSHIP)
                        .set(ENTITY_RELATIONSHIP.DESCRIPTION, d.description())
                        .set(ENTITY_RELATIONSHIP.LAST_UPDATED_AT, now)
                        .set(ENTITY_RELATIONSHIP.LAST_UPDATED_BY, user)
                        .where(ENTITY_RELATIONSHIP.RELATIONSHIP.eq(relationshipKind.code()))
                            .and(ENTITY_RELATIONSHIP.KIND_A.eq(relationshipKind.kindA().name()))
                            .and(ENTITY_RELATIONSHIP.KIND_B.eq(relationshipKind.kindB().name()))
                            .and(ENTITY_RELATIONSHIP.ID_A.eq(d.sourceEntityRef().id()))
                            .and(ENTITY_RELATIONSHIP.ID_B.eq(d.targetEntityRef().id())))
                .collect(Collectors.toSet());

        final Set<ChangeLogRecord> auditLogs = preview
                .validatedItems()
                .stream()
                .filter(d -> d.uploadOperation() != UploadOperation.NONE)
                .map(d -> {
                    ChangeLogRecord r = new ChangeLogRecord();
                    r.setMessage(mkChangeMessage(d.sourceEntityRef(),
                            d.targetEntityRef(),
                            relationshipKind,
                            d.uploadOperation()));
                    r.setOperation(toChangeLogOperation(d.uploadOperation()).name());
                    r.setParentKind(relationshipKind.code());
                    r.setParentId(relationshipKindId);
                    r.setCreatedAt(now);
                    r.setUserId(user);
                    r.setSeverity(Severity.INFORMATION.name());
                    return r;
                })
                .collect(Collectors.toSet());

        final long skippedRows = preview
                .validatedItems()
                .stream()
                .filter(d -> d.uploadOperation() == UploadOperation.NONE || !d.error().isEmpty())
                .count();

        return dsl
                .transactionResult(ctx -> {
                    DSLContext tx = ctx.dsl();
                    long insertCount = summarizeResults(tx.batchInsert(toInsert).execute());
                    long updateCount = summarizeResults(tx.batch(toUpdate).execute());

                    long changeLogCount = summarizeResults(tx.batchInsert(auditLogs).execute());

                    LOG.info(
                            "Batch Relationships: {} adds, {} updates, {} changelogs.",
                            insertCount,
                            updateCount,
                            changeLogCount
                    );

                    return ImmutableBulkUploadRelationshipApplyResult
                            .builder()
                            .recordsAdded(insertCount)
                            .recordsUpdated(updateCount)
                            .skippedRows(skippedRows)
                            .build();
                });
    }

    private String mkChangeMessage(EntityReference sourceRef, EntityReference targetRef, RelationshipKind relationshipKind, UploadOperation uploadOperation) {
        return format(
                "Bulk Relationships Update - Operation %s, Relationship b/w %s -> %s, Relationship Kind %s. %s",
                uploadOperation,
                sourceRef.name(),
                targetRef.name(),
                relationshipKind.id(),
                relationshipKind.code()
        );
    }

    private Operation toChangeLogOperation(UploadOperation uploadOperation) {
        switch (uploadOperation) {
            case ADD:
                return Operation.ADD;
            case UPDATE:
                return Operation.UPDATE;
            default:
                return Operation.UNKNOWN;
        }
    }

    private void verifyUserHasPermissions(String userId) {
        if (!userRoleService.hasRole(userId, SystemRole.ADMIN.name())) {
            throw new NotAuthorizedException();
        }
    }

//    public static void main(String[] args) {
//        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
//        DSLContext dsl = ctx.getBean(DSLContext.class);
//        System.out.println(dsl.select().from(Tables.MEASURABLE).fetch());
//    }
}
