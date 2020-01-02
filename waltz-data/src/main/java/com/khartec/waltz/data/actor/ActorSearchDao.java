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

package com.khartec.waltz.data.actor;

import com.khartec.waltz.data.SearchUtilities;
import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.PredicateUtilities.all;
import static java.util.stream.Collectors.toList;


@Repository
public class ActorSearchDao {

    private final DSLContext dsl;
    private final ActorDao actorDao;


    @Autowired
    public ActorSearchDao(DSLContext dsl, ActorDao actorDao) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(actorDao, "actorDao cannot be null");

        this.dsl = dsl;
        this.actorDao = actorDao;
    }


    public List<Actor> search(EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery().toLowerCase());
        return actorDao.findAll()
                .stream()
                .filter(actor -> {
                    String s = (actor.name() + " " + actor.description()).toLowerCase();
                    return all(
                            terms,
                            t -> s.indexOf(t) > -1);
                })
                .limit(options.limit())
                .collect(toList());
    }
}
