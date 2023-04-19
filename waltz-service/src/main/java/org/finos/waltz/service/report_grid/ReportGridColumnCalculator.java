package org.finos.waltz.service.report_grid;

import org.apache.commons.jexl3.*;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.either.Either;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.*;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.common.StringUtilities.notEmpty;
import static org.finos.waltz.model.report_grid.CellOption.mkCellOption;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ReportGridColumnCalculator {


    public static Set<ReportGridCell> calculate(ReportGridInstance instance,
                                                ReportGridDefinition definition) {
        ReportGridEvaluatorNamespace ns = new ReportGridEvaluatorNamespace(definition);
        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder.namespaces(newHashMap(null, ns)).cache(512).create();

        Map<Long, Collection<ReportGridCell>> rowBySubject = groupBy(
                instance.cellData(),
                ReportGridCell::subjectId);

        Map<Long, RatingSchemeItem> ratingSchemeItemsById = indexById(instance.ratingSchemeItems());

        Set<CompiledCalculatedColumn> derivedColumns = map(
                definition.derivedColumnDefinitions(),
                d -> {
                    Either<String, JexlScript> expr = compile(
                            jexl,
                            d.derivationScript());

                    return ImmutableCompiledCalculatedColumn
                            .builder()
                            .column(d)
                            .expression(expr)
                            .build();
                });

        return instance
                .subjects()
                .stream()
                .map(s -> {
                    long subjectId = s.entityReference().id();
                    Map<String, Object> ctx = initialiseContext(
                            definition.fixedColumnDefinitions(),
                            ratingSchemeItemsById,
                            s,
                            lookupRow(rowBySubject, subjectId));
                    ns.setContext(ctx);
                    return s;
                })
                .flatMap(subject ->
                        calcDerivedCols(
                                ns,
                                subject,
                                derivedColumns)
                        .stream())
                .collect(toSet());

    }


    private static Collection<ReportGridCell> lookupRow(Map<Long, Collection<ReportGridCell>> rowBySubject,
                                                        long subjectId) {
        return rowBySubject.getOrDefault(
                subjectId,
                Collections.emptySet());
    }


    private static Set<ReportGridCell> calcDerivedCols(ReportGridEvaluatorNamespace ns,
                                                       ReportSubject subject,
                                                       Set<CompiledCalculatedColumn> colsToCalc) {

        // we need to evaluate derived cols at least once
        AtomicBoolean evaluateRowAgain = new AtomicBoolean(true);
        // collecting the results as we go
        Map<Long, ReportGridCell> results = new HashMap<>();

        Set<CompiledCalculatedColumn> remaining = SetUtilities.fromCollection(colsToCalc);

        Map<ReportGridDerivedColumnDefinition, String> lastErrors = new HashMap<>();

        while (evaluateRowAgain.get()) {
            // assume this time will be the last
            evaluateRowAgain.set(false);

            // iterate over remaining columns attempting execution
            remaining.forEach(ccc -> {
                try {
                    // attempt to evaluate the cell
                    ofNullable(evaluateCalcCol(ccc, subject))
                            .ifPresent(result -> {
                                ReportGridCell existingResult = results.get(ccc.column().gridColumnId());
                                boolean isDifferent = existingResult == null || !existingResult.equals(result);

                                if (isDifferent) {
                                    // If successful record the result
                                    results.put(ccc.column().gridColumnId(), result);

                                    // ...and update the context so dependent expressions can be calculated
                                    ns.addContext(colToExtId(ccc.column()), result);

                                    // Since something has changed we want to run the loop again in case
                                    // ...a dependant expression can now be evaluated.
                                    evaluateRowAgain.set(true);
                                }
                        });

                    // clear out the error map for this column as the last evaluation succeeded (but may have been null)
                    lastErrors.remove(ccc.column());

                } catch (Exception e) {
                    // if we hit a problem we store it for later as it may resolve itself on a subsequent iteration
                    String msg = toMessage(e);
                    lastErrors.put(ccc.column(), msg);
                }
            });

            if (remaining.isEmpty()) {
                // nothing left to do, therefore we can finish on this iteration
                evaluateRowAgain.set(false);
            }
        }

        Set<ReportGridCell> errorResults = remaining
                .stream()
                .map(ccc -> tuple(
                        ccc.column(),
                        lastErrors.get(ccc.column())))
                .filter(d -> notEmpty(d.v2))
                .map(t -> ImmutableReportGridCell
                        .builder()
                        .subjectId(subject.entityReference().id())
                        .errorValue(t.v2)
                        .options(asSet(mkCellOption("EXECUTION_ERROR", "Execution Error")))
                        .columnDefinitionId(t.v1.gridColumnId())
                        .build())
                .collect(toSet());

        return union(results.values(), errorResults);
    }

    private static String toMessage(Exception e) {
        if (e.getCause() != null) {
            return e.getCause().getMessage();
        } else {
            return e.getMessage();
        }
    }


    private static Either<String, JexlScript> compile(JexlEngine jexl, String expression) {
        try {
            JexlScript expr = jexl.createScript(expression);
            return Either.right(expr);
        } catch (JexlException e) {
            return Either.left(e.getMessage());
        }
    }


    private static Map<String, Object> initialiseContext(List<ReportGridFixedColumnDefinition> columnDefinitions,
                                                         Map<Long, RatingSchemeItem> ratingSchemeItemsById,
                                                         ReportSubject subject,
                                                         Collection<ReportGridCell> row) {

        Map<Long, ReportGridCell> cellsInRowByColId = indexBy(row, ReportGridCell::columnDefinitionId);

        Map<String, Object> ctx = indexBy(
                columnDefinitions,
                ReportGridColumnCalculator::colToExtId,
                col -> mkVal(
                        ratingSchemeItemsById,
                        cellsInRowByColId.get(col.gridColumnId())));

        ctx.put("subjectId", subject.entityReference().id());
        ctx.put("subjectName", subject.entityReference().name().orElse(null));
        ctx.put("subjectLifecyclePhase", subject.lifecyclePhase().name());
        ctx.put("subjectExternalId", subject.entityReference().externalId().orElse(null));

        return ctx;
    }


    private static ReportGridCell evaluateCalcCol(CompiledCalculatedColumn compiledCalculatedColumn,
                                                  ReportSubject subject) {

        ReportGridDerivedColumnDefinition cd = compiledCalculatedColumn.column();

        return compiledCalculatedColumn
                .expression()
                .map(
                        compilationError -> ImmutableReportGridCell
                                .builder()
                                .subjectId(subject.entityReference().id())
                                .errorValue(compilationError)
                                .options(asSet(mkCellOption("COMPILE_ERROR", "Compile Error")))
                                .columnDefinitionId(cd.gridColumnId())
                                .build(),
                        expr -> {

                            MapContext mapContext = new MapContext(newHashMap(
                                    "subjectId", subject.entityReference().id(),
                                    "subjectExternalId", subject.entityReference().externalId().orElse(""),
                                    "subjectName", subject.entityReference().name().orElse(""),
                                    "subjectLifecyclePhase", subject.lifecyclePhase().name()));

                            Object result = expr.execute(mapContext);

                            if (result == null) {
                                return null;
                            } else {

                                CellResult cr = (result instanceof CellResult)
                                        ? (CellResult) result
                                        : CellResult.mkResult(result.toString(), "Provided", "PROVIDED");

                                return ImmutableReportGridCell
                                        .builder()
                                        .textValue(cr.value())
                                        .subjectId(subject.entityReference().id())
                                        .columnDefinitionId(cd.gridColumnId())
                                        .options(asSet(mkCellOption(cr.optionCode(), cr.optionText())))
                                        .build();
                            }
                        });
    }


    public static String colToExtId(ReportGridFixedColumnDefinition col) {
        return col
                .externalId()
                .orElseGet(() -> {

                    String base = col.displayName() == null
                            ? col.columnName()
                            : col.displayName();

                    return base
                            .toUpperCase()
                            .replaceAll(" ", "_");
                });
    }


    public static String colToExtId(ReportGridDerivedColumnDefinition col) {
        return col
                .externalId()
                .orElseGet(() -> col
                        .displayName()
                        .toUpperCase()
                        .replaceAll(" ", "_"));
    }


    private static CellVariable mkVal(Map<Long, RatingSchemeItem> ratingSchemeItemsById, ReportGridCell cell) {

        if (cell == null ) {
            return null;
        }

        return ImmutableCellVariable.builder()
                .from(cell)
                .ratings(map(cell.ratingIdValues(), ratingSchemeItemsById::get))
                .build();
    }

}
