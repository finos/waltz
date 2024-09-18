package org.finos.waltz.service.measurable_rating;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingItem;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingParseResult;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyParseResult;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyItemParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.IOUtilities.readAsString;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void parseFailsIfGivenEmpty() {
        BulkMeasurableRatingParseResult result = parser.parse("", BulkMeasurableItemParser.InputFormat.TSV);
        expectEmptyError(result);
    }

    @Test
    void parseFailsIfGivenNull() {
        BulkMeasurableRatingParseResult result = parser.parse(null, BulkMeasurableItemParser.InputFormat.TSV);
        expectEmptyError(result);
    }

    @Test
    void parseFailsIfGivenBlanks() {
        BulkMeasurableRatingParseResult result = parser.parse("   \t\n  ", BulkMeasurableItemParser.InputFormat.TSV);
        expectEmptyError(result);
    }

    @Test
    void parseIgnoresEmptyLines() {
        BulkMeasurableRatingParseResult result = parser.parse(readTestFile("test-measurable-rating-items-with-blanks.tsv"), BulkMeasurableItemParser.InputFormat.TSV);
        assertEquals(3, result.parsedItems().size());
        assertNarIds(result.parsedItems(), "109235-1", "109235-1", "324234-1");
    }

    @Test
    void parseIgnoresCommentedLines() {
        BulkMeasurableRatingParseResult result = parser.parse(readTestFile("test-measurable-rating-items-with-commented-lines.csv"), BulkMeasurableItemParser.InputFormat.CSV);
        assertEquals(2, result.parsedItems().size());
        assertNarIds(result.parsedItems(), "109235-1", "324234-1");
    }

    private void expectEmptyError(BulkMeasurableRatingParseResult result) {
        BulkMeasurableRatingParseResult.BulkMeasurableRatingParseError err = result.error();
        assertNotNull(err);
        assertTrue(err.message().contains("empty"));
        assertEquals(0, err.column());
        assertEquals(0, err.line());
    }

}
