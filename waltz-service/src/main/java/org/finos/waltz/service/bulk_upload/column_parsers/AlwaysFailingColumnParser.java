package org.finos.waltz.service.bulk_upload.column_parsers;

import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.*;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;

import static org.finos.waltz.common.StringUtilities.isEmpty;

public class AlwaysFailingColumnParser implements ColumnParser {
    private final AssessmentHeaderCell headerCell;

    public AlwaysFailingColumnParser(AssessmentHeaderCell headerCell) {
        this.headerCell = headerCell;
    }


    @Override
    public AssessmentCell apply(String cellValue,
                                Collection<Tuple2<Long, Long>> existingRatings) {

        if (isEmpty(cellValue)) {
            return ImmutableAssessmentCell.builder()
                    .columnId(headerCell.columnId())
                    .inputString(cellValue)
                    .build();
        } else {
            return ImmutableAssessmentCell.builder()
                    .columnId(headerCell.columnId())
                    .inputString(cellValue)
                    .addRatings(ImmutableAssessmentCellRating
                            .builder()
                            .status(ResolutionStatus.ERROR)
                            .addErrors(RatingResolutionError.mkError(
                                    RatingResolutionErrorCode.RATING_VALUE_NOT_FOUND,
                                    "Invalid column header, cannot parse cell"))
                            .build())
                    .build();
        }

    }


    @Override
    public AssessmentHeaderCell getHeader() {
        return headerCell;
    }
}
