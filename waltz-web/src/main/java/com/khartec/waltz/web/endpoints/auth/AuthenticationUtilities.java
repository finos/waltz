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
