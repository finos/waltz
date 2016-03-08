package com.khartec.waltz.data;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;

public class JooqUtilities {

    public static <R> List<R> queryTableForList(Table table, RecordMapper<? super Record, R> mapper, Condition condition) {
        return DSL.select(table.fields())
                .from(table)
                .where(condition)
                .fetch(mapper);
    }

}
