package org.finos.waltz.service.bulk_upload.column_parsers;

import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.*;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class SpecificRatingColumnParser implements ColumnParser {
    private final AssessmentHeaderCell headerCell;


    public SpecificRatingColumnParser(AssessmentHeaderCell headerCell) {
        this.headerCell = headerCell;
    }


    @Override
    public AssessmentCell apply(String cellValue,
                                Collection<Tuple2<Long, Long>> existingRatings) {


        String comment = cellValue.equalsIgnoreCase("X") || cellValue.equalsIgnoreCase("Y")
                ? null
                : cellValue;

        Long defnId = headerCell.resolvedAssessmentDefinition().flatMap(IdProvider::id).orElse(null);
        Long ratingId = headerCell.resolvedRating().flatMap(IdProvider::id).orElse(null);

        boolean existingRating = existingRatings.contains(tuple(defnId, ratingId));

        ResolutionStatus status = ColumnParser.determineResolutionStatus(existingRating, emptySet());

        AssessmentCellRating cellRatings = ImmutableAssessmentCellRating.builder()
                .resolvedRating(headerCell.resolvedRating().get())
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

    @Override
    public AssessmentHeaderCell getHeader() {
        return headerCell;
    }
}
