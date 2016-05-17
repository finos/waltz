package com.khartec.waltz.data;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.tally.ImmutableLongTally;
import com.khartec.waltz.model.tally.ImmutableStringTally;
import com.khartec.waltz.model.tally.LongTally;
import com.khartec.waltz.model.tally.StringTally;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class JooqUtilities {

    /**
     * Expects result set like: { Id, Count }
     */
    public static final RecordMapper<Record2<String,Integer>, StringTally> TO_STRING_TALLY = r ->
            ImmutableStringTally.builder()
                    .count(r.value2())
                    .id(r.value1())
                    .build();

    public static final RecordMapper<Record2<Long,Integer>, LongTally> TO_LONG_TALLY = r ->
            ImmutableLongTally.builder()
                    .count(r.value2())
                    .id(r.value1())
                    .build();

    public static <R> List<R> queryTableForList(Table table, RecordMapper<? super Record, R> mapper, Condition condition) {
        return DSL.select(table.fields())
                .from(table)
                .where(condition)
                .fetch(mapper);
    }

    public static List<StringTally> calculateStringTallies(
            DSLContext dsl,
            Table table,
            Field<String> fieldToTally,
            Condition recordsInScopeCondition) {
         return makeTallyQuery(
                    dsl,
                    table,
                    fieldToTally,
                    recordsInScopeCondition)
                .fetch(TO_STRING_TALLY);

    }

    public static List<LongTally> calculateLongTallies(
            DSLContext dsl,
            Table table,
            Field<Long> fieldToTally,
            Condition recordsInScopeCondition) {
        return makeTallyQuery(
                dsl,
                table,
                fieldToTally,
                recordsInScopeCondition)
        .fetch(TO_LONG_TALLY);
    }

    public static class MSSQL {

        public static SQL mkContains(String... terms) {
            StringJoiner joiner = new StringJoiner(" AND ", "CONTAINS(*, '", "')");
            Stream.of(terms)
                    .filter(StringUtilities::notEmpty)
                    .forEach(joiner::add);
            return DSL.sql(joiner.toString());
        }
    }

    private static <T> Select<Record2<T, Integer>> makeTallyQuery(
            DSLContext dsl,
            Table table,
            Field<T> fieldToTally,
            Condition recordsInScopeCondition) {
        return dsl.select(
                fieldToTally,
                DSL.count(fieldToTally))
                .from(table)
                .where(recordsInScopeCondition.toString())
                .groupBy(fieldToTally);

    }

}
