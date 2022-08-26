package org.finos.waltz.service.report_grid;

import org.finos.waltz.common.ArrayUtilities;
import org.finos.waltz.common.Checks;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableIdSelectionOptions;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ReportGridUtilities {

    public static Tuple2<ArrayList<List<String>>, ArrayList<List<String>>> parseNoteText(String noteText) {

        if (isEmpty(noteText)) {
            return null;
        }

        String[] lines = noteText.split("\\r?\\n");

        String tableHeader = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |";
        String filterHeader = "| Filter Column | Column Option Codes |";

        ArrayList<List<String>> headerRows = parseTableData(lines, tableHeader);
        ArrayList<List<String>> filterRows = parseTableData(lines, filterHeader);

        return tuple(headerRows, filterRows);
    }


    public static Set<String> getFilterValues(String string) {
        return Arrays
                .stream(string.split(";"))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    public static ArrayList<List<String>> parseTableData(String[] lines, String tableHeader) {

        if (ArrayUtilities.isEmpty(lines)) {
            return new ArrayList<>();
        }

        String sanitizedHeader = sanitizeString(tableHeader);

        ArrayList<List<String>> cells = new ArrayList<>();

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
                                .map(String::trim)
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
