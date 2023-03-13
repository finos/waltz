package org.finos.waltz.service.bulk_upload.column_parsers;

import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.*;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class SpecificRatingColumnParser implements ColumnParser {
    private final AssessmentHeaderCell headerCell;


    public SpecificRatingColumnParser(AssessmentHeaderCell headerCell) {
        this.headerCell = headerCell;
    }


    @Override
    public AssessmentCell apply(String cellValue,
                                Collection<Tuple2<Long, Long>> existingRatings) {

        Optional<RatingSchemeItem> rating = isEmpty(cellValue)
                ? Optional.empty()
                : headerCell.resolvedRating();

        String comment = cellValue.equalsIgnoreCase("X") || cellValue.equalsIgnoreCase("Y")
                ? null
                : cellValue;

        ResolutionStatus status = getResolutionStatus(existingRatings);

        AssessmentCellRating cellRatings = ImmutableAssessmentCellRating.builder()
                .resolvedRating(rating)
                .comment(ofNullable(comment))
                .errors(emptySet())
                .status(status)
                .build();

        return ImmutableAssessmentCell.builder()
                .columnId(headerCell.columnId())
                .inputString(cellValue)
                .ratings(asSet(cellRatings))
                .build();
    }

    private ResolutionStatus getResolutionStatus(Collection<Tuple2<Long, Long>> existingRatings) {

        Long defnId = headerCell.resolvedAssessmentDefinition().flatMap(IdProvider::id).orElse(null);
        Long ratingId = headerCell.resolvedRating().flatMap(IdProvider::id).orElse(null);

        boolean existingRating = existingRatings.contains(tuple(defnId, ratingId));

        return ColumnParser.determineResolutionStatus(existingRating, emptySet());
    }

    @Override
    public AssessmentHeaderCell getHeader() {
        return headerCell;
    }
}
