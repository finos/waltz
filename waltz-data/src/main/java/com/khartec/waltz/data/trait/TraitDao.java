/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.trait;

import com.khartec.waltz.model.trait.ImmutableTrait;
import com.khartec.waltz.model.trait.Trait;
import com.khartec.waltz.schema.tables.records.TraitRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.tables.Trait.TRAIT;

@Repository
public class TraitDao {

    private final DSLContext dsl;

    private static final RecordMapper<? super Record, Trait> TRAIT_MAPPER = r -> {
        TraitRecord record = r.into(TRAIT);
        return ImmutableTrait.builder()
                .id(record.getId())
                .description(mkSafe(record.getDescription()))
                .icon(record.getIcon())
                .name(record.getName())
                .applicationDeclarable(record.getApplicationDeclarable())
                .build();
    };


    @Autowired
    public TraitDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Trait> findAll() {
        return queryForList(DSL.trueCondition());
    }


    public Trait getById(long id) {
        return dsl.select(TRAIT.fields())
                .from(TRAIT)
                .where(TRAIT.ID.eq(id))
                .fetchOne(TRAIT_MAPPER);
    }


    public List<Trait> findByIds(List<Long> ids) {
        return queryForList(TRAIT.ID.in(ids));

    }


    public List<Trait> findApplicationDeclarableTraits() {
        return queryForList(TRAIT.APPLICATION_DECLARABLE.eq(true));
    }


    // -- HELPERS -----

    private List<Trait> queryForList(Condition condition) {
        return dsl.select(TRAIT.fields())
                .from(TRAIT)
                .where(condition)
                .orderBy(TRAIT.NAME.asc())
                .fetch(TRAIT_MAPPER);
    }



}
