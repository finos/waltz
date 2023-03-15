package org.finos.waltz.service.bulk_upload.column_parsers;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.ResolvedAssessmentHeaderStatus;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentCell;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.AssessmentHeaderCell;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.BulkUploadError;
import org.finos.waltz.service.bulk_upload.TabularDataUtilities.Row;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;

import static org.finos.waltz.common.StringUtilities.safeTrim;

public interface ColumnParser extends BiFunction<String, Collection<Tuple2<Long, Long>>, AssessmentCell> {

    static ColumnParser mkColumnParser(AssessmentHeaderCell headerCell) {
        if (headerCell.status() == ResolvedAssessmentHeaderStatus.HEADER_FOUND && headerCell.resolvedRating().isPresent()) {
            return new SpecificRatingColumnParser(headerCell);
        } else if (headerCell.status() == ResolvedAssessmentHeaderStatus.HEADER_FOUND && !headerCell.resolvedRating().isPresent()) {
            return new GenericRatingColumnParser(headerCell);
        } else {
            return new AlwaysFailingColumnParser(headerCell);
        }
    }

    static <T extends BulkUploadError> ResolutionStatus determineResolutionStatus(boolean alreadyExists,
                                                                                  Set<T> errors) {
        if (!CollectionUtilities.isEmpty(errors)) {
            return ResolutionStatus.ERROR;
        } else if (alreadyExists) {
            return ResolutionStatus.EXISTING;
        } else {
            return ResolutionStatus.NEW;
        }
    }


    static String sanitize(String ratingString) {
        return safeTrim(ratingString).toLowerCase();
    }

    AssessmentHeaderCell getHeader();
}
