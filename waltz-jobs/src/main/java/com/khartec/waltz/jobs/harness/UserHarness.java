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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.model.user.ImmutableUserRegistrationRequest;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.service.user.UserService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.tables.User.USER;


public class UserHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        UserRoleService userRoleService = ctx.getBean(UserRoleService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        int rc = dsl.insertInto(USER)
                .set(USER.USER_NAME, "kamran")
                .set(USER.PASSWORD, "1234")
                .onDuplicateKeyIgnore()
                .execute();

        System.out.println(rc);
//
//        ImmutableLoginRequest loginRequest = ImmutableLoginRequest.builder()
//                .userName("dwatkins")
//                .password("wrong")
//                .build();
//
//        boolean authenticated = userService.authenticate(loginRequest);
//        System.out.println(authenticated);
//
//        userRoleService.findAllUsers().forEach(System.out::println);
    }


    private static void registerUser(UserService userService) {
        ImmutableUserRegistrationRequest request = ImmutableUserRegistrationRequest.builder()
                .userName("silly")
                .password("password")
                .build();

        userService.registerNewUser(request);
    }

}
