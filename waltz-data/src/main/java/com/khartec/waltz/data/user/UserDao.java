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

package com.khartec.waltz.data.user;

import com.khartec.waltz.model.user.ImmutableUser;
import com.khartec.waltz.model.user.User;
import com.khartec.waltz.schema.tables.records.UserRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.User.USER;


@Repository
public class UserDao {


    private final DSLContext dsl;

    private final RecordMapper<Record, User> userMapper = r -> {
        UserRecord record = r.into(USER);
        return ImmutableUser.builder().userName(record.getUserName()).build();
    };


    @Autowired
    public UserDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public String getPassword(String userName) {
        Record1<String> possiblePassword = dsl.select(USER.PASSWORD)
                .from(USER)
                .where(USER.USER_NAME.equalIgnoreCase(userName))
                .fetchOne();

        if (possiblePassword != null) {
            return possiblePassword.value1();
        } else {
            return null;
        }
    }


    public int create(String userName, String passwordHash) {
        return dsl.insertInto(USER)
                .set(USER.USER_NAME, userName)
                .set(USER.PASSWORD, passwordHash)
                .onDuplicateKeyIgnore()
                .execute();
    }


    public List<String> findAllUserNames() {
        return dsl.select(USER.USER_NAME)
                .from(USER)
                .fetch(USER.USER_NAME);

    }


    public int deleteUser(String userName) {
        return dsl.delete(USER)
                .where(USER.USER_NAME.equalIgnoreCase(userName))
                .execute();
    }


    public int resetPassword(String userName, String passwordHash) {
        return dsl.update(USER)
                .set(USER.PASSWORD, passwordHash)
                .where(USER.USER_NAME.eq(userName))
                .execute();
    }
}
