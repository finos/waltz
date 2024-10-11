package org.finos.waltz.service.assessment_rating;

import org.finos.waltz.common.FunctionUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.CommonTableFieldsRegistry;
import org.finos.waltz.model.CommonTableFields;
import org.finos.waltz.model.DiffResult;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.assessment_rating.ImmutableAssessmentRating;
import org.finos.waltz.model.assessment_rating.bulk_upload.*;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.service.user.UserRoleService;
import org.jooq.*;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.platform.commons.util.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.asSet;
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

    private final DSLContext dsl;


    @Autowired
    public BulkAssessmentRatingService(AssessmentDefinitionService assessmentDefinitionService, AssessmentRatingService assessmentRatingService, RatingSchemeService ratingSchemeService, UserRoleService userRoleService, DSLContext dsl) {
        this.assessmentDefinitionService = assessmentDefinitionService;
        this.assessmentRatingService = assessmentRatingService;
        this.ratingSchemeService = ratingSchemeService;
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
                    .where(ctf.isActiveCondition())
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

                    if (t.v2() == null) {
                        validationErrors.add(ValidationError.ENTITY_KIND_NOT_FOUND);
                    }
                    if (t.v3() == null) {
                        validationErrors.add(ValidationError.RATING_NOT_FOUND);
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
}
