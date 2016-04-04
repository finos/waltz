package com.khartec.waltz.model.user;

/**
 * Created by dwatkins on 03/04/2016.
 */
public class UserUtilities {

    public static final String ANONYMOUS_USERNAME = "anonymous";
    public static final User ANONYMOUS_USER = ImmutableUser.builder()
            .userName(ANONYMOUS_USERNAME)
            .build();


}
