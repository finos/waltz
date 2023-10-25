package org.finos.waltz.service.report_grid;

import org.finos.waltz.common.Checks;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.report_grid.ReportGridCell;
import org.finos.waltz.model.report_grid.ReportGridDefinition;
import sun.plugin.dom.exception.InvalidStateException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.finos.waltz.common.ArrayUtilities.isEmpty;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.common.StringUtilities.lower;
import static org.finos.waltz.common.StringUtilities.notEmpty;
import static org.finos.waltz.service.report_grid.ReportGridUtilities.mkOptionCode;

public class ReportGridEvaluatorNamespace {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


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
        return ctx.get(cellExtId);
    }


    public String coalesceCells(String... cellExtIds) {
        checkAllCellsExist(cellExtIds);
        return Stream
                .of(cellExtIds)
                .map(ctx::get)
                .filter(Objects::nonNull)
                .map(this::cellToStr) //  e.g. coalesce('ONBOARD', 'SCOPE', 'PAAS')
                .findFirst()
                .orElse(null);
    }


    public Map<String, Object> getContext() {
        return ctx;
    }


    public boolean anyCellsProvided(String... cellExtIds) {
        checkAllCellsExist(cellExtIds);

        return Stream
                .of(cellExtIds)
                .map(ctx::get)
                .filter(Objects::nonNull)
                .map(d -> (ReportGridCell) d)
                .filter(d -> StringUtilities.isEmpty(d.errorValue())) // any cells remove cells with error!
                .anyMatch(d -> true);
    }


    public boolean allCellsProvided(String... cellExtIds) {
        checkAllCellsExist(cellExtIds);

        return Stream
                .of(cellExtIds)
                .map(ctx::get)
                .allMatch(c -> Objects.nonNull(c) && !hasErrors(c));
    }


    public boolean hasLifecyclePhase(String... lifecyclePhases) {
        String lifecyclePhase = (String) ctx.get("subjectLifecyclePhase");
        return Stream.of(lifecyclePhases)
                .anyMatch(s -> Objects.nonNull(s) && s.equalsIgnoreCase(lower(lifecyclePhase)));
    }


    public boolean hasExternalId(String... externalIds) {
        String extId = (String) ctx.get("subjectExternalId");
        return Stream.of(externalIds)
                .anyMatch(s -> Objects.nonNull(s) && s.equalsIgnoreCase(lower(extId)));
    }

    public boolean hasName(String... names) {
        String name = (String) ctx.get("subjectName");
        return Stream.of(names)
                .anyMatch(s -> Objects.nonNull(s) && s.equalsIgnoreCase(lower(name)));
    }


    public boolean hasId(Long... ids) {
        Long id = (Long) ctx.get("subjectId");
        return Stream.of(ids)
                .anyMatch(s -> Objects.nonNull(s) && s.equals(id));
    }


    public BigDecimal ratioProvided(String... cellExtIds) {

        BigDecimal ratio = calcRatio(cellExtIds);
        return ratio.equals(BigDecimal.ZERO)
                ? null
                : ratio.setScale(2, RoundingMode.HALF_UP);
    }


    public BigDecimal percentageProvided(String... cellExtIds) {
        BigDecimal ratio = calcRatio(cellExtIds);
        return ratio.equals(BigDecimal.ZERO)
                ? null
                : ratio.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }


    public boolean isAfterToday(String cellExtId) {
        LocalDate today = DateTimeUtilities.today();
        ReportGridCell reportGridCell = getReportGridCell(cellExtId);
        LocalDate dateVal = getLocalDate(reportGridCell);
        return dateVal.isAfter(today);
    }


    public CellResult isAfter(String cellExtId, String dateStr, String pass, String fail) {
        ReportGridCell reportGridCell = getReportGridCell(cellExtId);
        LocalDate dateVal = getLocalDate(reportGridCell);
        LocalDate comparisonDate = parseLocalDatefromString(dateStr);
        return dateVal.isAfter(comparisonDate) ? mkResult(pass) : mkResult(fail);
    }


    public CellResult isBefore(String cellExtId, String dateStr, String pass, String fail) {
        ReportGridCell reportGridCell = getReportGridCell(cellExtId);
        LocalDate dateVal = getLocalDate(reportGridCell);
        LocalDate comparisonDate = parseLocalDatefromString(dateStr);
        return dateVal.isBefore(comparisonDate) ? mkResult(pass) : mkResult(fail);
    }


    public boolean isBeforeToday(String cellExtId) {
        LocalDate today = DateTimeUtilities.today();
        ReportGridCell reportGridCell = getReportGridCell(cellExtId);
        LocalDate dateVal = getLocalDate(reportGridCell);
        return dateVal.isBefore(today);
    }


    public int compareDateCells(String cellExtIdA, String cellExtIdB) {
        ReportGridCell reportGridCellA = getReportGridCell(cellExtIdA);
        ReportGridCell reportGridCellB = getReportGridCell(cellExtIdB);
        LocalDate dateValA = getLocalDate(reportGridCellA);
        LocalDate dateValB = getLocalDate(reportGridCellB);
        return dateValA.compareTo(dateValB);
    }


    public int compareToToday(String cellExtId) {
        ReportGridCell reportGridCell = getReportGridCell(cellExtId);
        LocalDate dateVal = getLocalDate(reportGridCell);
        return dateVal.compareTo(DateTimeUtilities.today());
    }


    public int compareToDate(String cellExtId, String dateStr) {
        ReportGridCell reportGridCell = getReportGridCell(cellExtId);
        LocalDate dateVal = getLocalDate(reportGridCell);
        LocalDate comparisonDate = parseLocalDatefromString(dateStr);
        return dateVal.compareTo(comparisonDate);
    }


    public CellResult compareToDate(String cellExtId, String dateStr, String before, String equal, String after) {
        int comparison = compareToDate(cellExtId, dateStr);
        return comparison == 0
                ? mkResult(equal)
                : comparison < 0
                    ? mkResult(before)
                    : mkResult(after);
    }


    public boolean betweenDates(String cellExtId, String dateStrA, String dateStrB) {
        ReportGridCell reportGridCell = getReportGridCell(cellExtId);
        LocalDate dateVal = getLocalDate(reportGridCell);
        LocalDate dateA = parseLocalDatefromString(dateStrA);
        LocalDate dateB = parseLocalDatefromString(dateStrB);

        if (dateVal.isAfter(dateA) && dateVal.isBefore(dateB)) {
            return true;
        } else if (dateVal.isBefore(dateA) && dateVal.isAfter(dateB)) {
            return true;
        } else {
            return false;
        }
    }


    public CellResult betweenDates(String cellExtId, String dateStrA, String dateStrB, String pass, String fail) {
        return betweenDates(cellExtId, dateStrA, dateStrB)
                ? mkResult(pass)
                : mkResult(fail);
    }


    private ReportGridCell getReportGridCell(String cellExtId) {
        Object cellValue = cell(cellExtId);
        if (cellValue instanceof ReportGridCell) {
            return (ReportGridCell) cellValue;
        } else {
            throw new InvalidStateException("Cannot parse report grid cell from extId: " + cellExtId);
        }
    }


    private LocalDate getLocalDate(ReportGridCell cell) {
        LocalDateTime dateTime = cell.dateTimeValue();
        if (dateTime != null){
            return dateTime.toLocalDate();
        } else {
            String textVal = cell.textValue();
            return parseLocalDatefromString(textVal);
        }
    }


    public static LocalDate parseLocalDatefromString(String dateTimeString) {
        try {
            return LocalDate.parse(dateTimeString, DATE_FORMATTER);
        } catch (DateTimeParseException dtpe) {
            throw new IllegalArgumentException(format("Could not parse date from report grid cell value: %s", dateTimeString), dtpe);
        }
    }


    public CellResult mkResult(String value, String optionText, String optionCode) {
        return CellResult.mkResult(value, optionText, optionCode);
    }


    public CellResult mkResult(String value) {
        return value == null
                ? null
                : CellResult.mkResult(value, value, mkOptionCode(value));
    }


    /**
     * e.g.
     * <pre>
     * numToOutcome(
     *   cell('CTB').numberValue(),
     *   [ 0, "Zip",
     *     100000, "smallish",
     *     100000000, "big" ])
     * </pre>
     * @param num
     * @param outcomes
     * @return
     */
    public String numToOutcome(Byte num, Object[] outcomes) {
        if (num == null) return null;
        return numToOutcome(num.doubleValue(), outcomes);
    }



    public String numToOutcome(Number num, Object[] outcomes) {
        if (num == null) return null;
        if (outcomes.length % 2 != 0) {
            throw new IllegalStateException("Outcomes should be [boundary, outcome, ....], therefore must be an even number of array entries.  The boundary values should be increasing");
        }

        double val = num.doubleValue();

        for (int i = 0; i < outcomes.length; i += 2) {
            double bound = Double.parseDouble(outcomes[i].toString());
            String outcome = (String) outcomes[i + 1];

            if (val <= bound) {
                return outcome;
            }
        }
        return null;
    }


    // --- HELPERS ------------------

    private BigDecimal calcRatio(String[] cellExtIds) {

        if (isEmpty(cellExtIds)) {
            return BigDecimal.ZERO;
        }

        checkAllCellsExist(cellExtIds);

        long foundColumns = Stream
                .of(cellExtIds)
                .map(ctx::get)
                .filter(c -> Objects.nonNull(c) && !hasErrors(c))
                .count();

        if (foundColumns == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalColumns = BigDecimal.valueOf(cellExtIds.length);

        return BigDecimal
                .valueOf(foundColumns)
                .divide(totalColumns, 4, RoundingMode.HALF_UP);
    }


    private boolean hasErrors(Object c) {
        if (c instanceof ReportGridCell) {
            ReportGridCell cv = (ReportGridCell) c;
            return notEmpty(cv.errorValue());
        } else {
            return true;
        }
    }


    private void checkAllCellsExist(String... requiredCellExtIds) {
        checkAllCellsExist(asSet(requiredCellExtIds));
    }


    private void checkAllCellsExist(Set<String> requiredCellExtIds) {
        Set<String> availableCellExtIds = union(
                map(definition.fixedColumnDefinitions(), ReportGridColumnCalculator::colToExtId),
                map(definition.derivedColumnDefinitions(), ReportGridColumnCalculator::colToExtId));

        Checks.checkTrue(availableCellExtIds.containsAll(
                        requiredCellExtIds),
                "Not all cells external ids found in grid");
    }


    private String cellToStr(Object c) {
        if (c instanceof CellVariable) {
            CellVariable cell = (CellVariable) c;
            return cell.cellName();
        }
        if (c instanceof ReportGridCell) {
            ReportGridCell cell = (ReportGridCell) c;
            if (notEmpty(cell.textValue())) {
                return cell.textValue();
            }
            if (cell.numberValue() != null) {
                return cell.numberValue().toString();
            }
            if (cell.dateTimeValue() != null) {
                return DATE_FORMATTER.format(cell.dateTimeValue());
            }
        }
        return "";
    }

}
