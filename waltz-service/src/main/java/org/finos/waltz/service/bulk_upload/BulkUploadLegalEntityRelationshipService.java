package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.*;
import org.finos.waltz.common.StreamUtilities.Siphon;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.*;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.ArrayUtilities.idx;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.common.StringUtilities.safeTrim;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentHeaderResolutionError.mkError;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentResolutionErrorCode.*;
import static org.finos.waltz.model.bulk_upload.legal_entity_relationship.ResolvedAssessmentHeader.mkHeader;
import static org.finos.waltz.service.bulk_upload.BulkUploadUtilities.streamRowData;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class BulkUploadLegalEntityRelationshipService {

    private static final int LEGAL_ENTITY_REL_COLS_COUNT = 3;
    private final AssessmentDefinitionService assessmentDefinitionService;
    private final RatingSchemeService ratingSchemeService;

    @Autowired
    public BulkUploadLegalEntityRelationshipService(AssessmentDefinitionService assessmentDefinitionService,
                                                    RatingSchemeService ratingSchemeService) {

        checkNotNull(assessmentDefinitionService, "assessmentDefinitionService cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");

        this.assessmentDefinitionService = assessmentDefinitionService;
        this.ratingSchemeService = ratingSchemeService;
    }

    public ResolveBulkUploadLegalEntityRelationshipParameters resolve(BulkUploadLegalEntityRelationshipCommand uploadCommand) {

        Tuple2<Tuple2<Integer, String[]>, Set<Tuple2<Integer, String[]>>> parsedRows = readRows(uploadCommand.inputString());

        Set<Tuple2<Integer, ResolvedAssessmentHeader>> headersByColumnId = parseAssessmentsFromHeader(parsedRows.v1);

        // Parse and split to header row and data rows
        // List<List<String>> data;
        // List<String> headerRow = first(data);

        //Parse assessment headers for index and assessment definitions (and ratings)
        //Set<idx, defn, rating>
        //Set<Integer, Optional<EntityReference>, Optional<Long>> // maybe Assessment Definition and Rating Scheme Item for cardinality and use in resolved ratings

        //Parse data rows
        // parse LE
        // parse app
        // parse comment
        // assessment columns using header info

        return null;
    }

    public Set<Tuple2<Integer, ResolvedAssessmentHeader>> parseAssessmentsFromHeader(Tuple2<Integer, String[]> headerRow) {

        if (headerRow.v2.length <= 3) {
            return emptySet();
        }

        List<AssessmentDefinition> definitions = assessmentDefinitionService.findByEntityKind(EntityKind.LEGAL_ENTITY_RELATIONSHIP);

        Map<String, AssessmentDefinition> definitionsByExternalId = definitions
                .stream()
                .filter(d -> d.externalId().isPresent())
                .collect(Collectors.toMap(d -> d.externalId().get().toLowerCase(), d -> d));

        Map<String, AssessmentDefinition> definitionsByName = indexBy(definitions, d -> d.name().toLowerCase(), d -> d);

        Map<Long, Collection<RatingSchemeItem>> ratingSchemeItemsBySchemeId = groupBy(ratingSchemeService.getAllRatingSchemeItems(), RatingSchemeItem::ratingSchemeId);

        AtomicInteger columnNumber = new AtomicInteger(LEGAL_ENTITY_REL_COLS_COUNT + 1);

        return Stream.of(headerRow.v2)
                .skip(LEGAL_ENTITY_REL_COLS_COUNT)
                .map(headerString -> {

                    Set<AssessmentHeaderResolutionError> errors = new HashSet<>();

                    if (isEmpty(headerString)) {
                        errors.add(mkError(NO_VALUE_PROVIDED, "Header assessment is not provided"));
                        return tuple(columnNumber.getAndIncrement(), mkHeader(Optional.empty(), Optional.empty(), errors, ResolutionStatus.ERROR));
                    }

                    String[] assessmentHeader = headerString.split("/");

                    String definitionString = safeTrim(idx(assessmentHeader, 0, null));

                    if (isEmpty(definitionString)) {
                        errors.add(mkError(HEADER_DEFINITION_NOT_FOUND, "No assessment definition header provided"));
                        return tuple(columnNumber.getAndIncrement(), mkHeader(Optional.empty(), Optional.empty(), errors, ResolutionStatus.ERROR));
                    }

                    Optional<AssessmentDefinition> definition = determineDefinition(definitionsByExternalId, definitionsByName, definitionString);

                    if (OptionalUtilities.isEmpty(definition)) {
                        errors.add(mkError(HEADER_DEFINITION_NOT_FOUND, format("Could not identify an assessment definition with external id or name '%s'", definitionString)));
                        return tuple(columnNumber.getAndIncrement(), mkHeader(Optional.empty(), Optional.empty(), errors, ResolutionStatus.ERROR));
                    }

                    String ratingString = safeTrim(idx(assessmentHeader, 1, null));

                    if (isEmpty(ratingString)) {
                        return tuple(columnNumber.getAndIncrement(), mkHeader(definition, Optional.empty(), errors, ResolutionStatus.NEW));
                    }

                    Optional<RatingSchemeItem> rating = determineRating(ratingSchemeItemsBySchemeId, ratingString, definition);

                    if (OptionalUtilities.isEmpty(rating)) {
                        errors.add(mkError(HEADER_RATING_NOT_FOUND, format("Could not identify an assessment rating with external id or name '%s'", ratingString)));
                        return tuple(columnNumber.getAndIncrement(), mkHeader(definition, Optional.empty(), errors, ResolutionStatus.ERROR));
                    }

                    return tuple(columnNumber.getAndIncrement(), mkHeader(definition, rating, errors, ResolutionStatus.NEW));

                })
                .collect(Collectors.toSet());
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


    public static Tuple2<Tuple2<Integer, String[]>, Set<Tuple2<Integer, String[]>>> readRows(String inputString) {

        if (isEmpty(inputString)) {
            throw new IllegalStateException("Cannot parse empty data string");
        }

        Siphon<Tuple2<Integer, String[]>> headerRowSiphon = mkSiphon(t -> t.v1.equals(1));

        Set<Tuple2<Integer, String[]>> dataRows = streamRowData(inputString)
                .filter(headerRowSiphon)
                .collect(Collectors.toSet());

        if (headerRowSiphon.getResults().size() != 1) {
            throw new IllegalStateException("Must have one header row");
        }

        return tuple(first(headerRowSiphon.getResults()), dataRows);
    }

}
