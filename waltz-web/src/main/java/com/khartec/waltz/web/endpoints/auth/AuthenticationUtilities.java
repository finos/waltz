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

package com.khartec.waltz.web.endpoints.auth;

import com.khartec.waltz.model.user.UserUtilities;
import spark.Request;

public class AuthenticationUtilities {

    public static final String USER_KEY = "waltz-user";

    public static void setUserAsAnonymous(Request request) {
        request.attribute(USER_KEY, UserUtilities.ANONYMOUS_USERNAME);
    }

    public static void setUser(Request request, String user) {
        request.attribute(USER_KEY, user);
    }

    public static String getUsername(Request request) {
        return (String) request.attribute(USER_KEY);
    }


    public static boolean isAnonymous(Request request) {
        return UserUtilities.ANONYMOUS_USERNAME.equalsIgnoreCase((String) request.attribute(USER_KEY));
    }

}
