package org.finos.waltz.web.endpoints.extracts.dynamic;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.entity_field_reference.EntityFieldReference;
import org.finos.waltz.model.report_grid.ReportGridColumnDefinition;
import org.finos.waltz.web.endpoints.extracts.ColumnCommentary;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.common.ListUtilities.newArrayList;

@Component
public class FormatterUtils {


    public List<String> mkHeaderStrings(List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> columnDefinitions) {
        List<String> staticHeaders = newArrayList(
                "Subject Id",
                "Subject Name",
                "Subject External Id",
                "Subject Lifecycle Phase");
        List<String> columnHeaders = mkColumnHeaders(columnDefinitions);
        return concat(
                staticHeaders,
                columnHeaders);
    }


    public List<String> mkColumnHeaders(List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> columnDefinitions){
        if(columnDefinitions==null){
            return new ArrayList<>();
        }
        return columnDefinitions
                .stream()
                .sorted(Comparator.comparingLong(r -> r.v1.position()))
                .flatMap(r -> {
                    String name = getColumnName(r.v1);
                    return Stream.of(
                            name,
                            ColumnCommentary.HAS_COMMENTARY.equals(r.v2)
                                    ? String.format("%s: comment", name)
                                    : null);

                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public String getColumnName(ReportGridColumnDefinition column) {
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

    /**
     * Column names are composed of A / B     Eg King / Change Initiative.  For the JSON
     * representation we only part 'A'
     */
    public String getShortColumnName(String composedColumnName) {
        if(composedColumnName!=null){
            String[] components = composedColumnName.split("/");
            return components[0].trim();
        }
        return "";
    }
}
