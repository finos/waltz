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


import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.model.enum_value.EnumValueKind;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.ENUM_VALUE_ALIAS;

@Repository
public class EnumValueAliasDao {

    private final DSLContext dsl;


    @Autowired
    public EnumValueAliasDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Aliases<String> mkAliases(EnumValueKind kind) {
        Aliases<String> aliases = new Aliases<>();
        dsl.select(ENUM_VALUE_ALIAS.ENUM_KEY, ENUM_VALUE_ALIAS.ALIAS)
            .from(ENUM_VALUE_ALIAS)
            .where(ENUM_VALUE_ALIAS.ENUM_TYPE.eq(kind.dbValue()))
            .fetch()
            .forEach(r -> aliases.register(r.value1(), r.value2()));

        return aliases;

    }

}
