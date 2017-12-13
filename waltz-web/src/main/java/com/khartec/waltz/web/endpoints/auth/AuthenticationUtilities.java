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
