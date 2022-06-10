package org.finos.waltz.web.endpoints.extracts.dynamic;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.entity_field_reference.EntityFieldReference;
import org.finos.waltz.model.report_grid.ReportGridColumnDefinition;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.common.ListUtilities.newArrayList;

@Component
public class FormatterUtils {


    public List<String> mkHeaderStrings(List<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions) {
        List<String> staticHeaders = newArrayList(
                "Subject Id",
                "Subject Name",
                "Subject External Id",
                "Subject Lifecycle Phase");

        List<String> columnHeaders = columnDefinitions
                .stream()
                .sorted(Comparator.comparingLong(r -> r.v1.position()))
                .flatMap(r -> {
                    String name = getColumnName(r.v1);
                    Boolean needsComment = r.v2;
                    return Stream.of(
                            name,
                            needsComment
                                    ? String.format("%s: comment", name)
                                    : null);

                })
                .filter(Objects::nonNull)
                .collect(toList());

        return concat(
                staticHeaders,
                columnHeaders);
    }


    private String getColumnName(ReportGridColumnDefinition column) {
        if (column.displayName() != null) {
            return column.displayName();
        } else {
            String fieldName = Optional
                    .ofNullable(column.entityFieldReference())
                    .map(EntityFieldReference::displayName)
                    .orElse(null);

            return ListUtilities.asList(fieldName, column.columnName())
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" / "));
        }
    }
}
