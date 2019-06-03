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

package com.khartec.waltz.data.role;

import com.khartec.waltz.model.role.ImmutableRole;
import com.khartec.waltz.model.role.Role;
import com.khartec.waltz.schema.tables.records.RoleRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Role.ROLE;


@Repository
public class RoleDao {

    private final DSLContext dsl;

    @Autowired
    public RoleDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }

    public boolean create(RoleRecord role) {
            int execute = dsl.insertInto(ROLE)
                    .set(role).execute();
            return execute > 0;
    }

    public Set<Role> findAllRoles() {
        return dsl.selectFrom(ROLE).fetchSet(TO_ROLE_RECORD);
    }

    private final static RecordMapper<Record, Role> TO_ROLE_RECORD = r -> {
        RoleRecord record = r.into(ROLE);
        return ImmutableRole.builder()
                .key(record.getKey())
                .name(record.getName())
                .description(record.getDescription())
                .isCustom(record.getIsCustom())
                .build();
    };
}
