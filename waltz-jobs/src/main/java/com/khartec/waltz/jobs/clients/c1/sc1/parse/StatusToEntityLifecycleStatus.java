package com.khartec.waltz.jobs.clients.c1.sc1.parse;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.application.LifecyclePhase;

import java.util.function.Function;

public class StatusToEntityLifecycleStatus {

    private static final Aliases<EntityLifecycleStatus> aliases = new Aliases<>();


    static {
        aliases.register(EntityLifecycleStatus.ACTIVE,"aktiv");
        aliases.register(EntityLifecycleStatus.PENDING, "geplant");
        aliases.register(EntityLifecycleStatus.REMOVED, "abgeschaltet");
    }


    public static EntityLifecycleStatus apply(String s) {
        return aliases.lookup(s).orElse(EntityLifecycleStatus.ACTIVE);
    }
}
