package org.finos.waltz.test_common_again.helpers;

import java.util.concurrent.atomic.AtomicInteger;

public class NameHelper {

    private static final AtomicInteger counter = new AtomicInteger();


    public static String mkUserId(String stem) {
        return mkName(stem);
    }


    public static String mkUserId() {
        return mkName("testuser");
    }


    public static String mkName(String stem) {
        return stem + "_" + counter.incrementAndGet();
    }

    public static String mkName(String stem, String qualifier) {
        return mkName(stem + "_" + qualifier);
    }

}
