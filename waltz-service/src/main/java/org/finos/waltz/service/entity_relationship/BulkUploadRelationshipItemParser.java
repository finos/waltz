package org.finos.waltz.service.entity_relationship;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.finos.waltz.common.StreamUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.bulk_upload.entity_relationship.BulkUploadRelationshipItem;
import org.finos.waltz.model.bulk_upload.entity_relationship.BulkUploadRelationshipParsedResult;
import org.finos.waltz.model.bulk_upload.entity_relationship.ImmutableBulkUploadRelationshipParsedResult;
import org.finos.waltz.model.bulk_upload.entity_relationship.ImmutableBulkUploadRelationshipParseError;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.isEmpty;

public class BulkUploadRelationshipItemParser {

    public enum InputFormat {
        CSV,
        TSV,
        JSON
    }

    public BulkUploadRelationshipParsedResult parse(String input, InputFormat format) {
        if (isEmpty(input)) {
            return handleEmptyInput(input);
        }
        try {
            switch (format) {
                case CSV:
                    return parseCSV(clean(input));
                case TSV:
                    return parseTSV(clean(input));
                case JSON:
                    return parseJSON(clean(input));
                default:
                    throw new IllegalArgumentException(format("Unknown format: %s", format));
            }
        } catch (IOException e) {
            return ImmutableBulkUploadRelationshipParsedResult
                    .builder()
                    .input(input)
                    .error(ImmutableBulkUploadRelationshipParseError
                            .builder()
                            .message(e.getMessage())
                            .build())
                    .build();
        }
    }

    private BulkUploadRelationshipParsedResult parseJSON(String input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MappingIterator<BulkUploadRelationshipItem> items = mapper
                .readerFor(BulkUploadRelationshipItem.class)
                .readValues(input);

        return BulkUploadRelationshipParsedResult.mkResult(
                items.readAll(),
                input);
    }

    private BulkUploadRelationshipParsedResult parseTSV(String input) throws IOException {
        List<BulkUploadRelationshipItem> items = attemptToParseDelimited(input, configTSVSchema());
        return BulkUploadRelationshipParsedResult.mkResult(items, input);
    }

    private BulkUploadRelationshipParsedResult parseCSV(String input) throws IOException {
        List<BulkUploadRelationshipItem> items = attemptToParseDelimited(input, configCSVSchema());
        return BulkUploadRelationshipParsedResult.mkResult(items, input);
    }

    private List<BulkUploadRelationshipItem> attemptToParseDelimited(String input, CsvSchema bootstrapSchema) throws IOException {
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.TRIM_SPACES);
        mapper.enable(CsvParser.Feature.SKIP_EMPTY_LINES);

        MappingIterator<BulkUploadRelationshipItem> items = mapper
                .readerFor(BulkUploadRelationshipItem.class)
                .with(bootstrapSchema)
                .readValues(input);

        return items.readAll();
    }

    private BulkUploadRelationshipParsedResult handleEmptyInput(String input) {
        return ImmutableBulkUploadRelationshipParsedResult
                .builder()
                .error(ImmutableBulkUploadRelationshipParseError
                        .builder()
                        .message("Cannot parse input.")
                        .column(0)
                        .line(0)
                        .build())
                .input(input)
                .build();
    }

    private CsvSchema configCSVSchema() {
        return CsvSchema
                .emptySchema()
                .withHeader();
    }

    private CsvSchema configTSVSchema() {
        return CsvSchema
                .emptySchema()
                .withHeader()
                .withColumnSeparator('\t');
    }

    private String clean(String input) {
        return StreamUtilities
                .lines(input)
                .filter(StringUtilities::isDefined)
                .filter(line -> !line.startsWith("#"))
                .collect(Collectors.joining("\n"));
    }
}
