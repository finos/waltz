package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.*;
import org.finos.waltz.data.EntityAliasPopulator;
import org.finos.waltz.model.*;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.ResolvedAssessmentHeaderStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.*;
import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.bulk_upload.TabularDataUtilities.Row;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipKindService;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.ArrayUtilities.idx;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.*;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.common.StringUtilities.*;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentHeaderCell.mkHeader;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.LegalEntityRelationshipResolutionError.mkError;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.ResolvedReference.mkResolvedReference;
import static org.finos.waltz.service.bulk_upload.BulkUploadUtilities.getColumnValuesFromRows;
import static org.finos.waltz.service.bulk_upload.TabularDataUtilities.streamData;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class BulkUploadLegalEntityRelationshipService {

    private static final Set<String> FIXED_COL_HEADERS = asSet(LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER, LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER);
    private static final Set<String> RELATIONSHIP_COL_HEADERS = asSet(LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER, LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER, LegalEntityBulkUploadFixedColumns.COMMENT);
    private final AssessmentDefinitionService assessmentDefinitionService;
    private final RatingSchemeService ratingSchemeService;
    private final EntityAliasPopulator entityAliasPopulator;
    private final LegalEntityRelationshipKindService legalEntityRelationshipKindService;
    private final LegalEntityRelationshipService legalEntityRelationshipService;

    private final AssessmentRatingService assessmentRatingService;


    @Autowired
    public BulkUploadLegalEntityRelationshipService(AssessmentDefinitionService assessmentDefinitionService,
                                                    RatingSchemeService ratingSchemeService,
                                                    EntityAliasPopulator entityAliasPopulator,
                                                    LegalEntityRelationshipKindService legalEntityRelationshipKindService,
                                                    LegalEntityRelationshipService legalEntityRelationshipService,
                                                    AssessmentRatingService assessmentRatingService) {

        checkNotNull(assessmentDefinitionService, "assessmentDefinitionService cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
        checkNotNull(entityAliasPopulator, "entityAliasPopulator cannot be null");
        checkNotNull(legalEntityRelationshipKindService, "legalEntityRelationshipKindService cannot be null");
        checkNotNull(legalEntityRelationshipService, "legalEntityRelationshipService cannot be null");
        checkNotNull(assessmentRatingService, "assessmentRatingService cannot be null");

        this.assessmentDefinitionService = assessmentDefinitionService;
        this.ratingSchemeService = ratingSchemeService;
        this.entityAliasPopulator = entityAliasPopulator;
        this.legalEntityRelationshipKindService = legalEntityRelationshipKindService;
        this.legalEntityRelationshipService = legalEntityRelationshipService;
        this.assessmentRatingService = assessmentRatingService;
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

        Set<AssessmentHeaderCell> resolvedHeaders = parseAssessmentsFromHeader(
                mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, uploadCommand.legalEntityRelationshipKindId()),
                headers);

        Set<ResolvedUploadRow> resolvedRows = parseRowData(
                streamData(uploadCommand.inputString()).collect(toSet()),
                relKind,
                resolvedHeaders);

        return ImmutableResolveBulkUploadLegalEntityRelationshipResponse.builder()
                .uploadMode(uploadCommand.uploadMode())
                .rows(resolvedRows)
                .assessmentHeaders(resolvedHeaders)
                .build();
    }

    private Set<ResolvedUploadRow> parseRowData(Set<Row> rows,
                                                LegalEntityRelationshipKind relationshipKind,
                                                Set<AssessmentHeaderCell> assessmentHeaders) {

        Map<Tuple2<EntityReference, EntityReference>, EntityReference> existingRelToIdMap = loadExisitingRelsToIdMap(relationshipKind);

        //Rel ref -> [DefId, ratingId]
        Map<EntityReference, Collection<Tuple2<Long, Long>>> relationshipToExistingRatingsMap = loadRelationshipToExistingRatingsMap(relationshipKind);

        Map<String, EntityReference> targetIdentifierToIdMap = loadTargetIdentifierToReference(rows, relationshipKind);
        Map<String, EntityReference> legalEntityIdentifierToIdMap = loadLegalEntityIdentifierToReference(rows);

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

                    Set<AssessmentCell> assessments = resolveAssessments(row, assessmentHeaders, existingRatingsForRel);

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

    private Map<Tuple2<EntityReference, EntityReference>, EntityReference> loadExisitingRelsToIdMap(LegalEntityRelationshipKind relationshipKind) {
        Set<LegalEntityRelationship> existingRelationships = legalEntityRelationshipService.findByRelationshipKind(relationshipKind.id().get());

        Map<Tuple2<EntityReference, EntityReference>, EntityReference> existingRelToIdMap = indexBy(existingRelationships,
                r -> tuple(r.targetEntityReference(), r.legalEntityReference()),
                LegalEntityRelationship::entityReference);
        return existingRelToIdMap;
    }

    // For specific DEF / RATING cols, need to check over multiple cols for single value definitions
    private Set<AssessmentCell> assignDisallowedMultipleRatingsError(Set<AssessmentHeaderCell> resolvedAssessmentHeaders,
                                                                     Set<AssessmentCell> assessments) {

        Set<Long> singleValuedDefIds = resolvedAssessmentHeaders.stream()
                .filter(h -> h.resolvedAssessmentDefinition().map(d -> d.cardinality().equals(Cardinality.ZERO_ONE)).orElse(false))
                .map(h -> h.resolvedAssessmentDefinition().get().id().get())
                .collect(toSet());

        Map<Integer, Long> definitionIdByColIdx = indexBy(resolvedAssessmentHeaders,
                AssessmentHeaderCell::columnId,
                d -> d.resolvedAssessmentDefinition().map(def -> def.id().get()).orElse(null));

        Map<Long, Set<AssessmentCellRating>> assessmentCellsByDefId = assessments
                .stream()
                .flatMap(r -> r.ratings().stream().map(d -> tuple(d, definitionIdByColIdx.get(r.columnId()))))
                .filter(t -> t.v1.resolvedRating().isPresent() && singleValuedDefIds.contains(t.v2))
                .collect(groupingBy(d -> d.v2, Collectors.mapping(d -> d.v1, toSet())));

        Set<AssessmentCellRating> assessmentValuesWithDisallowedMultipleRatings = assessmentCellsByDefId
                .entrySet()
                .stream()
                .filter(kv -> kv.getValue().size() > 1)
                .flatMap(kv -> kv.getValue().stream())
                .collect(toSet());

        RatingResolutionError multipleRatingsDisallowedError = RatingResolutionError.mkError(RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED, "There have been multiple ratings specified for a single value assessment definition");

        return map(
                assessments,
                d -> d);
//        ?{
//                    return d;
//                    d.ratings()
//                            .contains()assessmentValuesWithDisallowedMultipleRatings.contains()
//
//                    d
//                            .ratings()
//                            .assessmentHeader()
//                            .headerDefinition()
//                            .map(defn -> {
//                                if (definitionsWithMultipleRatingsDisallowed.contains(defn)) {
//
//                                    Set<AssessmentCellRating> ratingsWithDisallowedError = map(
//                                            d.resolvedRatings(),
//                                            r -> ImmutableResolvedRatingValue
//                                                    .copyOf(r)
//                                                    .withErrors(union(r.errors(), asSet(multipleRatingsDisallowedError))));
//
//                                    return ImmutableResolvedAssessmentRating
//                                            .copyOf(d)
//                                            .withResolvedRatings(ratingsWithDisallowedError);
//                                } else {
//                                    return d;
//                                }
//                            })
//                            .orElse(d)
//                });
    }

    private Set<RatingSchemeItem> getDistinctRatings(List<AssessmentCell> ratings) {
        return ratings
                .stream()
                .flatMap(r -> r.ratings().stream())
                .map(AssessmentCellRating::resolvedRating)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    private Set<AssessmentCell> resolveAssessments(Row row,
                                                   Set<AssessmentHeaderCell> resolvedAssessmentHeaders,
                                                   Collection<Tuple2<Long, Long>> existingRatingsForRel) {

        Set<AssessmentCell> assessmentRatingsForRow = resolvedAssessmentHeaders
                .stream()
                .map(header -> {
                    String ratingValue = row.getValue(header.inputString());

                    if (header.resolvedRating().isPresent() && !isEmpty(ratingValue)) {
                        return mkResolvedRatingForRatingColumn(header, ratingValue, existingRatingsForRel);
                    } else {
                        return mkResolvedRatingForAssessmentDefinitionColumn(header, ratingValue, existingRatingsForRel);
                    }
                })
                .collect(toSet());

        return assignDisallowedMultipleRatingsError(resolvedAssessmentHeaders, assessmentRatingsForRow);
    }

    private AssessmentCell mkResolvedRatingForAssessmentDefinitionColumn(AssessmentHeaderCell header,
                                                                         String cellValue,
                                                                         Collection<Tuple2<Long, Long>> existingRatingInfo) {


        Set<RatingResolutionError> errors = new HashSet<>();

        Long defnId = header.resolvedAssessmentDefinition().flatMap(IdProvider::id).orElse(null);

        List<AssessmentCellRating> ratings = splitThenMap(
                cellValue,
                ";",
                ratingString -> {

                    Optional<RatingSchemeItem> rating = ofNullable(header.ratingLookupMap().get(ratingString.trim()));

                    if (OptionalUtilities.isEmpty(rating)) {
                        errors.add(RatingResolutionError.mkError(RatingResolutionErrorCode.RATING_VALUE_NOT_FOUND, format("Could not identify rating value from: '%s'", ratingString)));
                    }

                    boolean ratingExists = existingRatingInfo.contains(tuple(defnId, rating.flatMap(IdProvider::id)));
                    ResolutionStatus status = determineResolutionStatus(ratingExists, errors);

                    return ImmutableAssessmentCellRating.builder()
                            .resolvedRating(rating)
                            .errors(errors)
                            .status(status)
                            .build();
                });

        return ImmutableAssessmentCell.builder()
                .inputString(cellValue)
                .columnId(header.columnId())
                .ratings(ratings)
                .build();
    }

    private AssessmentCell mkResolvedRatingForRatingColumn(AssessmentHeaderCell header,
                                                           String ratingValue,
                                                           Collection<Tuple2<Long, Long>> existingRatingInfo) {

        String comment = ratingValue.equalsIgnoreCase("X") || ratingValue.equalsIgnoreCase("Y")
                ? null
                : ratingValue;

        Long defnId = header.resolvedAssessmentDefinition().flatMap(IdProvider::id).orElse(null);
        Long ratingId = header.resolvedRating().flatMap(IdProvider::id).orElse(null);

        boolean existingRating = existingRatingInfo.contains(tuple(defnId, ratingId));

        ResolutionStatus status = determineResolutionStatus(existingRating, emptySet());

        AssessmentCellRating cellRatings = ImmutableAssessmentCellRating.builder()
                .resolvedRating(header.resolvedRating().get())
                .comment(ofNullable(comment))
                .errors(emptySet())
                .status(status)
                .build();

        return ImmutableAssessmentCell.builder()
                .columnId(header.columnId())
                .inputString(ratingValue)
                .ratings(asSet(cellRatings))
                .build();
    }

    private ResolvedLegalEntityRelationship resolveLegalEntityRelationship(Row row,
                                                                           EntityKind targetKind,
                                                                           Map<String, EntityReference> targetIdentifierToIdMap,
                                                                           Map<String, EntityReference> legalEntityIdentifierToIdMap,
                                                                           Map<Tuple2<EntityReference, EntityReference>, EntityReference> existingRelToIdMap) {

        String legalEntityIdentifier = row.getValue(LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER);
        String entityIdentifier = row.getValue(LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER);
        String comment = row.getValue(LegalEntityBulkUploadFixedColumns.COMMENT);

        ResolvedReference legalEntityReference = mkResolvedReference(legalEntityIdentifier, legalEntityIdentifierToIdMap);
        ResolvedReference targetEntityReference = mkResolvedReference(entityIdentifier, targetIdentifierToIdMap);

        Set<LegalEntityRelationshipResolutionError> errors = reportRelationshipErrors(targetKind, legalEntityReference, targetEntityReference);

        Optional<EntityReference> existingRelationship = ofNullable(existingRelToIdMap.get(mkRelKeyFromResolvedReferences(targetEntityReference, legalEntityReference)));

        ResolutionStatus status = determineResolutionStatus(
                existingRelationship.isPresent(),
                errors);

        return ImmutableResolvedLegalEntityRelationship
                .builder()
                .legalEntityReference(legalEntityReference)
                .targetEntityReference(targetEntityReference)
                .comment(ofNullable(comment))
                .errors(errors)
                .status(status)
                .existingRelationshipReference(existingRelationship)
                .build();
    }

    private Set<LegalEntityRelationshipResolutionError> reportRelationshipErrors(EntityKind targetKind,
                                                                                 ResolvedReference legalEntityRef,
                                                                                 ResolvedReference targetEntityRef) {
        Set<LegalEntityRelationshipResolutionError> errors = new HashSet<>();

        if (!legalEntityRef.resolvedEntityReference().isPresent()) {
            errors.add(mkError(LegalEntityResolutionErrorCode.LEGAL_ENTITY_NOT_FOUND, format("Legal entity '%s' cannot be identified", legalEntityRef.inputString())));
        }

        if (!targetEntityRef.resolvedEntityReference().isPresent()) {
            errors.add(mkError(LegalEntityResolutionErrorCode.TARGET_ENTITY_NOT_FOUND, format("%s '%s' cannot be identified", targetKind.prettyName(), targetEntityRef.inputString())));
        }

        return errors;
    }

    public static <T extends BulkUploadError> ResolutionStatus determineResolutionStatus(boolean alreadyExists,
                                                                                         Set<T> errors) {
        if (!CollectionUtilities.isEmpty(errors)) {
            return ResolutionStatus.ERROR;
        } else if (alreadyExists) {
            return ResolutionStatus.EXISTING;
        } else {
            return ResolutionStatus.NEW;
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
                .collect(Collectors.toMap(d -> d.externalId().get().toLowerCase(), d -> d));

        Map<String, AssessmentDefinition> definitionsByName = indexBy(definitions, d -> d.name().toLowerCase(), d -> d);

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
            return mkHeader(headerString, definition, Optional.empty(), ResolvedAssessmentHeaderStatus.HEADER_RATING_NOT_FOUND, schemeItems, colIdx);
        }

        Map<String, RatingSchemeItem> ratingSchemeItemsByName = indexBy(schemeItems, NameProvider::name);
        Map<String, RatingSchemeItem> ratingSchemeItemsByExternalId = schemeItems
                .stream()
                .filter(d -> d.externalId().isPresent())
                .collect(Collectors.toMap(k -> k.externalId().get(), v -> v));

        Map<String, RatingSchemeItem> ratingSchemeItemsByLookupString = merge(ratingSchemeItemsByName, ratingSchemeItemsByExternalId);

        Optional<RatingSchemeItem> rating = ofNullable(ratingSchemeItemsByLookupString.get(ratingString));

        if (OptionalUtilities.isEmpty(rating)) {
            return mkHeader(headerString, definition, Optional.empty(), ResolvedAssessmentHeaderStatus.HEADER_RATING_NOT_FOUND, schemeItems, colIdx);
        }

        return mkHeader(headerString, definition, rating, ResolvedAssessmentHeaderStatus.HEADER_FOUND, schemeItems, colIdx);
    }

    private Optional<AssessmentDefinition> determineDefinition(Map<String, AssessmentDefinition> definitionsByExternalId, Map<String, AssessmentDefinition> definitionsByName, String definitionString) {
        return ofNullable(
                definitionsByExternalId
                        .getOrDefault(
                                definitionString.toLowerCase(),
                                definitionsByName.get(definitionString.toLowerCase())));
    }


    // Pull out to immutable

    public void ensureUserHasAdminRights(long legalEntityRelationshipKindId, String username) {
        LegalEntityRelationshipKind relKind = legalEntityRelationshipKindService.getById(legalEntityRelationshipKindId);
    }
}
