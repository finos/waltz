package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.EntityReference;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class NameHelper {


    private static final AtomicInteger counter = new AtomicInteger();


    public static String mkUserId(String stem) {
        return mkName(stem);
    }


    public static String mkUserId() {
        return mkName("testuser");
    }


    public static String mkName(String stem) {
        return stem + "_" + randomUUID();
    }

    public static String mkName(String stem, String qualifier) {
        return mkName(stem + "_" + qualifier);
    }


    public static String toName(EntityReference ref) {
        return ref
                .name()
                .orElseThrow(() -> new IllegalStateException(format("Reference %s, has no name!", ref)));
    }


}
