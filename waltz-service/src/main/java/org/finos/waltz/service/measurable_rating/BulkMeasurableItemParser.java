package org.finos.waltz.service.measurable_rating;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingItem;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingParseResult;
import org.finos.waltz.model.bulk_upload.measurable_rating.ImmutableBulkMeasurableRatingParseError;
import org.finos.waltz.model.bulk_upload.measurable_rating.ImmutableBulkMeasurableRatingParseResult;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.isEmpty;

public class BulkMeasurableItemParser {

    public enum InputFormat {
        CSV,
        TSV,
        JSON
    }


    public BulkMeasurableRatingParseResult parse(String input, InputFormat format) {
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
            return ImmutableBulkMeasurableRatingParseResult
                    .builder()
                    .input(input)
                    .error(ImmutableBulkMeasurableRatingParseError
                            .builder()
                            .message(e.getMessage())
                            .build())
                    .build();
        }
    }


    private BulkMeasurableRatingParseResult parseJSON(String input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MappingIterator<BulkMeasurableRatingItem> items = mapper
                .readerFor(BulkMeasurableRatingItem.class)
                .readValues(input);

        return BulkMeasurableRatingParseResult.mkResult(
                items.readAll(),
                input);
    }


    private BulkMeasurableRatingParseResult parseCSV(String input) throws IOException {
        List<BulkMeasurableRatingItem> items = attemptToParseDelimited(input, configureCSVSchema());
        return BulkMeasurableRatingParseResult.mkResult(items, input);
    }


    private BulkMeasurableRatingParseResult parseTSV(String input) throws IOException {
        List<BulkMeasurableRatingItem> items = attemptToParseDelimited(input, configureTSVSchema());
        return BulkMeasurableRatingParseResult.mkResult(items, input);
    }


    private List<BulkMeasurableRatingItem> attemptToParseDelimited(String input,
                                                        CsvSchema bootstrapSchema) throws IOException {
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.TRIM_SPACES);
        MappingIterator<BulkMeasurableRatingItem> items = mapper
                .readerFor(BulkMeasurableRatingItem.class)
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


    private BulkMeasurableRatingParseResult handleEmptyInput(String input) {
        return ImmutableBulkMeasurableRatingParseResult
                .builder()
                .input(input)
                .error(ImmutableBulkMeasurableRatingParseError
                        .builder()
                        .message("Cannot parse an empty string")
                        .column(0)
                        .line(0)
                        .build())
                .build();
    }

}
