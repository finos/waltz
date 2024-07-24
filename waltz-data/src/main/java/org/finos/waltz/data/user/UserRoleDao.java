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

package org.finos.waltz.data.user;

import org.finos.waltz.model.DiffResult;
import org.finos.waltz.model.user.ImmutableUser;
import org.finos.waltz.model.user.User;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.model.DiffResult.mkDiff;
import static org.finos.waltz.schema.tables.Role.ROLE;
import static org.finos.waltz.schema.tables.User.USER;
import static org.finos.waltz.schema.tables.UserRole.USER_ROLE;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Repository
public class UserRoleDao {

    private final DSLContext dsl;

    @Autowired
    public UserRoleDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public Set<String> getUserRoles(String userName) {
        return dsl
                .select(USER_ROLE.ROLE)
                .from(USER_ROLE)
                .where(USER_ROLE.USER_NAME.equalIgnoreCase(userName))
                .fetchSet(USER_ROLE.ROLE);
    }


    public Set<User> findAllUsers() {
        return findUsersByCondition(DSL.trueCondition());
    }


    public Set<User> findUsersForRole(Long roleId) {
        return findUsersByCondition(ROLE.ID.eq(roleId));
    }


    public int updateRoles(String userName, Set<String> newRoles) {
        Set<Tuple2<String, String>> updatesAsTuples = map(
                newRoles,
                r -> tuple(userName, r));
        return replaceRoles(updatesAsTuples);
    }
    

    public int addRoles(Set<Tuple2<String, String>> usersAndRolesToUpdate) {
        return addRoles(dsl, usersAndRolesToUpdate);
    }

    public int removeRoles(Set<Tuple2<String, String>> usersAndRolesToUpdate) {
        return removeRoles(dsl, usersAndRolesToUpdate);
    }


    public int replaceRoles(Set<Tuple2<String, String>> usersAndRolesToUpdate) {
        Set<String> distinctUsers = map(usersAndRolesToUpdate, t -> t.v1);
        return dsl.transactionResult(ctx -> {
            DSLContext tx = ctx.dsl();
            int rmRcs = tx.deleteFrom(USER_ROLE)
                    .where(USER_ROLE.USER_NAME.in(distinctUsers))
                    .execute();

             return addRoles(tx, usersAndRolesToUpdate);
        });
    }


    private int addRoles(DSLContext tx,
                         Set<Tuple2<String, String>> usersAndRolesToUpdate) {
        Set<Tuple2<String, String>> existing = tx
                .select(USER_ROLE.USER_NAME, USER_ROLE.ROLE)
                .from(USER_ROLE)
                .where(USER_ROLE.USER_NAME.in(map(usersAndRolesToUpdate, t -> t.v1)))
                .fetchSet(r -> tuple(r.get(USER_ROLE.USER_NAME), r.get(USER_ROLE.ROLE)));

        DiffResult<Tuple2<String, String>> diff = mkDiff(existing, usersAndRolesToUpdate);

        int[] rc = diff
                .otherOnly()
                .stream()
                .map(t -> tx
                        .insertInto(
                                USER_ROLE,
                                USER_ROLE.USER_NAME,
                                USER_ROLE.ROLE)
                        .values(t.v1(), t.v2()))
                .collect(collectingAndThen(
                        toList(),
                        tx::batch))
                .execute();

        return summarizeResults(rc);
    }


    private int removeRoles(DSLContext tx,
                            Set<Tuple2<String, String>> usersAndRolesToUpdate) {
        int[] rc = usersAndRolesToUpdate
                .stream()
                .map(t -> tx
                        .deleteFrom(USER_ROLE)
                        .where(USER_ROLE.USER_NAME.eq(t.v1()))
                        .and(USER_ROLE.ROLE.eq(t.v2())))
                .collect(collectingAndThen(
                        toList(),
                        tx::batch))
                .execute();

        return summarizeResults(rc);
    }


    private Set<User> findUsersByCondition(Condition condition) {
        Result<Record2<String, String>> records = dsl
                .select(USER.USER_NAME, USER_ROLE.ROLE)
                .from(USER)
                .innerJoin(USER_ROLE)
                .on(USER.USER_NAME.eq(USER_ROLE.USER_NAME))
                .innerJoin(ROLE).on(ROLE.KEY.eq(USER_ROLE.ROLE))
                .where(condition)
                .fetch();

        Map<String, Collection<Record2<String, String>>> byUserName = groupBy(
                records,
                r -> r.getValue(USER.USER_NAME));

        return byUserName
                .entrySet()
                .stream()
                .map( entry -> ImmutableUser.builder()
                        .userName(entry.getKey())
                        .roles(entry.getValue()
                                .stream()
                                .map(record -> record.getValue(USER_ROLE.ROLE))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()))
                        .build())
                .collect(toSet());
    }

}
