package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.OptionalUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.EntityAliasPopulator;
import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.ArrayUtilities.idx;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.common.StringUtilities.safeTrim;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentHeaderResolutionError.mkError;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentResolutionErrorCode.*;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.LegalEntityRelationshipResolutionError.mkError;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.ResolvedAssessmentHeader.mkHeader;
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

    public ResolveBulkUploadLegalEntityRelationshipParameters resolve(BulkUploadLegalEntityRelationshipCommand uploadCommand) {

        LegalEntityRelationshipKind relKind = legalEntityRelationshipKindService.getById(uploadCommand.legalEntityRelationshipKindId());


        Set<String> headers = streamData(uploadCommand.inputString())
                .findFirst()
                .map(Row::getHeaders)
                .orElseThrow(() -> new IllegalStateException("No data provided"));

        if (!headers.containsAll(FIXED_COL_HEADERS)) {
            throw new IllegalStateException(format("Not all mandatory columns (%s) provided", StringUtilities.join(FIXED_COL_HEADERS, ", ")));
        }

        Set<ResolvedAssessmentHeader> resolvedHeaders = parseAssessmentsFromHeader(
                mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, uploadCommand.legalEntityRelationshipKindId()),
                headers);

        Set<ResolvedUploadRow> resolvedRows = parseRowData(
                streamData(uploadCommand.inputString()).collect(toSet()),
                relKind,
                resolvedHeaders);

        return ImmutableResolveBulkUploadLegalEntityRelationshipParameters.builder()
                .uploadMode(uploadCommand.uploadMode())
                .inputString(uploadCommand.inputString())
                .resolvedRows(resolvedRows)
                .build();
    }

    private Set<ResolvedUploadRow> parseRowData(Set<Row> rows,
                                                LegalEntityRelationshipKind relationshipKind,
                                                Set<ResolvedAssessmentHeader> assessmentHeaders) {

        Set<LegalEntityRelationship> existingRelationships = legalEntityRelationshipService.findByRelationshipKind(relationshipKind.id().get());

        Map<Tuple2<EntityReference, EntityReference>, EntityReference> existingRelToIdMap = indexBy(existingRelationships,
                r -> tuple(r.targetEntityReference(), r.legalEntityReference()),
                LegalEntityRelationship::entityReference);

        List<AssessmentRating> existingAssessmentRatings = assessmentRatingService.findByEntityKind(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, Optional.of(relationshipKind.entityReference()));

        Map<Long, Collection<Tuple2<Long, Long>>> ratingsByRelationship = groupBy(existingAssessmentRatings,
                d -> d.entityReference().id(),
                d -> tuple(d.assessmentDefinitionId(), d.ratingId()));

        Set<String> targetIdentifiers = getColumnValuesFromRows(rows, LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER);
        Set<String> legalEntityIdentifiers = getColumnValuesFromRows(rows, LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER);

        Map<String, EntityReference> targetIdentifierToIdMap = entityAliasPopulator.fetchEntityReferenceLookupMap(relationshipKind.targetKind(), targetIdentifiers);
        Map<String, EntityReference> legalEntityIdentifierToIdMap = entityAliasPopulator.fetchEntityReferenceLookupMap(EntityKind.LEGAL_ENTITY, legalEntityIdentifiers);

        return rows
                .stream()
                .map(row -> {

                    ResolvedLegalEntityRelationship relationship = resolveLegalEntityRelationship(
                            relationshipKind.targetKind(),
                            targetIdentifierToIdMap,
                            legalEntityIdentifierToIdMap,
                            row,
                            existingRelToIdMap);

                    Collection<Tuple2<Long, Long>> existingRatingInfo = relationship.relationshipId()
                            .map(ratingsByRelationship::get)
                            .orElse(emptySet());

                    Set<ResolvedAssessmentRating> assessments = resolveAssessments(assessmentHeaders, existingRatingInfo, row);

                    return ImmutableResolvedUploadRow.builder()
                            .rowNumber(row.getRowNum())
                            .legalEntityRelationship(relationship)
                            .assessmentRatings(assessments)
                            .build();
                })
                .collect(toSet());
    }

    // For specific DEF / RATING cols, need to check over multiple cols for single value definitions
    private Set<ResolvedAssessmentRating> assignDisallowedMultipleRatingsError(Set<ResolvedAssessmentRating> assessments) {

        Map<AssessmentDefinition, List<ResolvedAssessmentRating>> ratingsByDefinitionForRow = assessments
                .stream()
                .filter(d -> d.assessmentHeader().headerDefinition().map(def -> def.cardinality().equals(Cardinality.ZERO_ONE)).orElse(false))
                .collect(groupingBy(d -> d.assessmentHeader().headerDefinition().orElse(null)));

        Set<AssessmentDefinition> definitionsWithMultipleRatingsDisallowed = ratingsByDefinitionForRow
                .entrySet()
                .stream()
                .filter(e -> getDistinctRatings(e.getValue()).size() > 1)
                .map(Map.Entry::getKey)
                .collect(toSet());

        RatingResolutionError multipleRatingsDisallowedError = RatingResolutionError.mkError(RatingResolutionErrorCode.MULTIPLE_RATINGS_DISALLOWED, "There have been multiple ratings specified for a single value assessment definition");

        return map(
                assessments,
                d -> d
                        .assessmentHeader()
                        .headerDefinition()
                        .map(defn -> {
                            if (definitionsWithMultipleRatingsDisallowed.contains(defn)) {

                                Set<ResolvedRatingValue> ratingsWithDisallowedError = map(
                                        d.resolvedRatings(),
                                        r -> ImmutableResolvedRatingValue
                                                .copyOf(r)
                                                .withErrors(union(r.errors(), asSet(multipleRatingsDisallowedError))));

                                return ImmutableResolvedAssessmentRating
                                        .copyOf(d)
                                        .withResolvedRatings(ratingsWithDisallowedError);
                            } else {
                                return d;
                            }
                        })
                        .orElse(d));
    }

    private Set<RatingSchemeItem> getDistinctRatings(List<ResolvedAssessmentRating> ratings) {
        return ratings
                .stream()
                .flatMap(r -> r.resolvedRatings().stream())
                .map(ResolvedRatingValue::rating)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    private Set<ResolvedAssessmentRating> resolveAssessments(Set<ResolvedAssessmentHeader> resolvedAssessmentHeaders,
                                                             Collection<Tuple2<Long, Long>> existingRatingInfo,
                                                             Row row) {

        Set<ResolvedAssessmentRating> assessmentRatingsForRow = resolvedAssessmentHeaders
                .stream()
                .map(header -> {
                    String ratingValue = row.getValue(header.inputString());

                    if (header.headerRating().isPresent() && !isEmpty(ratingValue)) {
                        return mkResolvedRatingForRatingColumn(header, ratingValue, existingRatingInfo);
                    } else {
                        return mkResolvedRatingForAssessmentDefinitionColumn(header, ratingValue, existingRatingInfo);
                    }
                })
                .collect(toSet());

        return assignDisallowedMultipleRatingsError(assessmentRatingsForRow);
    }

    private ResolvedAssessmentRating mkResolvedRatingForAssessmentDefinitionColumn(ResolvedAssessmentHeader header,
                                                                                   String ratingValue,
                                                                                   Collection<Tuple2<Long, Long>> existingRatingInfo) {

        Map<Long, Collection<RatingSchemeItem>> ratingSchemeItemsBySchemeId = groupBy(ratingSchemeService.getAllRatingSchemeItems(), RatingSchemeItem::ratingSchemeId);

        Set<RatingResolutionError> errors = new HashSet<>();

        Long defnId = header.headerDefinition().flatMap(IdProvider::id).orElse(null);
        String[] ratingValues = ratingValue.split(";");

        Set<ResolvedRatingValue> ratings = Stream.of(ratingValues)
                .map(ratingString -> {

                    Optional<RatingSchemeItem> ratingSchemeItem = determineRating(ratingSchemeItemsBySchemeId, safeTrim(ratingString), header.headerDefinition());

                    if (OptionalUtilities.isEmpty(ratingSchemeItem)) {
                        errors.add(RatingResolutionError.mkError(RatingResolutionErrorCode.RATING_VALUE_NOT_FOUND, format("Could not identify rating value from: '%s'", ratingString)));
                    }

                    boolean ratingExists = existingRatingInfo.contains(tuple(defnId, ratingSchemeItem.flatMap(IdProvider::id)));
                    ResolutionStatus status = determineResolutionStatus(ratingExists, errors);

                    return ImmutableResolvedRatingValue.builder()
                            .rating(ratingSchemeItem)
                            .errors(errors)
                            .status(status)
                            .build();
                })
                .collect(toSet());

        return ImmutableResolvedAssessmentRating.builder()
                .assessmentHeader(header)
                .resolvedRatings(ratings)
                .build();
    }

    private ResolvedAssessmentRating mkResolvedRatingForRatingColumn(ResolvedAssessmentHeader header,
                                                                     String ratingValue,
                                                                     Collection<Tuple2<Long, Long>> existingRatingInfo) {

        String comment = ratingValue.equalsIgnoreCase("X") || ratingValue.equalsIgnoreCase("Y")
                ? null
                : ratingValue;

        Long defnId = header.headerDefinition().flatMap(IdProvider::id).orElse(null);
        Long ratingId = header.headerRating().flatMap(IdProvider::id).orElse(null);

        boolean existingRating = existingRatingInfo.contains(tuple(defnId, ratingId));

        ResolutionStatus status = determineResolutionStatus(existingRating, emptySet());

        ImmutableResolvedRatingValue resolvedRatingValue = ImmutableResolvedRatingValue.builder()
                .rating(header.headerRating().get())
                .comment(Optional.ofNullable(comment))
                .errors(emptySet())
                .status(status)
                .build();

        return ImmutableResolvedAssessmentRating.builder()
                .assessmentHeader(header)
                .resolvedRatings(asSet(resolvedRatingValue))
                .build();
    }

    private ResolvedLegalEntityRelationship resolveLegalEntityRelationship(EntityKind targetKind,
                                                                           Map<String, EntityReference> targetIdentifierToIdMap,
                                                                           Map<String, EntityReference> legalEntityIdentifierToIdMap,
                                                                           Row row,
                                                                           Map<Tuple2<EntityReference, EntityReference>, EntityReference> existingRelToIdMap) {

        String legalEntityIdentifier = row.getValue(LegalEntityBulkUploadFixedColumns.LEGAL_ENTITY_IDENTIFIER);
        String entityIdentifier = row.getValue(LegalEntityBulkUploadFixedColumns.ENTITY_IDENTIFIER);
        String comment = row.getValue(LegalEntityBulkUploadFixedColumns.COMMENT);

        EntityReference legalEntityRef = legalEntityIdentifierToIdMap.get(legalEntityIdentifier);
        EntityReference targetEntityRef = targetIdentifierToIdMap.get(entityIdentifier);

        Set<LegalEntityRelationshipResolutionError> errors = new HashSet<>();

        if (legalEntityRef == null) {
            errors.add(mkError(LegalEntityResolutionErrorCode.LEGAL_ENTITY_NOT_FOUND, format("Legal entity '%s' cannot be identified", legalEntityIdentifier)));
        }

        if (targetEntityRef == null) {
            errors.add(mkError(LegalEntityResolutionErrorCode.TARGET_ENTITY_NOT_FOUND, format("%s '%s' cannot be identified", targetKind.prettyName(), legalEntityIdentifier)));
        }

        EntityReference existingRel = existingRelToIdMap.get(tuple(targetEntityRef, legalEntityRef));
        Optional<Long> relationshipId = ofNullable(existingRel).map(EntityReference::id);

        ResolutionStatus status = determineResolutionStatus(relationshipId.isPresent(), errors);

        return ImmutableResolvedLegalEntityRelationship
                .builder()
                .legalEntityReference(ofNullable(legalEntityRef))
                .targetEntityReference(ofNullable(targetEntityRef))
                .comment(Optional.ofNullable(comment))
                .errors(errors)
                .status(status)
                .relationshipId(relationshipId)
                .build();
    }

    public static <T extends BulkUploadError> ResolutionStatus determineResolutionStatus(boolean exists,
                                                                                         Set<T> errors) {
        if (!CollectionUtilities.isEmpty(errors)) {
            return ResolutionStatus.ERROR;
        } else if (exists) {
            return ResolutionStatus.EXISTING;
        } else {
            return ResolutionStatus.NEW;
        }
    }

    public Set<ResolvedAssessmentHeader> parseAssessmentsFromHeader(EntityReference relKind, Set<String> headers) {

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

        Map<Long, Collection<RatingSchemeItem>> ratingSchemeItemsBySchemeId = groupBy(
                ratingSchemeService.getAllRatingSchemeItems(),
                RatingSchemeItem::ratingSchemeId);

        return headers
                .stream()
                .filter(h -> !RELATIONSHIP_COL_HEADERS.contains(h))
                .map(headerString -> determineAssessmentByOffset(headerString, definitionsByExternalId, definitionsByName, ratingSchemeItemsBySchemeId))
                .collect(toSet());
    }

    private ResolvedAssessmentHeader determineAssessmentByOffset(String headerString,
                                                                 Map<String, AssessmentDefinition> definitionsByExternalId,
                                                                 Map<String, AssessmentDefinition> definitionsByName,
                                                                 Map<Long, Collection<RatingSchemeItem>> ratingSchemeItemsBySchemeId) {

        Set<AssessmentHeaderResolutionError> errors = new HashSet<>();

        if (isEmpty(headerString)) {
            errors.add(mkError(NO_VALUE_PROVIDED, "Header assessment is not provided"));
            return mkHeader(headerString, Optional.empty(), Optional.empty(), errors, ResolutionStatus.ERROR);
        }

        String[] assessmentHeader = headerString.split("/");

        String definitionString = safeTrim(idx(assessmentHeader, 0, null));

        if (isEmpty(definitionString)) {
            errors.add(mkError(HEADER_DEFINITION_NOT_FOUND, "No assessment definition header provided"));
            return mkHeader(headerString, Optional.empty(), Optional.empty(), errors, ResolutionStatus.ERROR);
        }

        Optional<AssessmentDefinition> definition = determineDefinition(definitionsByExternalId, definitionsByName, definitionString);

        if (OptionalUtilities.isEmpty(definition)) {
            errors.add(mkError(HEADER_DEFINITION_NOT_FOUND, format("Could not identify an assessment definition with external id or name '%s'", definitionString)));
            return mkHeader(headerString, Optional.empty(), Optional.empty(), errors, ResolutionStatus.ERROR);
        }

        String ratingString = safeTrim(idx(assessmentHeader, 1, null));

        if (isEmpty(ratingString)) {
            return mkHeader(headerString, definition, Optional.empty(), errors, ResolutionStatus.NEW); //Does resolution status besides error matter for headers?
        }

        Optional<RatingSchemeItem> rating = determineRating(ratingSchemeItemsBySchemeId, ratingString, definition);

        if (OptionalUtilities.isEmpty(rating)) {
            errors.add(mkError(HEADER_RATING_NOT_FOUND, format("Could not identify an assessment rating with external id or name '%s'", ratingString)));
            return mkHeader(headerString, definition, Optional.empty(), errors, ResolutionStatus.ERROR);
        }

        return mkHeader(headerString, definition, rating, errors, ResolutionStatus.NEW);
    }

    private Optional<RatingSchemeItem> determineRating(Map<Long, Collection<RatingSchemeItem>> ratingSchemeItemsBySchemeId,
                                                       String ratingString,
                                                       Optional<AssessmentDefinition> definition) {

        Collection<RatingSchemeItem> schemeItems = definition
                .map(defn -> ratingSchemeItemsBySchemeId.getOrDefault(defn.ratingSchemeId(), emptySet()))
                .orElse(emptySet());

        Map<String, RatingSchemeItem> ratingSchemeItemsByExternalId = schemeItems
                .stream()
                .filter(d -> d.externalId().isPresent())
                .collect(Collectors.toMap(d -> d.externalId().get().toLowerCase(), d -> d));

        Map<String, RatingSchemeItem> ratingSchemeItemsByName = indexBy(schemeItems, d -> d.name().toLowerCase());

        return Optional.ofNullable(ratingSchemeItemsByExternalId
                .getOrDefault(
                        ratingString.toLowerCase(),
                        ratingSchemeItemsByName.get(ratingString.toLowerCase())));
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
