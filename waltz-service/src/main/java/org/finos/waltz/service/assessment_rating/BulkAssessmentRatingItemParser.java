package org.finos.waltz.service.assessment_rating;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.finos.waltz.common.StreamUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.assessment_rating.bulk_upload.AssessmentRatingParsedItem;
import org.finos.waltz.model.assessment_rating.bulk_upload.AssessmentRatingParsedResult;
import org.finos.waltz.model.assessment_rating.bulk_upload.ImmutableAssessmentRatingParseError;
import org.finos.waltz.model.assessment_rating.bulk_upload.ImmutableAssessmentRatingParsedResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.StringUtilities.isEmpty;

@Service
public class BulkAssessmentRatingItemParser {

    public enum InputFormat {
        CSV,
        TSV,
        JSON
    }


    public AssessmentRatingParsedResult parse(String input, InputFormat format) {
        if (isEmpty(input)) {
            return handleEmptyInput(input);
        }

        try {
            switch (format) {
                case TSV:
                    return parseTSV(clean(input));
                default:
                    throw new IllegalArgumentException(format("Unknown format: %s", format));
            }
        } catch (IOException e) {
            return ImmutableAssessmentRatingParsedResult
                    .builder()
                    .input(input)
                    .error(ImmutableAssessmentRatingParseError
                            .builder()
                            .message(e.getMessage())
                            .build())
                    .build();
        }
    }

    private AssessmentRatingParsedResult parseTSV(String input) throws IOException {
        List<AssessmentRatingParsedItem> items = attemptToParseDelimited(input, configureTSVSchema());
        return AssessmentRatingParsedResult.mkResult(items, input);
    }


    private List<AssessmentRatingParsedItem> attemptToParseDelimited(String input,
                                                                     CsvSchema bootstrapSchema) throws IOException {
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.TRIM_SPACES);
        mapper.enable(CsvParser.Feature.SKIP_EMPTY_LINES);
        MappingIterator<AssessmentRatingParsedItem> items = mapper
                .readerFor(AssessmentRatingParsedItem.class)
                .with(bootstrapSchema)
                .readValues(input);

        return items.readAll();
    }

    private CsvSchema configureTSVSchema() {
        return CsvSchema
                .emptySchema()
                .withHeader()
                .withColumnSeparator('\t');
    }


    private AssessmentRatingParsedResult handleEmptyInput(String input) {
        return ImmutableAssessmentRatingParsedResult
                .builder()
                .input(input)
                .error(ImmutableAssessmentRatingParseError
                        .builder()
                        .message("Cannot parse an empty string")
                        .column(0)
                        .line(0)
                        .build())
                .build();
    }

    private String clean(String input) {
        return StreamUtilities
                .lines(input)
                .filter(StringUtilities::isDefined)
                .filter(line -> ! line.startsWith("#"))
                .collect(Collectors.joining("\n"));
    }
}
