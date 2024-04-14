package org.finos.waltz.jobs.example.demodata.loaders;

import org.apache.poi.ss.usermodel.Sheet;
import org.finos.waltz.common.Columns;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.schema.tables.records.AssessmentDefinitionRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeItemRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeRecord;
import org.immutables.value.Value;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple3;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;
import static org.finos.waltz.schema.Tables.ASSESSMENT_DEFINITION;
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

        default String assessmentExtId() {
            return toExtId(assessmentName());
        }

        default String ratingExtId() {
            return toExtId(assessmentName());
        }
    }


    public static void process(DSLContext waltz, Sheet sheet, Timestamp now) {
        Set<AssessmentRow> configRows = StreamSupport
                .stream(sheet.spliterator(), false)
                .skip(1)
                .map(r -> {
                    String ratingName = strVal(r, Columns.C);
                    boolean isUnmapped = "n/a".equals(ratingName);
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
                .collect(toSet());

        Map<Tuple3<String, String, String>, Collection<AssessmentRow>> byDef = MapUtilities
            .groupBy(
                configRows,
                r -> tuple(
                        r.assessmentGroup(),
                        r.assessmentName(),
                        r.assessmentExtId()));

        byDef
            .keySet()
            .stream()
            .map(t -> {
                RatingSchemeRecord r = waltz.newRecord(rs);
                r.setName(t.v2);
                r.setDescription(format("Rating Scheme: %s / %s", t.v1, t.v2));
                r.setExternalId(toExtId(t.v3));
                return r;
            })
            .collect(collectingAndThen(toSet(), waltz::batchInsert))
            .execute();

        Map<String, Long> rsByExtIt = waltz
                .select(rs.ID, rs.EXTERNAL_ID)
                .from(rs)
                .fetchMap(rs.EXTERNAL_ID, rs.ID);

        byDef
            .entrySet()
            .stream()
            .flatMap(kv -> {
                Long rsId = rsByExtIt.get(kv.getKey().v3);
                AtomicInteger ctr = new AtomicInteger(1);
                return kv
                    .getValue()
                    .stream()
                    .map(d -> tuple(rsId, d, ctr.getAndIncrement()));
            })
            .map(t -> {
                RatingSchemeItemRecord r = waltz.newRecord(rsi);
                r.setSchemeId(t.v1);
                r.setName(t.v2.ratingName());
                r.setColor(t.v2.ratingColor());
                r.setCode(t.v3.toString());
                r.setExternalId(t.v2.ratingExtId());
                r.setDescription(t.v2.ratingDescription());
                return r;
            })
            .collect(collectingAndThen(toSet(), waltz::batchInsert))
            .execute();

        byDef
            .keySet()
            .stream()
            .map(t -> {
                AssessmentDefinitionRecord r = waltz.newRecord(ASSESSMENT_DEFINITION);
                r.setDefinitionGroup(t.v1);
                r.setName(t.v2);
                r.setDescription(format("Assessment: %s / %s", t.v1, t.v2));
                r.setEntityKind(EntityKind.APPLICATION.name());
                r.setExternalId(toExtId(t.v2));
                r.setProvenance(PROVENANCE);
                r.setLastUpdatedAt(now);
                r.setLastUpdatedBy(USER_ID);
                r.setRatingSchemeId(rsByExtIt.get(t.v3));
                r.setVisibility(AssessmentVisibility.PRIMARY.name());
                return r;
            })
            .collect(collectingAndThen(toSet(), waltz::batchInsert))
            .execute();


        System.out.println(configRows);

    }
}
