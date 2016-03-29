package com.khartec.waltz.data;

import org.jooq.DSLContext;

import java.util.List;

public interface FullTextSearch<T> {

    List<T> search(DSLContext dsl, String terms);
}
