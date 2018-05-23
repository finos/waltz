/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
                .needsPlannedDate(r.getNeedsPlannedDate())
                .userSelectable(r.getUserSelectable())
                .color(r.getColor())
                .position(r.getPosition())
                .description(r.getDescription())
                .build();


    private static final RecordMapper<RatingSchemeRecord, RatingScheme> TO_SCHEME_MAPPER = r ->
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


    private List<RagName> fetchItems(Condition itemCondition) {
        return dsl
                .selectFrom(RATING_SCHEME_ITEM)
                .where(itemCondition)
                .orderBy(RATING_SCHEME_ITEM.POSITION.asc())
                .fetch(TO_ITEM_MAPPER);
    }

}
