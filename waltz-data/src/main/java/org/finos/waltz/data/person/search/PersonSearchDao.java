/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.data.person.search;

import org.finos.waltz.data.DBExecutorPoolInterface;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.person.PersonDao;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.person.Person;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import static org.finos.waltz.common.SetUtilities.orderedUnion;
import static org.finos.waltz.data.JooqUtilities.mkBasicTermSearch;
import static org.finos.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Person.PERSON;

@Repository
public class PersonSearchDao implements SearchDao<Person> {

    private final DSLContext dsl;
    private final DBExecutorPoolInterface dbExecutorPool;


    @Autowired
    public PersonSearchDao(DSLContext dsl, DBExecutorPoolInterface dbExecutorPool) {
        this.dsl = dsl;
        this.dbExecutorPool = dbExecutorPool;
    }


    @Override
    public List<Person> search(EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition displayNameCondition = mkBasicTermSearch(PERSON.DISPLAY_NAME, terms);

        Future<List<Person>> peopleViaEmail = dbExecutorPool.submit(() -> executeWithCondition(options, PERSON.EMAIL.startsWithIgnoreCase(options.searchQuery())));
        Future<List<Person>> peopleViaName = dbExecutorPool.submit(() -> executeWithCondition(options, displayNameCondition));

        return new ArrayList<>(Unchecked.supplier(() ->
                orderedUnion(
                        peopleViaEmail.get(),
                        peopleViaName.get()))
                .get());
    }


    private List<Person> executeWithCondition(EntitySearchOptions options, Condition condition) {
        boolean showRemoved = options
                .entityLifecycleStatuses()
                .contains(EntityLifecycleStatus.REMOVED);

        Condition maybeFilterRemoved = showRemoved
                ? DSL.trueCondition()  // match anything
                : PERSON.IS_REMOVED.isFalse();

        return dsl
                .select(PERSON.fields())
                .from(PERSON)
                .where(condition)
                .and(maybeFilterRemoved)
                .limit(options.limit())
                .fetch(PersonDao.personMapper);
    }
}
