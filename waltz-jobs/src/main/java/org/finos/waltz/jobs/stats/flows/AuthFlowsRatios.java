package org.finos.waltz.jobs.stats.flows;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.DataType;
import org.finos.waltz.schema.tables.EnumValue;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.LogicalFlowDecorator;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.AggregateFunction;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.lambda.Seq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.finos.waltz.common.CollectionUtilities.sumInts;
import static org.finos.waltz.common.StringUtilities.join;
import static org.jooq.lambda.Seq.seq;
import static org.jooq.lambda.tuple.Tuple.tuple;


/**
 * The purpose of this stat calculator is to emit
 * ratios of the usage of auth sources by data type.
 *
 * Two stats should be emitted, one for the exact mapping
 * and one for the roll up.
 */
public class AuthFlowsRatios {

    private static final Logger LOG = LoggerFactory.getLogger(AuthFlowsRatios.class);

    private final DSLContext dsl;


    private final DataType dt = Tables.DATA_TYPE.as("dt");
    private final EnumValue ev = Tables.ENUM_VALUE.as("ev");
    private final LogicalFlow lf = Tables.LOGICAL_FLOW.as("lf");
    private final LogicalFlowDecorator lfd = Tables.LOGICAL_FLOW_DECORATOR.as("lfd");


    private final Condition dtToLfdJoinCond = dt.ID.eq(lfd.DECORATOR_ENTITY_ID)
            .and(lfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()));

    private final Condition lfdToLfJoinCond = lf.ID.eq(lfd.LOGICAL_FLOW_ID);

    private final Condition lfNotRemovedCond = lf.IS_REMOVED.isFalse()
            .and(lf.ENTITY_LIFECYCLE_STATUS.notEqual(EntityLifecycleStatus.REMOVED.name()));

    private void go() {
        LOG.info("started");

        AggregateFunction<Integer> count = DSL.count();
        Result<Record4<String, String, String, Integer>> records = dsl
                .select(
                        dt.CODE,
                        dt.NAME,
                        lfd.RATING,
                        count)
                .from(dt)
                .innerJoin(lfd).on(dtToLfdJoinCond)
                .innerJoin(lf).on(lfdToLfJoinCond)
                .where(lfNotRemovedCond)
                .groupBy(
                        dt.CODE,
                        dt.NAME,
                        lfd.RATING)
                .fetch();

        String body = seq(records)
                .grouped(r -> tuple(
                        r.get(dt.CODE),
                        r.get(dt.NAME)))
                .map(g -> {
                    Map<String, Integer> countsByRating = g.v2.toMap(
                            r -> r.get(lfd.RATING),
                            r -> r.get(count));
                    int primary = countsByRating.getOrDefault("PRIMARY", 0);
                    int secondary = countsByRating.getOrDefault("SECONDARY", 0);
                    int discouraged = countsByRating.getOrDefault("DISCOURAGED", 0);
                    int noOpinion = countsByRating.getOrDefault("NO_OPINION", 0);
                    long total = sumInts(countsByRating.values());

                    return tuple(
                            g.v1.v2,    // dtName
                            g.v1.v1,    // dtCode
                            total,
                            primary,
                            secondary,
                            discouraged,
                            noOpinion,
                            primary > 0
                                    ? (float) total / primary
                                    : 0,
                            primary + secondary > 0
                                    ? (float) total / (primary + secondary)
                                    : 0);
                })
                .map(t -> join(
                        t.toList(),
                        "\t"))
                .collect(joining("\n"));

        System.out.printf(
                "%s\n%s\n",
                mkHeader(),
                body);

        LOG.info("ended");
    }


    private String mkHeader() {
        Map<String, String> ratingCodeToDisplayName = dsl.select(ev.KEY, ev.DISPLAY_NAME)
                .from(ev)
                .where(ev.TYPE.eq("AuthoritativenessRating"))
                .fetchMap(ev.KEY, ev.DISPLAY_NAME);

        String primary = ratingCodeToDisplayName.getOrDefault("PRIMARY", "Primary");
        String secondary = ratingCodeToDisplayName.getOrDefault("SECONDARY", "Secondary");
        String discouraged = ratingCodeToDisplayName.getOrDefault("DISCOURAGED", "Discouraged");
        String noOpinion = ratingCodeToDisplayName.getOrDefault("NO_OPINION", "No Opinion");
        return Seq.of(
                "Data Type",
                "Code",
                "Total",
                primary,
                secondary,
                discouraged,
                noOpinion,
                String.format("Total / %s", primary),
                String.format("Total / (%s + %s)", primary, secondary))
            .collect(joining("\t"));
    }


    public AuthFlowsRatios(DSLContext dsl) {
        this.dsl = dsl;
    }


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        new AuthFlowsRatios(dsl).go();
    }
}
