package org.finos.waltz.jobs.example.demodata.loaders;

import org.apache.poi.ss.usermodel.Sheet;
import org.finos.waltz.common.Columns;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.finos.waltz.jobs.example.demodata.DemoUtils.numberVal;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.strVal;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AppToAssessmentLoader {

    @Value.Immutable
    interface AssessmentRow {
        String assessmentName();
        String assessmentGroup();
        @Nullable
        String ratingName();
        @Nullable
        String ratingDescription();
        @Nullable
        String ratingColor();
        int weighting();
    }

    public static void process(DSLContext waltz, Sheet sheet, Timestamp now) {
        Set<ImmutableAssessmentRow> configRows = StreamSupport
                .stream(sheet.spliterator(), false)
                .skip(1)
                .map(r -> {
                    String ratingName = strVal(r, Columns.C);
                    boolean isUnmapped = ratingName.equals("n/a");
                    return ImmutableAssessmentRow
                            .builder()
                            .assessmentGroup(strVal(r, Columns.A))
                            .assessmentName(strVal(r, Columns.B))
                            .ratingName(isUnmapped ? null : ratingName)
                            .ratingDescription(isUnmapped ? null : strVal(r, Columns.D))
                            .ratingColor(isUnmapped ? null : strVal(r, Columns.E))
                            .weighting(numberVal(r, Columns.F).intValue())
                            .build();
                })
                .collect(Collectors.toSet());

        Map<Tuple2<String, String>, Collection<ImmutableAssessmentRow>> byDef = MapUtilities.groupBy(
                configRows,
                r -> tuple(r.assessmentGroup(), r.assessmentName()));

        byDef.keySet().stream().map(entry -> )
        byDef.entrySet()
                .stream()
                .flatMap(kv -> {
                    Tuple2<String, String> scheme = kv.getKey();
                    Collection<ImmutableAssessmentRow> ratings = kv.getValue();
                    return Stream.concat(Stream.of(waltz.newRecord()))

                })


        System.out.println(configRows);

    }
}
