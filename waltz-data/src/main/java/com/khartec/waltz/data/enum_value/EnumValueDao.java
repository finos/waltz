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

package com.khartec.waltz.data.enum_value;


import com.khartec.waltz.model.EnumValue;
import com.khartec.waltz.model.ImmutableEnumValue;
import com.khartec.waltz.model.enum_value.EnumValueKind;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.records.EnumValueRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EnumValue.ENUM_VALUE;

@Repository
public class EnumValueDao {


    private static final RecordMapper<? super EnumValueRecord, EnumValue> TO_DOMAIN_MAPPER = r ->
            ImmutableEnumValue
                .builder()
                .type(r.getType())
                .key(r.getKey())
                .name(r.getDisplayName())
                .description(r.getDescription())
                .icon(r.getIconName())
                .iconColor(r.getIconColor())
                .position(r.getPosition())
                .build();


    public static Condition mkExistsCondition(EnumValueKind kind, String key) {
        return DSL.exists(
                DSL.selectFrom(Tables.ENUM_VALUE)
                        .where(Tables.ENUM_VALUE.TYPE.eq(kind.dbValue()))
                        .and(Tables.ENUM_VALUE.KEY.eq(key)));
    }


    private final DSLContext dsl;


    @Autowired
    public EnumValueDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EnumValue> findAll() {
        return dsl.selectFrom(ENUM_VALUE)
                .fetch()
                .map(TO_DOMAIN_MAPPER);
    }

}
