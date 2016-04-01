package com.khartec.waltz.web.endpoints.auth;

import spark.Request;

public class AuthenticationUtilities {

    public static final String USER_KEY = "waltz-user";
    public static final String ANONYMOUS_USER = "anonymous";

    public static void setUserAsAnonymous(Request request) {
        request.attribute(USER_KEY, AuthenticationUtilities.ANONYMOUS_USER);
    }

    public static void setUser(Request request, String user) {
        request.attribute(USER_KEY, user);
    }

    public static String getUsername(Request request) {
        return (String) request.attribute(USER_KEY);
    }
}
