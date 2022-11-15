package org.finos.waltz.service.report_grid;

import org.finos.waltz.common.Checks;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.report_grid.ReportGridCell;
import org.finos.waltz.model.report_grid.ReportGridDefinition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.finos.waltz.common.ArrayUtilities.isEmpty;
import static org.finos.waltz.common.SetUtilities.*;
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


    public CellResult mkResult(String value, String optionText, String optionCode) {
        return CellResult.mkResult(value, optionText, optionCode);
    }


    public CellResult mkResult(String value) {
        return CellResult.mkResult(value, value, mkOptionCode(value));
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
            return StringUtilities.notEmpty(cv.errorValue());
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
            return cell.rating().name();
        }
        if (c instanceof ReportGridCell) {
            ReportGridCell cell = (ReportGridCell) c;
            if (StringUtilities.notEmpty(cell.textValue())) {
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
