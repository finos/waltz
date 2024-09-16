package org.finos.waltz.service.measurable_rating;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingItem;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingParseResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.IOUtilities.readAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BulkMeasurableRatingItemParserTest {

    private final BulkMeasurableItemParser parser = new BulkMeasurableItemParser();

    @Test
    void simpleTSV() {
        BulkMeasurableRatingParseResult result = parser.parse(readTestFile("test-measurable-rating-items.tsv"), BulkMeasurableItemParser.InputFormat.TSV);
        assertNull(result.error());
        assertEquals(3, result.parsedItems().size());
        assertNarIds(result.parsedItems(), "109235-1", "109235-1", "324234-1");
    }

    private void assertNarIds(List<BulkMeasurableRatingItem> parsedItems, String... resultNarIds) {
        Set<String> narIds = SetUtilities.map(parsedItems, BulkMeasurableRatingItem::assetCode);
        Set<String> expectedNarIds = SetUtilities.asSet(resultNarIds);
        assertEquals(expectedNarIds, narIds);
    }

    private String readTestFile(String fileName) {
        return readAsString(BulkMeasurableRatingItemParserTest.class.getResourceAsStream(fileName));
    }
}
