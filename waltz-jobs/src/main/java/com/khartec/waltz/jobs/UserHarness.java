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

package com.khartec.waltz.jobs;

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
