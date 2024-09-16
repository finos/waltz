package org.finos.waltz.service.taxonomy_management;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyItem;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyParseResult;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyParseResult.BulkTaxonomyParseError;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyItemParser.InputFormat;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.IOUtilities.readAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulkTaxonomyItemParserTest {

    private final BulkTaxonomyItemParser parser = new BulkTaxonomyItemParser();

    @Test
    void simpleCSV() {
        BulkTaxonomyParseResult result = parser.parse(readTestFile("test-taxonomy-items.csv"), InputFormat.CSV);
        assertNull(result.error());
        assertEquals(3, result.parsedItems().size());
        assertNames(result.parsedItems(), "A1", "A1_1", "A1_2");
    }


    @Test
    void dodgyCSV() {
        BulkTaxonomyParseResult result = parser.parse(readTestFile("test-bad-taxonomy-items.csv"), InputFormat.CSV);
        assertNotNull(result.error());
        assertTrue(result.error().message().contains("externalId"));
    }


    @Test
    void canHandleCSVsWithAliasedColumns() {
        BulkTaxonomyParseResult result = parser.parse(readTestFile("test-taxonomy-items-aliased.csv"), InputFormat.CSV);
        assertNull(result.error());
        assertEquals(3, result.parsedItems().size());
        assertNames(result.parsedItems(), "A1", "A1_1", "A1_2");
    }


    @Test
    void canHandleJSONsWithAliasedProperties() {
        BulkTaxonomyParseResult result = parser.parse(readTestFile("test-taxonomy-items-aliased.json"), InputFormat.JSON);
        assertNull(result.error());
        assertEquals(3, result.parsedItems().size());
        assertNames(result.parsedItems(), "A1", "A1_1", "A1_2");
    }


    @Test
    void simpleTSV() {
        BulkTaxonomyParseResult result = parser.parse(readTestFile("test-taxonomy-items.tsv"), InputFormat.TSV);
        assertNull(result.error());
        assertEquals(3, result.parsedItems().size());
        assertNames(result.parsedItems(), "A1", "A1_1", "A1_2");
    }


    @Test
    void simpleJSON() {
        BulkTaxonomyParseResult result = parser.parse(readTestFile("test-taxonomy-items.json"), InputFormat.JSON);
        assertNull(result.error());
        assertEquals(3, result.parsedItems().size());
        assertNames(result.parsedItems(), "A1", "A1_1", "A1_2");
    }


    @Test
    void parseFailsIfGivenEmpty() {
        BulkTaxonomyParseResult result = parser.parse("", InputFormat.TSV);
        expectEmptyError(result);
    }


    @Test
    void parseFailsIfGivenNull() {
        BulkTaxonomyParseResult result = parser.parse(null, InputFormat.TSV);
        expectEmptyError(result);
    }


    @Test
    void parseFailsIfGivenBlanks() {
        BulkTaxonomyParseResult result = parser.parse("   \t\n  ", InputFormat.TSV);
        expectEmptyError(result);
    }


    @Test
    void parseIgnoresEmptyLines() {
        BulkTaxonomyParseResult result = parser.parse(readTestFile("test-taxonomy-items-with-blanks.tsv"), InputFormat.TSV);
        assertEquals(3, result.parsedItems().size());
        assertNames(result.parsedItems(), "A1", "A1_1", "A1_2");
    }


    @Test
    void parseIgnoresCommentedLines() {
        BulkTaxonomyParseResult result = parser.parse(readTestFile("test-taxonomy-items-with-commented-lines.csv"), InputFormat.CSV);
        assertEquals(3, result.parsedItems().size());
        assertNames(result.parsedItems(), "A1", "A1_1", "A1_2");
    }


    // -- HELPERS ------------------

    private void expectEmptyError(BulkTaxonomyParseResult result) {
        BulkTaxonomyParseError err = result.error();
        assertNotNull(err);
        assertTrue(err.message().contains("empty"));
        assertEquals(0, err.column());
        assertEquals(0, err.line());
    }


    private String readTestFile(String fileName) {
        return readAsString(BulkTaxonomyItemParserTest.class.getResourceAsStream(fileName));
    }


    private void assertNames(List<BulkTaxonomyItem> items,
                             String... names) {

        Set<String> itemNames = SetUtilities.map(items, BulkTaxonomyItem::name);
        Set<String> expectedNames = SetUtilities.asSet(names);
        assertEquals(expectedNames, itemNames);
    }

}