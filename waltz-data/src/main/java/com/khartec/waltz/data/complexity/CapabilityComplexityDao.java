package com.khartec.waltz.data.complexity;

import com.khartec.waltz.model.tally.ImmutableLongTally;
import com.khartec.waltz.model.tally.LongTally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;

@Repository
public class CapabilityComplexityDao {

    private static final Field<BigDecimal> SCORE_ALIAS = DSL.field("score", BigDecimal.class);

    private static final Field<BigDecimal> SCORE_FIELD =
            DSL.sum(
                DSL.coalesce(
                    DSL.value(1).div(
                        DSL.nullif(CAPABILITY.LEVEL, 0)), 1));

    private static final RecordMapper<Record2<Long, BigDecimal>, LongTally> toScoreMapper =
            r -> ImmutableLongTally.builder()
                    .count(r.value2().doubleValue())
                    .id(r.value1())
                    .build();


    private final DSLContext dsl;


    @Autowired
    public CapabilityComplexityDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<LongTally> findScores() {
        return mkSelectQueryWhere(DSL.trueCondition())
                .fetch(toScoreMapper);
    }


    public List<LongTally> findScoresForOrgUnitIds(Long... orgUnitIds) {
        return mkSelectQueryIncludingApplicationWhere(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitIds))
                .fetch(toScoreMapper);
    }


    public List<LongTally> findScoresForAppIds(Long... appIds) {
        return mkSelectQueryWhere(APP_CAPABILITY.APPLICATION_ID.in(appIds))
                .fetch(toScoreMapper);
    }


    public LongTally findScoresForAppId(Long appId) {
        return mkSelectQueryWhere(APP_CAPABILITY.APPLICATION_ID.eq(appId))
                .fetchOne(toScoreMapper);
    }



    public Double findBaseline() {
        return dsl.select(DSL.max(SCORE_ALIAS))
                .from(mkSelectQueryWhere(DSL.trueCondition()))
                .fetchOne()
                .value1()
                .doubleValue();
    }


    public Double findBaseLineForOrgUnitIds(Long... orgUnitIds) {
        Condition orgUnitMatches = APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitIds);

        SelectHavingStep<Record2<Long, BigDecimal>> subSelect =
                mkSelectQueryIncludingApplicationWhere(orgUnitMatches);

        return dsl.select(DSL.max(SCORE_ALIAS))
                .from(subSelect)
                .fetchOne()
                .value1()
                .doubleValue();
    }


    // -- HELPER ---

    private SelectHavingStep<Record2<Long, BigDecimal>> mkSelectQueryWhere(Condition conditionStep) {
        return dsl.select(APP_CAPABILITY.APPLICATION_ID, SCORE_FIELD.as(SCORE_ALIAS))
                .from(APP_CAPABILITY)
                .innerJoin(CAPABILITY)
                .on(CAPABILITY.ID.eq(APP_CAPABILITY.CAPABILITY_ID))
                .where(conditionStep)
                .groupBy(APP_CAPABILITY.APPLICATION_ID);
    }


    private SelectHavingStep<Record2<Long, BigDecimal>> mkSelectQueryIncludingApplicationWhere(Condition conditionStep) {
        return dsl.select(APP_CAPABILITY.APPLICATION_ID, SCORE_FIELD.as(SCORE_ALIAS))
                .from(APP_CAPABILITY)
                .innerJoin(CAPABILITY)
                .on(CAPABILITY.ID.eq(APP_CAPABILITY.CAPABILITY_ID))
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(APP_CAPABILITY.APPLICATION_ID))
                .where(conditionStep)
                .groupBy(APP_CAPABILITY.APPLICATION_ID);
    }
}
