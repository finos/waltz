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

package com.khartec.waltz.data.user;

import com.khartec.waltz.model.user.ImmutableUser;
import com.khartec.waltz.model.user.User;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.schema.tables.User.USER;
import static com.khartec.waltz.schema.tables.UserRole.USER_ROLE;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;


@Repository
public class UserRoleDao {


    private static final Logger LOG = LoggerFactory.getLogger(UserRoleDao.class);

    private final DSLContext dsl;

    @Autowired
    public UserRoleDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public Set<String> getUserRoles(String userName) {
        List<String> roles = dsl.select(USER_ROLE.ROLE)
                .from(USER_ROLE)
                .where(USER_ROLE.USER_NAME.equalIgnoreCase(userName))
                .fetch(USER_ROLE.ROLE);

        return new HashSet<String>(roles);
    }


    public List<User> findAllUsers() {
        Result<Record2<String, String>> records = dsl.select(USER.USER_NAME, USER_ROLE.ROLE)
                .from(USER)
                .leftOuterJoin(USER_ROLE)
                .on(USER.USER_NAME.eq(USER_ROLE.USER_NAME))
                .fetch();

        Map<String, List<Record2<String, String>>> byUserName = records.stream()
                .collect(groupingBy(r -> r.getValue(USER.USER_NAME)));

        return byUserName.entrySet().stream()
                .map( entry -> ImmutableUser.builder()
                        .userName(entry.getKey())
                        .roles(entry.getValue()
                                .stream()
                                .map(record -> record.getValue(USER_ROLE.ROLE))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()))
                        .build())
                .collect(toList());
    }


    public boolean updateRoles(String userName, Set<String> newRoles) {
        try {
            dsl.transaction(config -> {
                LOG.info("Removing existing roles for: " + userName);
                DSL.using(config)
                        .delete(USER_ROLE)
                        .where(USER_ROLE.USER_NAME.eq(userName))
                        .execute();

                LOG.info("Inserting roles for " + userName + " / " + newRoles) ;
                DSLContext batcher = DSL.using(config);
                Set<Query> inserts = map(newRoles, r -> batcher
                        .insertInto(USER_ROLE, USER_ROLE.USER_NAME, USER_ROLE.ROLE)
                        .values(userName, r));

                batcher.batch(inserts)
                        .execute();
            });
            return true;
        } catch (Exception e) {
            return false;
        }

    }


    public int deleteUser(String userName) {
        return dsl.delete(USER_ROLE)
                .where(USER_ROLE.USER_NAME.eq(userName))
                .execute();
    }
}
