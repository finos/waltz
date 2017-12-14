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

package com.khartec.waltz.data.perspective_definition;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.perspective.ImmutablePerspectiveDefinition;
import com.khartec.waltz.model.perspective.PerspectiveDefinition;
import com.khartec.waltz.model.rating.ImmutableRagNames;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.rating.RagNames;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.schema.tables.records.PerspectiveDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.model.rating.RagName.mkRagName;
import static com.khartec.waltz.schema.tables.PerspectiveDefinition.PERSPECTIVE_DEFINITION;

@Repository
public class PerspectiveDefinitionDao {

    private static final RecordMapper<PerspectiveDefinitionRecord, PerspectiveDefinition> TO_DOMAIN_MAPPER = r -> {
        RagNames ragNames = ImmutableRagNames.builder()
                .R(mkRagName(RagRating.R, r.getRatingNameR(), null))
                .A(mkRagName(RagRating.A, r.getRatingNameA(), null))
                .G(mkRagName(RagRating.G, r.getRatingNameG(), null))
                .Z(mkRagName(RagRating.Z, r.getRatingNameZ(), null))
                .X(mkRagName(RagRating.X, r.getRatingNameX(), null))
                .build();

        return ImmutablePerspectiveDefinition.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .categoryX(r.getCategoryX())
                .categoryY(r.getCategoryY())
                .ragNames(ragNames)
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PerspectiveDefinitionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PerspectiveDefinition> findAll() {
        return dsl.selectFrom(PERSPECTIVE_DEFINITION)
                .orderBy(PERSPECTIVE_DEFINITION.NAME.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }

    public boolean create(PerspectiveDefinition perspectiveDefinition) {

        Set<Set<Long>> existing = dsl
                .select(PERSPECTIVE_DEFINITION.CATEGORY_X, PERSPECTIVE_DEFINITION.CATEGORY_Y)
                .from(PERSPECTIVE_DEFINITION)
                .fetch()
                .stream()
                .map(pd -> SetUtilities.fromArray(pd.value1(), pd.value2()))
                .collect(Collectors.toSet());

        Set<Long> proposed = SetUtilities.fromArray(
                perspectiveDefinition.categoryX(),
                perspectiveDefinition.categoryY());

        if (existing.contains(proposed)) {
            return false;
        } else {
            RagNames ragNames = perspectiveDefinition.ragNames();

            PerspectiveDefinitionRecord record = dsl.newRecord(PERSPECTIVE_DEFINITION);
            record.setName(perspectiveDefinition.name());
            record.setCategoryX(perspectiveDefinition.categoryX());
            record.setCategoryY(perspectiveDefinition.categoryY());
            record.setRatingNameR(ragNames.R().name());
            record.setRatingNameA(ragNames.A().name());
            record.setRatingNameG(ragNames.G().name());
            record.setRatingNameZ(ragNames.Z().name());
            record.setRatingNameX(ragNames.X().name());
            record.setDescription(mkSafe(perspectiveDefinition.description()));

            return record.insert() == 1;
        }
    }
}
