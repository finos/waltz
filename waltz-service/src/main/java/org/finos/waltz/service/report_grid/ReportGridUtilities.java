package org.finos.waltz.service.report_grid;

import org.finos.waltz.common.ArrayUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableIdSelectionOptions;
import org.finos.waltz.model.report_grid.*;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ReportGridUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGridUtilities.class);
    private static final int HEADER_COLUMN_COUNT = 4;
    private static final int FILTER_OPTIONS_COLUMN_COUNT = 3;
    private static final String TABLE_HEADER = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |";
    private static final String FILTER_HEADER = "| Filter Column | Filter Operator | Value/s |";
    private static final String FILTER_DELIMETER = ";";


    public static Tuple2<List<String>, List<List<String>>> parseGridFilterNoteText(String noteText) {

        if (isEmpty(noteText)) {
            return null;
        }

        String[] lines = noteText.split("\\r?\\n");


        List<List<String>> headerRows = parseTableData(lines, TABLE_HEADER);
        List<List<String>> filterRows = parseTableData(lines, FILTER_HEADER);

        if (headerRows.size() != 1) {
            throw new IllegalArgumentException(format(
                    "Incorrect number of header rows found [%d], ensure there are blank rows between tables",
                    headerRows.size()));
        }

        List<String> headerRow = headerRows
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot identify header row"));

        if (headerRow.size() != HEADER_COLUMN_COUNT) {
            throw new IllegalArgumentException(format(
                    "Incorrect number of header columns found [%d], should follow : [Grid Name, Grid Identifier, Vantage Point Kind, Vantage Point Id]",
                    headerRow.size()));
        }

        List<String> filterRow = filterRows
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Filter rows identified"));

        if (filterRow.size() != FILTER_OPTIONS_COLUMN_COUNT) {
            throw new IllegalArgumentException(format(
                    "Incorrect number of filter columns found [%d], should follow : [Filter Column, Filter Operator, Value/s]",
                    filterRow.size()));
        }

        return tuple(headerRow, filterRows);
    }


    public static Set<String> getFilterValues(String string) {
        return Arrays
                .stream(string.split(FILTER_DELIMETER))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    public static List<List<String>> parseTableData(String[] lines, String tableHeader) {

        if (ArrayUtilities.isEmpty(lines)) {
            return new ArrayList<>();
        }

        String sanitizedHeader = sanitizeString(tableHeader);

        List<List<String>> cells = new ArrayList<>();

        AtomicBoolean savingRows = new AtomicBoolean(false);

        Arrays.stream(lines)
                .iterator()
                .forEachRemaining(line -> {

                    //Stop parsing when hit empty line
                    if (StringUtilities.isEmpty(line)) {
                        savingRows.set(false);
                        return;
                    }

                    //Ignore lines which don't look part of a table or are just describing table structure
                    if (!line.startsWith("|") || line.startsWith("|--") || line.startsWith("| --")) {
                        return;
                    }

                    //Saving rows must come before checking header so that table header doesn't get saved
                    if (savingRows.get()) {

                        List<String> cellData = Arrays
                                .stream(line.split("\\|"))
                                .map(s -> s.replaceAll("`", "").trim())
                                .filter(s -> !StringUtilities.isEmpty(s))
                                .collect(Collectors.toList());

                        cells.add(cellData);
                    }

                    //Start parsing when you hit the header row
                    if (sanitizeString(line).contains(sanitizedHeader)) {
                        savingRows.set(true);
                    }
                });

        return cells;
    }

    public static Set<GridFilter> getGridFilters(List<List<String>> filterRows, ReportGridDefinition grid) {

        Map<String, Long> columnsDefinitionIdByName;

        try {
            columnsDefinitionIdByName = indexBy(grid.fixedColumnDefinitions(),
                    r -> r.entityFieldReference() == null
                            ? sanitizeString(r.columnName())
                            : sanitizeString(format("%s/%s", r.entityFieldReference().displayName(), r.columnName())),
                    ReportGridFixedColumnDefinition::gridColumnId);

        } catch (Exception e) {
            LOG.error("Could not fetch grid filters, grid cannot have multiple columns with the same name");
            return Collections.emptySet();
        }

        return filterRows
                .stream()
                .map(r -> {

                    String columnString = r.get(0);
                    String filterOperator = r.get(1);
                    String values = r.get(2);

                    String columnName = sanitizeString(columnString);
                    Long columnDefnId = columnsDefinitionIdByName.get(columnName);

                    if (columnDefnId == null) {
                        LOG.info(format("Cannot find column '%s' on grid. Skipping this filter", columnName));
                        return null;
                    } else {

                        Optional<FilterOperator> operator = FilterOperator.parseString(filterOperator);

                        return operator
                                .map(op -> ImmutableGridFilter.builder()
                                        .columnDefinitionId(columnDefnId)
                                        .filterOperator(op)
                                        .filterValues(getFilterValues(values))
                                        .build())
                                .orElse(null);

                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static String sanitizeString(String name) {
        return mkSafe(name)
                .replaceAll("[:;*?/\\\\]", "")
                .replaceAll("\\s+", "")
                .toLowerCase()
                .trim();
    }

    public static IdSelectionOptions modifySelectionOptionsForGrid(IdSelectionOptions idSelectionOptions) {
        return idSelectionOptions.entityReference().kind() == EntityKind.PERSON
                ? ImmutableIdSelectionOptions
                .copyOf(idSelectionOptions)
                .withScope(HierarchyQueryScope.EXACT)
                : idSelectionOptions;
    }

}
