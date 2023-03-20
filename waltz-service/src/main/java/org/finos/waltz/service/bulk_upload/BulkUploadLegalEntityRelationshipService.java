package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.*;
import org.finos.waltz.common.StreamUtilities.Siphon;
import org.finos.waltz.data.EntityAliasPopulator;
import org.finos.waltz.model.*;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.ResolvedAssessmentHeaderStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.*;
import org.finos.waltz.model.legal_entity.ImmutableLegalEntityRelationship;
import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.bulk_upload.TabularDataUtilities.Row;
import org.finos.waltz.service.bulk_upload.assessment_strategy.AssessmentStrategy;
import org.finos.waltz.service.bulk_upload.column_parsers.ColumnParser;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipKindService;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.UpdateConditionStep;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.ArrayUtilities.idx;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.*;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.common.StringUtilities.*;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.model.DiffResult.mkDiff;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentHeaderCell.mkHeader;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.LegalEntityRelationshipResolutionError.mkError;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.ResolvedReference.mkResolvedReference;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;
import static org.finos.waltz.service.bulk_upload.BulkUploadUtilities.getColumnValuesFromRows;
import static org.finos.waltz.service.bulk_upload.TabularDataUtilities.streamData;
import static org.finos.waltz.service.bulk_upload.assessment_strategy.AssessmentStrategy.determineStrategy;
import static org.finos.waltz.service.bulk_upload.assessment_strategy.AssessmentStrategy.mkAssessmentRatingRecord;
import static org.finos.waltz.service.bulk_upload.column_parsers.ColumnParser.sanitize;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class BulkUploadLegalEntityRelationshipService {

    private static final Logger LOG = LoggerFactory.getLogger(BulkUploadLegalEntityRelationshipService.class);
    private static final Set<String> FIXED_COL_HEADERS = asSet(LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER, LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER);
    private static final Set<String> RELATIONSHIP_COL_HEADERS = asSet(
            LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER,
            LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER,
            LegalEntityBulkUploadFixedColumns.COMMENT,
            LegalEntityBulkUploadFixedColumns.REMOVE_RELATIONSHIP);
    private final AssessmentDefinitionService assessmentDefinitionService;
    private final RatingSchemeService ratingSchemeService;
    private final EntityAliasPopulator entityAliasPopulator;
    private final LegalEntityRelationshipKindService legalEntityRelationshipKindService;
    private final LegalEntityRelationshipService legalEntityRelationshipService;

    private final AssessmentRatingService assessmentRatingService;

    private final DSLContext dsl;


    @Autowired
    public BulkUploadLegalEntityRelationshipService(AssessmentDefinitionService assessmentDefinitionService,
                                                    RatingSchemeService ratingSchemeService,
                                                    EntityAliasPopulator entityAliasPopulator,
                                                    LegalEntityRelationshipKindService legalEntityRelationshipKindService,
                                                    LegalEntityRelationshipService legalEntityRelationshipService,
                                                    AssessmentRatingService assessmentRatingService,
                                                    DSLContext dsl) {

        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(assessmentDefinitionService, "assessmentDefinitionService cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
        checkNotNull(entityAliasPopulator, "entityAliasPopulator cannot be null");
        checkNotNull(legalEntityRelationshipKindService, "legalEntityRelationshipKindService cannot be null");
        checkNotNull(legalEntityRelationshipService, "legalEntityRelationshipService cannot be null");
        checkNotNull(assessmentRatingService, "assessmentRatingService cannot be null");

        this.dsl = dsl;
        this.assessmentDefinitionService = assessmentDefinitionService;
        this.ratingSchemeService = ratingSchemeService;
        this.entityAliasPopulator = entityAliasPopulator;
        this.legalEntityRelationshipKindService = legalEntityRelationshipKindService;
        this.legalEntityRelationshipService = legalEntityRelationshipService;
        this.assessmentRatingService = assessmentRatingService;
    }

    public SaveBulkUploadLegalEntityRelationshipResponse save(BulkUploadLegalEntityRelationshipCommand uploadCommand, String username) {

        ResolveBulkUploadLegalEntityRelationshipResponse resolvedCmd = resolve(uploadCommand);

        Set<ResolvedLegalEntityRelationship> relationships = map(resolvedCmd.rows(), ResolvedUploadRow::legalEntityRelationship);


        return dsl
                .transactionResult(ctx -> {

                    DSLContext tx = ctx.dsl();

                    BulkChangeStatistics relationshipStats = handeRelationships(tx, relationships, uploadCommand.legalEntityRelationshipKindId(), username);

                    Set<SaveBulkUploadAssessmentStats> assessmentStats = handleAssessments(tx,
                            uploadCommand.legalEntityRelationshipKindId(),
                            resolvedCmd,
                            uploadCommand.updateMode(),
                            username);

                    return ImmutableSaveBulkUploadLegalEntityRelationshipResponse.builder()
                            .relationshipStats(relationshipStats)
                            .assessmentStats(assessmentStats)
                            .build();
                });
    }

    private Set<SaveBulkUploadAssessmentStats> handleAssessments(DSLContext tx,
                                                                 long relationshipKindId,
                                                                 ResolveBulkUploadLegalEntityRelationshipResponse resolvedCmd,
                                                                 BulkUpdateMode updateMode,
                                                                 String username) {

        Map<Long, AssessmentDefinition> definitionsById = resolvedCmd
                .assessmentHeaders()
                .stream()
                .map(d -> d.resolvedAssessmentDefinition().orElse(null))
                .filter(Objects::nonNull)
                .collect(toMap(k -> k.id().get(), v -> v, (a, b) -> a));

        Map<Tuple2<EntityReference, EntityReference>, EntityReference> existingRelsToIdMap = loadExistingTargetIdAndLegalEntityIdToRelIdMap(tx, relationshipKindId);

        Siphon<Tuple2<AssessmentCell, EntityReference>> noRelFoundSiphon = mkSiphon(t -> t.v2 == null);

        Map<Integer, Long> definitionsByColId = indexBy(
                resolvedCmd.assessmentHeaders(),
                AssessmentHeaderCell::columnId,
                d -> d.resolvedAssessmentDefinition()
                        .flatMap(IdProvider::id)
                        .orElse(null));

        Siphon<Tuple4<Long, EntityReference, Long, String>> noDefnFoundSiphon = mkSiphon(t -> t.v1 == null);

        List<Tuple2<EntityReference, Set<AssessmentCell>>> rows = resolvedCmd
                .rows()
                .stream()
                .filter(d -> CollectionUtilities.isEmpty(d.legalEntityRelationship().errors()))
                .filter(d -> asSet(UploadOperation.ADD, UploadOperation.UPDATE).contains(d.legalEntityRelationship().operation()))
                .map(r -> {

                    Tuple2<EntityReference, EntityReference> lookupKey = tuple(
                            r.legalEntityRelationship().targetEntityReference().resolvedEntityReference().orElse(null),
                            r.legalEntityRelationship().legalEntityReference().resolvedEntityReference().orElse(null));

                    return tuple(
                            existingRelsToIdMap.get(lookupKey),
                            r.assessmentRatings());
                })
                .collect(toList());

        Map<Long, Set<Tuple3<Long, Long, String>>> requiredRatingsByDefnId = rows
                .stream()
                .flatMap(t -> t.v2
                        .stream()
                        .map(r -> tuple(r, t.v1)))
                .filter(noRelFoundSiphon)
                .flatMap(t -> t.v1.ratings()
                        .stream()
                        .filter(r -> r.resolvedRating().isPresent())
                        .map(r -> tuple(
                                definitionsByColId.get(t.v1.columnId()),
                                t.v2,
                                r.resolvedRating()
                                        .flatMap(IdProvider::id)
                                        .orElse(null),
                                r.comment().orElse(null))))
                .filter(noDefnFoundSiphon)
                .collect(groupingBy(t -> t.v1, mapping(t -> tuple(t.v2.id(), t.v3, t.v4), toSet())));

        if (noRelFoundSiphon.hasResults()) {
            throw new IllegalStateException("Could not resolve the legal entity relationship for some of the assessments");
        }

        Map<Long, List<Tuple3<Long, Long, String>>> existingRatingsByDefinitionId = loadExistingAssessmentRatingsByDefnId(
                tx,
                resolvedCmd.assessmentHeaders(),
                map(rows, d -> d.v1.id()));

        return definitionsById
                .values()
                .stream()
                .map(def -> {
                    BulkChangeStatistics statistics = updateAssessmentRatings(
                            tx,
                            updateMode,
                            def,
                            fromCollection(existingRatingsByDefinitionId.getOrDefault(def.id().get(), emptyList())),
                            requiredRatingsByDefnId.getOrDefault(def.id().get(), emptySet()),
                            username);

                    return ImmutableSaveBulkUploadAssessmentStats.builder()
                            .definition(def)
                            .assessmentStatistics(statistics)
                            .build();
                })
                .collect(toSet());
    }

    private BulkChangeStatistics updateAssessmentRatings(DSLContext tx,
                                                         BulkUpdateMode updateMode,
                                                         AssessmentDefinition definition,
                                                         Set<Tuple3<Long, Long, String>> existingRatings,
                                                         Set<Tuple3<Long, Long, String>> requiredRatings,
                                                         String username) {

        AssessmentStrategy updateStrategy = determineStrategy(updateMode, definition.cardinality());

        return updateStrategy.apply(tx, definition, requiredRatings, existingRatings, username);

    }

    private Map<Long, List<Tuple3<Long, Long, String>>> loadExistingAssessmentRatingsByDefnId(DSLContext tx,
                                                                                              Set<AssessmentHeaderCell> assessmentHeaders,
                                                                                              Set<Long> inScopeRelationshipIds) {

        Set<Long> definitionIds = assessmentHeaders
                .stream()
                .map(d -> d.resolvedAssessmentDefinition()
                        .map(def -> def.id().get())
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(toSet());

        return tx
                .select(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID, ASSESSMENT_RATING.ENTITY_ID, ASSESSMENT_RATING.RATING_ID, ASSESSMENT_RATING.DESCRIPTION)
                .from(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.in(definitionIds)
                        .and(ASSESSMENT_RATING.ENTITY_ID.in(inScopeRelationshipIds)))
                .fetchGroups(
                        d -> d.get(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID),
                        d -> tuple(d.get(ASSESSMENT_RATING.ENTITY_ID), d.get(ASSESSMENT_RATING.RATING_ID), d.get(ASSESSMENT_RATING.DESCRIPTION)));

    }

    private BulkChangeStatistics handeRelationships(DSLContext tx,
                                                    Set<ResolvedLegalEntityRelationship> relationships,
                                                    long relKindId,
                                                    String username) {

        Set<ResolvedLegalEntityRelationship> validRelationships = filter(relationships, d -> CollectionUtilities.isEmpty(d.errors()));

        Set<ResolvedLegalEntityRelationship> additions = filter(validRelationships, r -> r.operation().equals(UploadOperation.ADD));
        Set<ResolvedLegalEntityRelationship> updates = filter(validRelationships, r -> r.operation().equals(UploadOperation.UPDATE));
        Set<ResolvedLegalEntityRelationship> removals = filter(validRelationships, r -> r.operation().equals(UploadOperation.REMOVE));

        Set<LegalEntityRelationship> relationshipsToAdd = map(additions, d -> mkRelationship(relKindId, d, username));
        Set<LegalEntityRelationship> relationshipsToUpdate = map(updates, d -> mkRelationship(relKindId, d, username));
        Set<LegalEntityRelationship> relationshipsToDelete = map(removals, d -> mkRelationship(relKindId, d, username));

        int added = legalEntityRelationshipService.bulkAdd(tx, relationshipsToAdd, username);
        int updated = legalEntityRelationshipService.bulkUpdate(tx, relationshipsToUpdate, username);
        int removed = legalEntityRelationshipService.bulkRemove(tx, relationshipsToDelete, username);

        BulkChangeStatistics stats = ImmutableBulkChangeStatistics.builder()
                .addedCount(added)
                .updatedCount(updated)
                .removedCount(removed)
                .build();

        LOG.info("Bulk legal entity upload: {}", stats);

        return stats;
    }


    private LegalEntityRelationship mkRelationship(long relKindId, ResolvedLegalEntityRelationship d, String username) {
        return ImmutableLegalEntityRelationship.builder()
                .targetEntityReference(d.targetEntityReference().resolvedEntityReference().get())
                .legalEntityReference(d.legalEntityReference().resolvedEntityReference().get())
                .relationshipKindId(relKindId)
                .description(d.comment().orElse(null))
                .lastUpdatedBy(username)
                .id(d.existingRelationshipReference().map(EntityReference::id))
                .build();
    }


    public ResolveBulkUploadLegalEntityRelationshipResponse resolve(BulkUploadLegalEntityRelationshipCommand uploadCommand) {

        LegalEntityRelationshipKind relKind = legalEntityRelationshipKindService.getById(uploadCommand.legalEntityRelationshipKindId());


        Set<String> headers = streamData(uploadCommand.inputString())
                .findFirst()
                .map(Row::getHeaders)
                .orElseThrow(() -> new IllegalStateException("No data provided"));

        if (!headers.containsAll(FIXED_COL_HEADERS)) {
            throw new IllegalStateException(format("Not all mandatory columns (%s) provided", StringUtilities.join(FIXED_COL_HEADERS, ", ")));
        }

        Set<AssessmentHeaderCell> resolvedHeaders = FunctionUtilities.time("headers", () -> parseAssessmentsFromHeader(
                mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, uploadCommand.legalEntityRelationshipKindId()),
                headers));

        Set<ResolvedUploadRow> resolvedRows = FunctionUtilities.time("rows", () -> parseRowData(
                streamData(uploadCommand.inputString()).collect(toSet()),
                relKind,
                resolvedHeaders));

        return ImmutableResolveBulkUploadLegalEntityRelationshipResponse.builder()
                .rows(resolvedRows)
                .assessmentHeaders(resolvedHeaders)
                .build();
    }

    private Set<ResolvedUploadRow> parseRowData(Set<Row> rows,
                                                LegalEntityRelationshipKind relationshipKind,
                                                Set<AssessmentHeaderCell> assessmentHeaders) {

        Map<Tuple2<EntityReference, EntityReference>, EntityReference> existingRelToIdMap = loadExistingTargetIdAndLegalEntityIdToRelIdMap(null, relationshipKind.id().get());

        //Rel ref -> [DefId, ratingId]
        Map<EntityReference, Collection<Tuple2<Long, Long>>> relationshipToExistingRatingsMap = loadRelationshipToExistingRatingsMap(relationshipKind);

        Map<String, EntityReference> targetIdentifierToIdMap = loadTargetIdentifierToReference(rows, relationshipKind);
        Map<String, EntityReference> legalEntityIdentifierToIdMap = loadLegalEntityIdentifierToReference(rows);

        Set<ColumnParser> columnParsers = SetUtilities.map(assessmentHeaders, ColumnParser::mkColumnParser);

        return rows
                .stream()
                .map(row -> {

                    ResolvedLegalEntityRelationship relationship = resolveLegalEntityRelationship(
                            row,
                            relationshipKind.targetKind(),
                            targetIdentifierToIdMap,
                            legalEntityIdentifierToIdMap,
                            existingRelToIdMap);

                    Collection<Tuple2<Long, Long>> existingRatingsForRel = relationship.existingRelationshipReference()
                            .map(ref -> relationshipToExistingRatingsMap.getOrDefault(ref, emptySet()))
                            .orElse(emptySet());

                    Set<AssessmentCell> assessments = resolveAssessments(row, columnParsers, existingRatingsForRel);

                    return ImmutableResolvedUploadRow.builder()
                            .rowNumber(row.getRowNum())
                            .legalEntityRelationship(relationship)
                            .assessmentRatings(assessments)
                            .build();
                })
                .collect(toSet());
    }


    private Map<String, EntityReference> loadLegalEntityIdentifierToReference(Set<Row> rows) {
        Set<String> legalEntityIdentifiers = getColumnValuesFromRows(rows, LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER);
        return entityAliasPopulator.fetchEntityReferenceLookupMap(EntityKind.LEGAL_ENTITY, legalEntityIdentifiers);
    }

    private Map<String, EntityReference> loadTargetIdentifierToReference(Set<Row> rows, LegalEntityRelationshipKind relationshipKind) {
        Set<String> targetIdentifiers = getColumnValuesFromRows(rows, LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER);
        return entityAliasPopulator.fetchEntityReferenceLookupMap(relationshipKind.targetKind(), targetIdentifiers);
    }

    /**
     * @param relationshipKind legal entity relationship kind
     * @return Relationship Reference to collection of Definition Id, Rating Id
     */
    private Map<EntityReference, Collection<Tuple2<Long, Long>>> loadRelationshipToExistingRatingsMap(LegalEntityRelationshipKind relationshipKind) {

        List<AssessmentRating> existingAssessmentRatings = assessmentRatingService
                .findByEntityKind(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, Optional.of(relationshipKind.entityReference()));

        return groupBy(
                existingAssessmentRatings,
                AssessmentRating::entityReference,
                d -> tuple(d.assessmentDefinitionId(), d.ratingId()));
    }

    private Map<Tuple2<EntityReference, EntityReference>, EntityReference> loadExistingTargetIdAndLegalEntityIdToRelIdMap(DSLContext tx, Long relationshipKindId) {
        Set<LegalEntityRelationship> existingRelationships = legalEntityRelationshipService.findByRelationshipKindId(tx, relationshipKindId);

        return indexBy(existingRelationships,
                r -> tuple(r.targetEntityReference(), r.legalEntityReference()),
                LegalEntityRelationship::entityReference);
    }

    private Set<AssessmentCell> assignDisallowedMultipleRatingsError(Set<ColumnParser> columnParsers,
                                                                     Set<AssessmentCell> assessments) {

        Map<Integer, AssessmentHeaderCell> headersByColId = columnParsers
                .stream()
                .map(ColumnParser::getHeader)
                .collect(toMap(AssessmentHeaderCell::columnId, v -> v));

        Map<Optional<AssessmentDefinition>, Set<RatingSchemeItem>> ratingsByDefinition = getResolvedRatingsByDefinition(assessments, headersByColId);

        return assessments
                .stream()
                .map(assessmentCell -> {

                    AssessmentHeaderCell header = headersByColId.get(assessmentCell.columnId());

                    if (!header.isSingleValued() || !header.resolvedAssessmentDefinition().isPresent() || assessmentCell.ratings().isEmpty() || isEmpty(assessmentCell.inputString())) {
                        return assessmentCell;
                    }

                    Set<RatingSchemeItem> ratings = ratingsByDefinition.getOrDefault(header.resolvedAssessmentDefinition(), emptySet());

                    if (ratings.size() > 1) {
                        return mkAssessmentWithMultipleRatingsDisallowedError(assessmentCell);
                    } else {
                        return assessmentCell;
                    }
                }).collect(toSet());
    }

    private AssessmentCell mkAssessmentWithMultipleRatingsDisallowedError(AssessmentCell assessmentCell) {

        RatingResolutionError multipleRatingsDisallowedError = RatingResolutionError.mkError(
                RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED,
                "There have been multiple ratings specified for a single value assessment definition");

        Set<AssessmentCellRating> ratingsWithDisallowedError = map(
                assessmentCell.ratings(),
                r -> ImmutableAssessmentCellRating
                        .copyOf(r)
                        .withStatus(ResolutionStatus.ERROR)
                        .withErrors(union(r.errors(), asSet(multipleRatingsDisallowedError))));

        return ImmutableAssessmentCell
                .copyOf(assessmentCell)
                .withRatings(ratingsWithDisallowedError);
    }

    private Map<Optional<AssessmentDefinition>, Set<RatingSchemeItem>> getResolvedRatingsByDefinition(Set<AssessmentCell> assessments,
                                                                                                      Map<Integer, AssessmentHeaderCell> headersByColId) {
        return assessments
                .stream()
                .flatMap(r -> {

                    AssessmentHeaderCell header = headersByColId.get(r.columnId());

                    return r.ratings()
                            .stream()
                            .filter(d -> d.resolvedRating().isPresent())
                            .map(d -> tuple(header.resolvedAssessmentDefinition(), d.resolvedRating().get()));
                })
                .collect(groupingBy(t -> t.v1, Collectors.mapping(t -> t.v2, Collectors.toSet())));
    }


    private Set<AssessmentCell> resolveAssessments(Row row,
                                                   Set<ColumnParser> columnParsers,
                                                   Collection<Tuple2<Long, Long>> existingRatingsForRel) {

        Set<AssessmentCell> assessmentRatingsForRow = columnParsers
                .stream()
                .map(parser -> {
                    String cellValue = row.getValue(parser.getHeader().inputString());
                    return parser.apply(mkSafe(cellValue), existingRatingsForRel);
                })
                .filter(Objects::nonNull)
                .collect(toSet());

        return assignDisallowedMultipleRatingsError(columnParsers, assessmentRatingsForRow);
    }

    private ResolvedLegalEntityRelationship resolveLegalEntityRelationship(Row row,
                                                                           EntityKind targetKind,
                                                                           Map<String, EntityReference> targetIdentifierToIdMap,
                                                                           Map<String, EntityReference> legalEntityIdentifierToIdMap,
                                                                           Map<Tuple2<EntityReference, EntityReference>, EntityReference> existingRelToIdMap) {

        String legalEntityIdentifier = row.getValue(LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER);
        String entityIdentifier = row.getValue(LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER);
        String comment = row.getValue(LegalEntityBulkUploadFixedColumns.COMMENT);
        String removal = row.getValue(LegalEntityBulkUploadFixedColumns.REMOVE_RELATIONSHIP);

        ResolvedReference legalEntityReference = mkResolvedReference(legalEntityIdentifier, legalEntityIdentifierToIdMap);
        ResolvedReference targetEntityReference = mkResolvedReference(entityIdentifier, targetIdentifierToIdMap);

        Optional<EntityReference> existingRelationship = ofNullable(existingRelToIdMap.get(mkRelKeyFromResolvedReferences(targetEntityReference, legalEntityReference)));

        Set<LegalEntityRelationshipResolutionError> errors = reportRelationshipErrors(
                targetKind,
                legalEntityReference,
                targetEntityReference,
                existingRelationship,
                removal);

        UploadOperation action = determineAction(
                existingRelationship.isPresent(),
                columnIsMarked(removal));

        return ImmutableResolvedLegalEntityRelationship
                .builder()
                .legalEntityReference(legalEntityReference)
                .targetEntityReference(targetEntityReference)
                .comment(ofNullable(comment))
                .errors(errors)
                .operation(action)
                .existingRelationshipReference(existingRelationship)
                .build();
    }

    private boolean columnIsMarked(String cellValue) {
        return notEmpty(cellValue) && (cellValue.equalsIgnoreCase("Y") || cellValue.equalsIgnoreCase("X"));
    }

    private Set<LegalEntityRelationshipResolutionError> reportRelationshipErrors(EntityKind targetKind,
                                                                                 ResolvedReference legalEntityRef,
                                                                                 ResolvedReference targetEntityRef,
                                                                                 Optional<EntityReference> existingRelationship,
                                                                                 String removal) {
        Set<LegalEntityRelationshipResolutionError> errors = new HashSet<>();

        if (!legalEntityRef.resolvedEntityReference().isPresent()) {
            errors.add(mkError(LegalEntityResolutionErrorCode.LEGAL_ENTITY_NOT_FOUND, format("Legal entity '%s' cannot be identified", legalEntityRef.inputString())));
        }

        if (!targetEntityRef.resolvedEntityReference().isPresent()) {
            errors.add(mkError(LegalEntityResolutionErrorCode.TARGET_ENTITY_NOT_FOUND, format("%s '%s' cannot be identified", targetKind.prettyName(), targetEntityRef.inputString())));
        }

        if (!existingRelationship.isPresent() && columnIsMarked(removal)) {
            errors.add(mkError(LegalEntityResolutionErrorCode.REMOVAL_NOT_VALID,
                    format("Existing relationship between: %s:'%s' and legal entity: '%s' cannot be identified, therefore cannot be removed",
                            targetKind.prettyName(),
                            targetEntityRef.inputString(),
                            legalEntityRef.inputString())));
        }

        return errors;
    }

    public static <T extends BulkUploadError> UploadOperation determineAction(boolean alreadyExists,
                                                                              boolean isRemoval) {
        if (isRemoval) {
            return UploadOperation.REMOVE;
        } else if (alreadyExists) {
            return UploadOperation.UPDATE;
        } else {
            return UploadOperation.ADD;
        }
    }

    private static Tuple2<EntityReference, EntityReference> mkRelKeyFromResolvedReferences(ResolvedReference targetEntityReference, ResolvedReference legalEntityReference) {
        return tuple(
                targetEntityReference.resolvedEntityReference().orElse(null),
                legalEntityReference.resolvedEntityReference().orElse(null));
    }

    public Set<AssessmentHeaderCell> parseAssessmentsFromHeader(EntityReference relKind, Set<String> headers) {

        if (headers.size() <= FIXED_COL_HEADERS.size()) {
            return emptySet();
        }

        Set<AssessmentDefinition> definitions = assessmentDefinitionService.findByEntityKindAndQualifier(
                EntityKind.LEGAL_ENTITY_RELATIONSHIP,
                relKind);

        Map<String, AssessmentDefinition> definitionsByExternalId = definitions
                .stream()
                .filter(d -> d.externalId().isPresent())
                .collect(toMap(d -> sanitize(d.externalId().get()), d -> d));

        Map<String, AssessmentDefinition> definitionsByName = indexBy(definitions, d -> sanitize(d.name()), d -> d);

        Map<Long, Set<RatingSchemeItem>> ratingSchemeItemsBySchemeId = groupAndThen(
                ratingSchemeService.getAllRatingSchemeItems(),
                RatingSchemeItem::ratingSchemeId,
                SetUtilities::fromCollection);

        AtomicInteger colIdx = new AtomicInteger(0);

        return headers
                .stream()
                .filter(h -> !RELATIONSHIP_COL_HEADERS.contains(h))
                .map(headerString -> mkAssessmentHeaderCell(headerString, colIdx.getAndIncrement(), definitionsByExternalId, definitionsByName, ratingSchemeItemsBySchemeId))
                .collect(toSet());
    }

    private AssessmentHeaderCell mkAssessmentHeaderCell(String headerString,
                                                        int colIdx,
                                                        Map<String, AssessmentDefinition> definitionsByExternalId,
                                                        Map<String, AssessmentDefinition> definitionsByName,
                                                        Map<Long, Set<RatingSchemeItem>> ratingSchemeItemsBySchemeId) {

        String[] assessmentHeader = headerString.split("/");
        String definitionString = safeTrim(idx(assessmentHeader, 0, null));

        if (isEmpty(definitionString)) {
            return mkHeader(headerString, Optional.empty(), Optional.empty(), ResolvedAssessmentHeaderStatus.HEADER_DEFINITION_NOT_FOUND, emptySet(), colIdx);
        }

        Optional<AssessmentDefinition> definition = determineDefinition(definitionsByExternalId, definitionsByName, definitionString);

        if (OptionalUtilities.isEmpty(definition)) {
            return mkHeader(headerString, Optional.empty(), Optional.empty(), ResolvedAssessmentHeaderStatus.HEADER_DEFINITION_NOT_FOUND, emptySet(), colIdx);
        }

        Set<RatingSchemeItem> schemeItems = definition
                .map(defn -> ratingSchemeItemsBySchemeId.getOrDefault(defn.ratingSchemeId(), emptySet()))
                .orElse(emptySet());

        String ratingString = safeTrim(idx(assessmentHeader, 1, null));

        if (isEmpty(ratingString)) {
            return mkHeader(headerString, definition, Optional.empty(), ResolvedAssessmentHeaderStatus.HEADER_FOUND, schemeItems, colIdx);
        }

        Optional<RatingSchemeItem> rating = determineRating(schemeItems, ratingString);

        if (OptionalUtilities.isEmpty(rating)) {
            return mkHeader(headerString, definition, Optional.empty(), ResolvedAssessmentHeaderStatus.HEADER_RATING_NOT_FOUND, schemeItems, colIdx);
        }

        return mkHeader(headerString, definition, rating, ResolvedAssessmentHeaderStatus.HEADER_FOUND, schemeItems, colIdx);
    }

    private Optional<RatingSchemeItem> determineRating(Set<RatingSchemeItem> schemeItems, String ratingString) {

        Map<String, RatingSchemeItem> ratingSchemeItemsByName = indexBy(schemeItems, d -> sanitize(d.name()));
        Map<String, RatingSchemeItem> ratingSchemeItemsByCode = indexBy(schemeItems, d -> sanitize(d.rating()));
        Map<String, RatingSchemeItem> ratingSchemeItemsByExternalId = schemeItems
                .stream()
                .filter(d -> d.externalId().isPresent())
                .collect(toMap(k -> sanitize(k.externalId().get()), v -> v));

        Map<String, RatingSchemeItem> ratingSchemeItemsByLookupString = merge(
                ratingSchemeItemsByName,
                ratingSchemeItemsByCode,
                ratingSchemeItemsByExternalId);

        String lookupString = sanitize(ratingString);

        return ofNullable(ratingSchemeItemsByLookupString.get(lookupString));
    }

    private Optional<AssessmentDefinition> determineDefinition(Map<String, AssessmentDefinition> definitionsByExternalId, Map<String, AssessmentDefinition> definitionsByName, String definitionString) {
        String lookupString = sanitize(definitionString);
        return ofNullable(
                definitionsByExternalId
                        .getOrDefault(
                                lookupString,
                                definitionsByName.get(lookupString)));
    }
}
