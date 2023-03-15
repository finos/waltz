package org.finos.waltz.service.bulk_upload.column_parsers;

import org.finos.waltz.common.OptionalUtilities;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.*;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.StringUtilities.splitThenMap;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class GenericRatingColumnParser implements ColumnParser {
    private final AssessmentHeaderCell headerCell;

    public GenericRatingColumnParser(AssessmentHeaderCell headerCell) {
        this.headerCell = headerCell;
    }


    @Override
    public AssessmentCell apply(String cellValue, Collection<Tuple2<Long, Long>> existingRatingInfo) {

        Set<RatingResolutionError> errors = new HashSet<>();

        Long defnId = headerCell.resolvedAssessmentDefinition().flatMap(IdProvider::id).orElse(null);

        List<AssessmentCellRating> ratings = splitThenMap(
                cellValue,
                ";",
                ratingString -> {

                    String lookupValue = ColumnParser.sanitize(ratingString);

                    Optional<RatingSchemeItem> rating = ofNullable(headerCell.ratingLookupMap().get(lookupValue));

                    if (OptionalUtilities.isEmpty(rating)) {
                        errors.add(RatingResolutionError.mkError(
                                RatingResolutionErrorCode.RATING_VALUE_NOT_FOUND,
                                format("Could not identify rating value from: '%s'", ratingString)));
                    }

                    boolean ratingExists = existingRatingInfo.contains(tuple(defnId, rating.flatMap(IdProvider::id)));
                    ResolutionStatus status = ColumnParser.determineResolutionStatus(ratingExists, errors);

                    return ImmutableAssessmentCellRating.builder()
                            .resolvedRating(rating)
                            .errors(errors)
                            .status(status)
                            .build();
                });

        return ImmutableAssessmentCell.builder()
                .inputString(cellValue)
                .columnId(headerCell.columnId())
                .ratings(ratings)
                .build();
    }


    @Override
    public AssessmentHeaderCell getHeader() {
        return headerCell;
    }
}
