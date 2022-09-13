package org.finos.waltz.service.report_grid;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlScript;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.either.Either;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.newHashMap;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.common.StringUtilities.notEmpty;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ReportGridColumnCalculator {


    public static Set<ReportGridCell> calculate(ReportGridInstance instance,
                                                ReportGridDefinition definition) {
        ReportGridEvaluatorNamespace ns = new ReportGridEvaluatorNamespace(definition);
        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder.namespaces(newHashMap(null, ns)).create();

        Map<Long, Collection<ReportGridCell>> rowBySubject = groupBy(
                instance.cellData(),
                ReportGridCell::subjectId);

        Map<Long, RatingSchemeItem> ratingSchemeItemsById = indexById(instance.ratingSchemeItems());

        Set<CompiledCalculatedColumn> derivedColumns = map(
                definition.derivedColumnDefinitions(),
                d -> {
                    Either<String, JexlScript> expr = compile(
                            jexl,
                            d.expression());

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
                    Collection<ReportGridCell> row = rowBySubject.getOrDefault(subjectId, Collections.emptySet());
                    Map<String, Object> ctx = initialiseContext(
                            definition.fixedColumnDefinitions(),
                            ratingSchemeItemsById,
                            subjectId,
                            row);
                    ns.setContext(ctx);
                    return subjectId;
                })
                .flatMap(subject ->
                        calcDerivedCols(
                                ns,
                                subject,
                                derivedColumns)
                        .stream())
                .collect(Collectors.toSet());

    }


    private static Set<ReportGridCell> calcDerivedCols(ReportGridEvaluatorNamespace ns,
                                                       long subjectId,
                                                       Set<CompiledCalculatedColumn> colsToCalc) {

        // we need to evaluate derived cols at least once
        AtomicBoolean evaluateRowAgain = new AtomicBoolean(true);
        // collecting the results as we go
        Map<Long, ReportGridCell> results = new HashMap<>();
        // this collection tracks successfully calculated columns, so we don't keep re-evaluating
        Set<CompiledCalculatedColumn> colsToRemove = new HashSet<>();

        Set<CompiledCalculatedColumn> remaining = SetUtilities.fromCollection(colsToCalc);

        Map<ReportGridDerivedColumnDefinition, String> lastErrors = new HashMap<>();

        while (evaluateRowAgain.get()) {
            // assume this time will be the last
            evaluateRowAgain.set(false);

            // iterate over remaining columns attempting execution
            remaining.forEach(ccc -> {
                try {
                    // attempt to evaluate the cell
                    ofNullable(evaluateCalcCol(ccc, subjectId))
                            .ifPresent(result -> {


                                ReportGridCell existingResult = results.get(ccc.column().id());
                                boolean isDifferent = existingResult == null || !existingResult.equals(result);

                                if (isDifferent) {
                                    // If successful record the result
                                    results.put(ccc.column().id(), result);

                                    // ...add the column to the list of cols to remove after this iteration
                                    //   (we remove late to prevent a concurrent modification exception)
//                            colsToRemove.add(ccc);

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

            // remove all the cols with a result. We don't need to evaluate them again
            remaining.removeAll(colsToRemove);

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
                        .subjectId(subjectId)
                        .errorValue(t.v2)
                        .optionCode("EXECUTION_ERROR")
                        .optionText("Execution Error")
                        .columnDefinitionId(t.v1.id())
                        .build())
                .collect(Collectors.toSet());

        return union(results.values(), errorResults);
    }

    private static String toMessage(Exception e) {
        return e.getCause().getMessage();
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
                                                         Long subjectId,
                                                         Collection<ReportGridCell> row) {

        Map<String, Object> ctx = new HashMap<>();

        Map<Long, ReportGridCell> cellsInRowbyColId = MapUtilities.indexBy(row, ReportGridCell::columnDefinitionId);

        columnDefinitions
                .forEach(col -> {
                    ctx.put(colToExtId(col),
                            mkVal(ratingSchemeItemsById, cellsInRowbyColId.get(col.id())));
                });

        ctx.put("subjectId", subjectId);

        return ctx;
    }


    private static ReportGridCell evaluateCalcCol(CompiledCalculatedColumn compiledCalculatedColumn,
                                                  Long subjectId) {

        ReportGridDerivedColumnDefinition cd = compiledCalculatedColumn.column();

        return compiledCalculatedColumn
                .expression()
                .map(
                        compilationError -> ImmutableReportGridCell
                                .builder()
                                .subjectId(subjectId)
                                .errorValue(compilationError)
                                .optionCode("COMPILE_ERROR")
                                .optionText("Compile Error")
                                .columnDefinitionId(cd.id())
                                .build(),
                        expr -> {
                            Object result = expr.execute(null);

                            if (result == null) {
                                return null;
                            } else {

                                CellResult cr = (result instanceof CellResult)
                                        ? (CellResult) result
                                        : CellResult.mkResult(result.toString(), "Provided", "PROVIDED");

                                return ImmutableReportGridCell
                                        .builder()
                                        .textValue(cr.value())
                                        .subjectId(subjectId)
                                        .columnDefinitionId(cd.id())
                                        .optionCode(cr.optionCode())
                                        .optionText(cr.optionText())
                                        .build();
                            }
                        });
    }


    public static String colToExtId(ReportGridFixedColumnDefinition col) {
        String base = col.displayName() == null
                ? col.columnName()
                : col.displayName();
        return base
                .toUpperCase()
                .replaceAll(" ", "_");
    }


    public static String colToExtId(ReportGridDerivedColumnDefinition col) {
        return col.displayName()
                .toUpperCase()
                .replaceAll(" ", "_");
    }


    private static CellVariable mkVal(Map<Long, RatingSchemeItem> ratingSchemeItemsById, ReportGridCell cell) {

        if (cell == null ) {
            return null;
        }

        return ImmutableCellVariable.builder()
                .from(cell)
                .rating(cell.ratingIdValue() != null
                        ? ratingSchemeItemsById.get(cell.ratingIdValue())
                        : null)
                .build();
    }

}
