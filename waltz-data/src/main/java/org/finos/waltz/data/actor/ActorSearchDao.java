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

package org.finos.waltz.data.actor;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.SearchUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.actor.Actor;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.common.PredicateUtilities.all;
import static org.finos.waltz.schema.Tables.ACTOR;
import static org.finos.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;


@Repository
public class ActorSearchDao implements SearchDao<Actor> {

    private final ActorDao actorDao;
    private final DSLContext dsl;


    @Autowired
    public ActorSearchDao(DSLContext dsl, ActorDao actorDao) {
        checkNotNull(actorDao, "actorDao cannot be null");
        checkNotNull(dsl, "dsl cannot be null");
        this.actorDao = actorDao;
        this.dsl = dsl;
    }


    /**
     * Searches across name and description
     * @param options - contains search term and limits
     * @return matches (if any)
     */
    @Override
    public List<Actor> search(EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery().toLowerCase());

        return concat(
                doBasicSearch(options, terms),
                doAliasSearch(options, terms));
    }


    private List<Actor> doAliasSearch(EntitySearchOptions options,
                                      List<String> terms) {
        Condition aliasCondition = ENTITY_ALIAS.KIND.eq(EntityKind.ACTOR.name())
                .and(JooqUtilities.mkBasicTermSearch(ENTITY_ALIAS.ALIAS, terms));

        return dsl
                .selectDistinct(ACTOR.fields())
                .from(ACTOR)
                .innerJoin(ENTITY_ALIAS)
                .on(ENTITY_ALIAS.ID.eq(ACTOR.ID)
                        .and(ENTITY_ALIAS.KIND.eq(EntityKind.ACTOR.name())))
                .where(aliasCondition)
                .orderBy(ACTOR.NAME)
                .limit(options.limit())
                .fetch(ActorDao.TO_DOMAIN_MAPPER);
    }


    private List<Actor> doBasicSearch(EntitySearchOptions options,
                                  List<String> terms) {
        return actorDao
                .findAll()
                .stream()
                .filter(actor -> {
                    String s = (actor.name() + " " + actor.description() + " " + actor.externalId()).toLowerCase();
                    return all(
                            terms,
                            s::contains);
                })
                .limit(options.limit())
                .collect(toList());
    }
}
