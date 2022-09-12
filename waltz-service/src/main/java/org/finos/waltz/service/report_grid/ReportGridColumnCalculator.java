package org.finos.waltz.service.report_grid;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.either.Either;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.*;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.newHashMap;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.StringUtilities.notEmpty;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ReportGridColumnCalculator {


    public static Set<ReportGridCell> calculate(ReportGridInstance instance, ReportGridDefinition definition) {
        ReportGridEvaluatorNamespace ns = new ReportGridEvaluatorNamespace();
        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder.namespaces(newHashMap(null, ns)).create();

        Map<Long, Collection<ReportGridCell>> rowBySubject = groupBy(
                instance.cellData(),
                ReportGridCell::subjectId);

        Map<Long, RatingSchemeItem> ratingSchemeItemsById = indexById(instance.ratingSchemeItems());

        Set<CompiledCalculatedColumn> derivedColumns = map(
                definition.calculatedColumnDefinitions(),
                d -> {
                    Either<String, JexlExpression> valueExpr = compile(
                            jexl,
                            d.valueExpression());

                    Either<String, JexlExpression> perhapsOutcomeExpression = notEmpty(d.outcomeExpression())
                            ? compile(
                                jexl,
                                d.outcomeExpression())
                            : null;

                    return ImmutableCompiledCalculatedColumn
                            .builder()
                            .column(d)
                            .valueExpression(valueExpr)
                            .outcomeExpression(perhapsOutcomeExpression)
                            .build();
                });


        return instance
                .subjects()
                .stream()
                .map(s -> {
                    long subjectId = s.entityReference().id();
                    Collection<ReportGridCell> row = rowBySubject.getOrDefault(subjectId, Collections.emptySet());
                    Map<String, Object> ctx = initialiseContext(
                            definition.columnDefinitions(),
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
        AtomicBoolean runAgain = new AtomicBoolean(true);
        // collecting the results as we go
        Set<ReportGridCell> results = new HashSet<>();
        // this collection tracks successfully calculated columns, so we don't keep re-evaluating
        Set<CompiledCalculatedColumn> colsToRemove = new HashSet<>();

        Set<CompiledCalculatedColumn> remaining = SetUtilities.fromCollection(colsToCalc);

        Map<ReportGridCalculatedColumnDefinition, String> lastErrors = new HashMap<>();

        while (runAgain.get()) {
            // assume this time will be the last
            runAgain.set(false);

            // iterate over remaining columns attempting execution
            remaining.forEach(ccc -> {
                try {
                    // attempt to evaluate the cell
                    ofNullable(evaluateCalcCol(ccc, subjectId))
                        .ifPresent(result -> {
                            // If successful record the result
                            results.add(result);

                            // ...add the column to the list of cols to remove after this iteration
                            //   (we remove late to prevent a concurrent modification exception)
                            colsToRemove.add(ccc);

                            // ...and update the context so dependent expressions can be calculated
                            ns.addContext(colToExtId(ccc.column()), result);

                            // Since something has changed we want to run the loop again in case
                            // ...a dependant expression can now be evaluated.
                            runAgain.set(true);
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
                runAgain.set(false);
            }
        }

        Stream<Tuple2<ReportGridCalculatedColumnDefinition, String>> remainingStream = remaining
                .stream()
                .map(ccc -> tuple(
                        ccc.column(),
                        lastErrors.get(ccc.column())));

        // handle remaining values (errors and blanks)
        remainingStream
                .map(t -> {
                    boolean isError = StringUtilities.notEmpty(t.v2);
                    String optionCode = isError ? "EXECUTION_ERROR" : "NO_VALUE";
                    String optionText = isError ? "Execution Error" : "No Value";
                    String errorValue = isError ? t.v2 : null;

                    return ImmutableReportGridCell
                            .builder()
                            .subjectId(subjectId)
                            .errorValue(errorValue)
                            .optionCode(optionCode)
                            .optionText(optionText)
                            .columnDefinitionId(t.v1.id())
                            .build();
                })
                .forEach(results::add);

        return results;
    }

    private static String toMessage(Exception e) {
        return e.getCause().getMessage();
    }


    private static Either<String, JexlExpression> compile(JexlEngine jexl, String expression) {
        try {
            JexlExpression expr = jexl.createExpression(expression);
            return Either.right(expr);
        } catch (JexlException e) {
            return Either.left(e.getMessage());
        }
    }


    private static Map<String, Object> initialiseContext(List<ReportGridColumnDefinition> columnDefinitions,
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

        ReportGridCalculatedColumnDefinition cd = compiledCalculatedColumn.column();

        Optional<Object> outcome = ofNullable(compiledCalculatedColumn.outcomeExpression())
                .map(outcomeExpr ->
                        outcomeExpr.map(
                            err -> err,
                            expr -> expr.evaluate(null)));


        return compiledCalculatedColumn
                .valueExpression()
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
                        Object result = expr.evaluate(null);
                        return result == null
                                ? null
                                : ImmutableReportGridCell
                                    .builder()
                                    .textValue(result.toString())
                                    .subjectId(subjectId)
                                    .columnDefinitionId(cd.id())
                                    .optionCode(outcome.map(Object::toString).orElse(""))
                                    .optionText(outcome.map(Object::toString).orElse(""))
                                    .build();
                    });


    }


    private static String colToExtId(ReportGridColumnDefinition col) {
        String base = col.displayName() == null
                ? col.columnName()
                : col.displayName();
        return base
                .toUpperCase()
                .replaceAll(" ", "_");
    }


    private static String colToExtId(ReportGridCalculatedColumnDefinition col) {
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
