package com.khartec.waltz.jobs.clients.c1.sc1.parse;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.model.application.LifecyclePhase;

public class StatusToLifecyclePhase {

    private static final Aliases<LifecyclePhase> aliases = new Aliases<>();


    static {
        aliases.register(LifecyclePhase.PRODUCTION,"aktiv");
        aliases.register(LifecyclePhase.DEVELOPMENT, "geplant");
        aliases.register(LifecyclePhase.RETIRED, "abgeschaltet", "beendet");
    }


    public static LifecyclePhase apply(String s) {
        return aliases.lookup(s).orElse(LifecyclePhase.PRODUCTION);
    }
}
