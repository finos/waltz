/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

import com.khartec.waltz.model.user.ImmutableLoginRequest;
import com.khartec.waltz.model.user.ImmutableUserRegistrationRequest;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.user.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class UserHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        UserService userService = ctx.getBean(UserService.class);

//        registerUser(userService);

        ImmutableLoginRequest loginRequest = ImmutableLoginRequest.builder()
                .userName("dwatkins")
                .password("wrong")
                .build();

        boolean authenticated = userService.authenticate(loginRequest);
        System.out.println(authenticated);

        userService.findAllUsers().forEach(System.out::println);
    }


    private static void registerUser(UserService userService) {
        ImmutableUserRegistrationRequest request = ImmutableUserRegistrationRequest.builder()
                .userName("admin")
                .password("password")
                .build();

        userService.registerNewUser(request);
    }

}
