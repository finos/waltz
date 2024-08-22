package org.finos.waltz.service.taxonomy_management;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.finos.waltz.model.taxonomy_management.BulkTaxonomyItem;
import org.finos.waltz.model.taxonomy_management.BulkTaxonomyParseResult;
import org.finos.waltz.model.taxonomy_management.ImmutableBulkTaxonomyParseError;
import org.finos.waltz.model.taxonomy_management.ImmutableBulkTaxonomyParseResult;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.isEmpty;

public class BulkTaxonomyItemParser {

    public enum InputFormat {
        CSV,
        TSV,
        JSON
    }


    public BulkTaxonomyParseResult parse(String input, InputFormat format) {
        if (isEmpty(input)) {
            return handleEmptyInput(input);
        }

        try {
            switch (format) {
                case CSV:
                    return parseCSV(input);
                case TSV:
                    return parseTSV(input);
                case JSON:
                    return parseJSON(input);
                default:
                    throw new IllegalArgumentException(format("Unknown format: %s", format));
            }
        } catch (IOException e) {
            return ImmutableBulkTaxonomyParseResult
                    .builder()
                    .input(input)
                    .error(ImmutableBulkTaxonomyParseError
                            .builder()
                            .message(e.getMessage())
                            .build())
                    .build();
        }
    }


    private BulkTaxonomyParseResult parseJSON(String input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MappingIterator<BulkTaxonomyItem> items = mapper
                .readerFor(BulkTaxonomyItem.class)
                .readValues(input);

        return BulkTaxonomyParseResult.mkResult(
                items.readAll(),
                input);
    }


    private BulkTaxonomyParseResult parseCSV(String input) throws IOException {
        List<BulkTaxonomyItem> items = attemptToParseDelimited(input, configureCSVSchema());
        return BulkTaxonomyParseResult.mkResult(items, input);
    }


    private BulkTaxonomyParseResult parseTSV(String input) throws IOException {
        List<BulkTaxonomyItem> items = attemptToParseDelimited(input, configureTSVSchema());
        return BulkTaxonomyParseResult.mkResult(items, input);
    }


    private List<BulkTaxonomyItem> attemptToParseDelimited(String input,
                                                        CsvSchema bootstrapSchema) throws IOException {
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.TRIM_SPACES);
        MappingIterator<BulkTaxonomyItem> items = mapper
                .readerFor(BulkTaxonomyItem.class)
                .with(bootstrapSchema)
                .readValues(input);

        return items.readAll();
    }


    private CsvSchema configureCSVSchema() {
        return CsvSchema
                .emptySchema()
                .withHeader();
    }


    private CsvSchema configureTSVSchema() {
        return CsvSchema
                .emptySchema()
                .withHeader()
                .withColumnSeparator('\t');
    }


    private BulkTaxonomyParseResult handleEmptyInput(String input) {
        return ImmutableBulkTaxonomyParseResult
                .builder()
                .input(input)
                .error(ImmutableBulkTaxonomyParseError
                        .builder()
                        .message("Cannot parse an empty string")
                        .column(0)
                        .line(0)
                        .build())
                .build();
    }

}
