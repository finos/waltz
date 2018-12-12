package com.khartec.waltz.jobs.clients.c1.sc1.parse;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.poi.ss.usermodel.Row;

import java.util.function.Function;

public class FlagToBoolean {

    private static final Aliases<Boolean> aliases = new Aliases<>();


    static {
        aliases.register(Boolean.TRUE,"x");
    }


    public static Boolean apply(String s) {
        return aliases.lookup(s).orElse(Boolean.FALSE);
    }
}
