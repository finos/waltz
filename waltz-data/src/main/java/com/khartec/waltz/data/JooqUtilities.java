package com.khartec.waltz.data;

import com.khartec.waltz.common.StringUtilities;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class JooqUtilities {

    public static <R> List<R> queryTableForList(Table table, RecordMapper<? super Record, R> mapper, Condition condition) {
        return DSL.select(table.fields())
                .from(table)
                .where(condition)
                .fetch(mapper);
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

}
