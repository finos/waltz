/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.stream.Collectors.*;
import static org.jooq.impl.DSL.currentDate;
import static org.jooq.impl.DSL.inline;

public class JooqUtilities {

    public static final Field<Integer> TALLY_COUNT_FIELD = DSL.field("count", Integer.class);


    public static EntityReference readRef(Record record, Field<String> kindField, Field<Long> idField) {
        return mkRef(
                EntityKind.valueOf(record.getValue(kindField)),
                record.getValue(idField));
    }


    public static EntityReference readRef(Record record, Field<String> kindField, Field<Long> idField, Field<String> nameField) {
        return mkRef(
                EntityKind.valueOf(record.getValue(kindField)),
                record.getValue(idField),
                record.getValue(nameField));
    }


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


    /**
     * Attempts to read the value of the field (f)
     * from the record (r).  If the value is null
     * then the default (dflt) values is returned.
     * @param r Record
     * @param f Field to read
     * @param dflt  Default value to return if field-value is null
     * @param <T>  Type of the field-value
     * @return
     */
    public static <T> T safeGet(Record r, Field<T> f, T dflt) {
        return Optional
                .ofNullable(r.getValue(f))
                .orElse(dflt);
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

        public static SQL mkContainsPrefix(Collection<String> terms) {
            checkNotNull(terms, "terms cannot be null");

            // this is based on: https://stackoverflow.com/a/4321828
            List<String> wildcardTerms = terms
                    .stream()
                    .map(s -> "\"" + s + "*\"")
                    .map(s -> s.replace('&', ' '))
                    .collect(Collectors.toList());

            return mkContains(wildcardTerms);
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


    public static Condition mkBasicTermSearch(Field<String> field, List<String> terms) {
        Function<String, Condition> mapper = (term) -> field.likeIgnoreCase("%"+term+"%");
        BinaryOperator<Condition> combiner = (a, b) -> a.and(b);
        return terms.stream().collect(Collectors.reducing(DSL.trueCondition(), mapper, combiner));
    }

}
