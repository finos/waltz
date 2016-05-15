package com.khartec.waltz.data.complexity;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.tally.ImmutableLongTally;
import com.khartec.waltz.model.tally.ImmutableStringTally;
import com.khartec.waltz.model.tally.LongTally;
import com.khartec.waltz.model.tally.StringTally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;

@Repository
public class ServerComplexityDao {

    private static final Field<Integer> SERVER_COUNT_FIELD = DSL.field(DSL.count().as("server_count"));

    private final DSLContext dsl;


    @Autowired
    public ServerComplexityDao(DSLContext dsl) {
        this.dsl = dsl;
    }



    public List<StringTally> findCountsByAssetCodes(String... assetCodes) {
        return findCountsByAssetCodes(SERVER_INFORMATION.ASSET_CODE.in(assetCodes));
    }


    public int findBaseline() {
        return findBaseline(DSL.trueCondition());
    }


    public List<StringTally> findCounts() {
        return findCountsByAssetCodes(DSL.trueCondition());
    }


    public List<LongTally> findCountsByAppIdSelector(Select<Record1<Long>> idSelector) {
        Checks.checkNotNull(idSelector, "idSelector cannot be null");

        return dsl.select(APPLICATION.ID, SERVER_COUNT_FIELD)
                .from(SERVER_INFORMATION)
                .innerJoin(APPLICATION)
                .on(SERVER_INFORMATION.ASSET_CODE.eq(APPLICATION.ASSET_CODE))
                .where(APPLICATION.ID.in(idSelector))
                .groupBy(APPLICATION.ID)
                .fetch(r -> ImmutableLongTally.builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());
    }


    private List<StringTally> findCountsByAssetCodes(Condition condition) {
        Checks.checkNotNull(condition, "Condition must be given, use DSL.trueCondition() for 'none'");

        return dsl.select(SERVER_INFORMATION.ASSET_CODE, SERVER_COUNT_FIELD)
                .from(SERVER_INFORMATION)
                .where(condition)
                .groupBy(SERVER_INFORMATION.ASSET_CODE)
                .orderBy(SERVER_COUNT_FIELD.desc())
                .fetch(r -> ImmutableStringTally.builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());

    }


    private int findBaseline(Condition condition) {
        return dsl.select(DSL.max(SERVER_COUNT_FIELD))
                .from(DSL.select(SERVER_INFORMATION.ASSET_CODE, SERVER_COUNT_FIELD)
                        .from(SERVER_INFORMATION)
                        .where(condition)
                        .groupBy(SERVER_INFORMATION.ASSET_CODE))
                .fetchOne(r -> r.value1());
    }

}
