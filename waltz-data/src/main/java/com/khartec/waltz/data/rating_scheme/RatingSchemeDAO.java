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

package com.khartec.waltz.data.rating_scheme;

import com.khartec.waltz.model.rating.ImmutableRagName;
import com.khartec.waltz.model.rating.ImmutableRatingScheme;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.rating.RatingScheme;
import com.khartec.waltz.schema.tables.records.RatingSchemeItemRecord;
import com.khartec.waltz.schema.tables.records.RatingSchemeRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.StringUtilities.firstChar;
import static com.khartec.waltz.schema.tables.RatingScheme.RATING_SCHEME;
import static com.khartec.waltz.schema.tables.RatingSchemeItem.RATING_SCHEME_ITEM;

@Repository
public class RatingSchemeDAO {

    private static final RecordMapper<RatingSchemeItemRecord, RagName> TO_ITEM_MAPPER = r ->
        ImmutableRagName.builder()
                .id(r.getId())
                .ratingSchemeId(r.getSchemeId())
                .name(r.getName())
                .rating(firstChar(r.getCode(), 'X'))
                .userSelectable(r.getUserSelectable())
                .color(r.getColor())
                .position(r.getPosition())
                .description(r.getDescription())
                .build();


    public static final RecordMapper<RatingSchemeRecord, RatingScheme> TO_SCHEME_MAPPER = r ->
        ImmutableRatingScheme.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();


    private final DSLContext dsl;


    @Autowired
    public RatingSchemeDAO(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Collection<RatingScheme> findAll() {
        Map<Long, Collection<RagName>> itemsByScheme = groupBy(
                d -> d.ratingSchemeId(),
                fetchItems(DSL.trueCondition()));

        return dsl
                .selectFrom(RATING_SCHEME)
                .fetch(TO_SCHEME_MAPPER)
                .stream()
                .map(s -> ImmutableRatingScheme
                        .copyOf(s)
                        .withRatings(itemsByScheme.getOrDefault(
                                s.id().get(),
                                Collections.emptyList())))
                .collect(Collectors.toList());
    }


    public RatingScheme getById(long id) {
        Condition itemCondition = RATING_SCHEME_ITEM.SCHEME_ID.eq(id);
        List<RagName> items = fetchItems(itemCondition);
        return ImmutableRatingScheme
                .copyOf(dsl
                    .selectFrom(RATING_SCHEME)
                    .where(RATING_SCHEME.ID.eq(id))
                    .fetchOne(TO_SCHEME_MAPPER))
                .withRatings(items);
    }


    public List<RagName> fetchItems(Condition itemCondition) {
        return dsl
                .selectFrom(RATING_SCHEME_ITEM)
                .where(itemCondition)
                .orderBy(RATING_SCHEME_ITEM.POSITION.asc())
                .fetch(TO_ITEM_MAPPER);
    }


    public RagName getRagNameById(long id){
        checkNotNull(id, "id cannot be null");
        return dsl.selectFrom(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.ID.eq(id))
                .fetchOne(TO_ITEM_MAPPER);
    }

}
