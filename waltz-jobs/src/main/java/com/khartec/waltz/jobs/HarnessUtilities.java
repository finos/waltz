package com.khartec.waltz.jobs;

import java.util.function.Supplier;

public class HarnessUtilities {

    public static <T> T time(String name, Supplier<T> s) {
        System.out.println("-- begin [" + name + "]");

        long st = System.currentTimeMillis();

        try {
            T r = s.get();
            long end = System.currentTimeMillis();

            System.out.println("-- end [" + name + "]");
            System.out.println("-- dur [" + name + "]:" + (end - st));
            System.out.println("-- result [" + name + "]:" + r);
            System.out.println();

            return r;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

    }
}
