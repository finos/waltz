package com.khartec.waltz.data;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.EndOfLifeStatus;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.*;
import static org.jooq.impl.DSL.currentDate;
import static org.jooq.impl.DSL.inline;

public class JooqUtilities {

    public static final ExecutorService DB_EXECUTOR_POOL =
            Executors.newFixedThreadPool(
                10, // TODO: ensure this matches the db conn pool size
                (runnable) -> {
                    Thread t = new Thread(runnable, "DB Executor");
                    t.setDaemon(true);
                    return t;
                });


    public static final Field<Integer> TALLY_COUNT_FIELD = DSL.field("count", Integer.class);


    /**
     * Expects result set like: { Id, Count }
     */
    public static final RecordMapper<Record2<String,Integer>, Tally<String>> TO_STRING_TALLY = r ->
            ImmutableTally.<String>builder()
                    .count(r.value2())
                    .id(r.value1())
                    .build();


    public static final RecordMapper<Record2<Long,Integer>, Tally<Long>> TO_LONG_TALLY = r ->
            ImmutableTally.<Long>builder()
                    .count(r.value2())
                    .id(r.value1())
                    .build();


    /**
     * <ul>
     *     <li>value1 : id</li>
     *     <li>value2 : name</li>
     *     <li>value3 : kind</li>
     * </ul>
     */
    public static final RecordMapper<? super Record3<Long, String, String>, EntityReference> TO_ENTITY_REFERENCE = r ->
        ImmutableEntityReference.builder()
                .id(r.value1())
                .name(r.value2())
                .kind(EntityKind.valueOf(r.value3()))
                .build();


    public static List<Tally<String>> calculateStringTallies(
            DSLContext dsl,
            Table table,
            Field<String> fieldToTally,
            Condition recordsInScopeCondition) {

        Select<Record2<String, Integer>> tallyQuery = makeTallyQuery(
                dsl,
                table,
                fieldToTally,
                recordsInScopeCondition);

        return tallyQuery
                .fetch(TO_STRING_TALLY);
    }


    public static List<Tally<String>> calculateStringTallies(
            Result<? extends Record> records,
            Field<String> fieldToTally) {

        return records.stream()
                .collect(groupingBy(r -> r.getValue(fieldToTally), counting()))
                .entrySet()
                .stream()
                .map(e -> ImmutableTally.<String>builder()
                        .id(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(toList());
    }


    public static List<Tally<Long>> calculateLongTallies(
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

        public static SQL mkContains(Collection<String> terms) {
            checkNotNull(terms, "terms cannot be null");
            return mkContains(terms.toArray(new String[0]));
        }

        public static SQL mkContains(String... terms) {
            StringJoiner joiner = new StringJoiner(" AND ", "CONTAINS(*, '", "')");
            Stream.of(terms)
                    .filter(StringUtilities::notEmpty)
                    .map(t -> wrapSpecialInQuotes(t))
                    .forEach(joiner::add);
            return DSL.sql(joiner.toString());
        }

        private static String wrapSpecialInQuotes(String t) {
            return t.contains("&") ? "\"" + t + "\"" : t;
        }
    }


    public static <T> SelectHavingStep<Record2<T, Integer>> makeTallyQuery(
            DSLContext dsl,
            Table table,
            Field<T> fieldToTally,
            Condition recordsInScopeCondition) {
        return dsl.select(
                fieldToTally,
                DSL.count(fieldToTally).as(TALLY_COUNT_FIELD))
                .from(table)
                .where(dsl.renderInlined(recordsInScopeCondition))
                .groupBy(fieldToTally);
    }


    public static Field<String> mkEndOfLifeStatusDerivedField(Field<Date> endOfLifeDateField) {

        return DSL.when(endOfLifeDateField.lt(currentDate()), inline(EndOfLifeStatus.END_OF_LIFE.name()))
                .otherwise(inline(EndOfLifeStatus.NOT_END_OF_LIFE.name()));
    }

}
