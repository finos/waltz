package org.finos.waltz.service.report_grid;

import org.finos.waltz.common.Checks;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.report_grid.ReportGridCell;
import org.finos.waltz.model.report_grid.ReportGridDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.service.report_grid.ReportGridUtilities.sanitizeString;

public class ReportGridEvaluatorNamespace {

    private final ReportGridDefinition definition;
    private Map<String, Object> ctx = new HashMap<>();

    public ReportGridEvaluatorNamespace(ReportGridDefinition definition) {
        this.definition = definition;
    }

    public void setContext(Map<String, Object> ctx) {
        this.ctx = ctx;
    }

    public void addContext(String key, Object value) {
        ctx.put(key, value);
    }

    public Object cell(String cellExtId) {
        Object object = ctx.get(cellExtId);

        if (object == null) {
            throw new IllegalArgumentException(format("Cannot find cell: '%s' in context", cellExtId));
        } else {
            return object;
        }
    }

    public Map<String, Object> getContext() {
        return ctx;
    }


    public boolean anyCellsProvided(String... cellExtIds) {
        checkAllCellsExist(cellExtIds);

        return Stream.of(cellExtIds)
                .map(ctx::get)
                .filter(Objects::nonNull)
                .filter(d -> d instanceof CellVariable)
                .map(d -> (CellVariable) d)
                .filter(d -> StringUtilities.isEmpty(d.errorValue())) // any cells remove cells with error!
                .anyMatch(d -> true);
    }

    public boolean allCellsProvided(String... cellExtIds) {
        checkAllCellsExist(cellExtIds);

        return Stream.of(cellExtIds)
                .map(ctx::get)
                .allMatch(c -> Objects.nonNull(c) && !hasErrors(c));
    }

    public double ratioProvided(String... cellExtIds) {

        checkAllCellsExist(cellExtIds);

        long foundColumns = Stream.of(cellExtIds)
                .map(ctx::get)
                .filter(c -> Objects.nonNull(c) && !hasErrors(c))
                .count();

        int totalColumns = cellExtIds.length;

        return ((double) foundColumns) / totalColumns;
    }

    private boolean hasErrors(Object c) {
        if (c instanceof ReportGridCell) {
            ReportGridCell cv = (ReportGridCell) c;
            return StringUtilities.notEmpty(cv.errorValue());
        } else {
            return true;
        }
    }

    public CellResult mkResult(String value, String optionText, String optionCode) {
        return CellResult.mkResult(value, optionText, optionCode);
    }

    public CellResult mkResult(String value) {
        return CellResult.mkResult(value, value, sanitizeString(value));
    }


    private void checkAllCellsExist(String... requiredCellExtIds) {
        checkAllCellsExist(asSet(requiredCellExtIds));
    }

    private void checkAllCellsExist(Set<String> requiredCellExtIds) {
        Set<String> availableCellExtIds = union(
                map(definition.columnDefinitions(), ReportGridColumnCalculator::colToExtId),
                map(definition.calculatedColumnDefinitions(), ReportGridColumnCalculator::colToExtId));

        Checks.checkTrue(availableCellExtIds.containsAll(
                        requiredCellExtIds),
                "Not all cells external ids found in grid");
    }

}
