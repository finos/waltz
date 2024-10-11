package org.finos.waltz.service.assessment_rating;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.assessment_rating.bulk_upload.AssessmentRatingParsedItem;
import org.finos.waltz.model.assessment_rating.bulk_upload.AssessmentRatingParsedResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.IOUtilities.readAsString;
import static org.junit.jupiter.api.Assertions.*;

public class BulkAssessmentRatingServiceItemParserTest {

    private final BulkAssessmentRatingItemParser parser = new BulkAssessmentRatingItemParser();

    private String readTestFile(String fileName) {
        return readAsString(BulkAssessmentRatingItemParser.class.getResourceAsStream(fileName));
    }

    @Test
    void simpleTSV() {
        AssessmentRatingParsedResult result = parser.parse(readTestFile("test-assessment-rating-items.tsv"), BulkAssessmentRatingItemParser.InputFormat.TSV);
        assertNull(result.error());
        assertEquals(1, result.parsedItems().size());
        assertExternalIds(result.parsedItems(), "IN75");
    }

    private void assertExternalIds(List<AssessmentRatingParsedItem> parsedItems, String... resultNarIds) {
        Set<String> externalIds = SetUtilities.map(parsedItems, AssessmentRatingParsedItem::externalId);
        Set<String> expectedNarIds = SetUtilities.asSet(resultNarIds);
        assertEquals(expectedNarIds, externalIds);
    }
}
