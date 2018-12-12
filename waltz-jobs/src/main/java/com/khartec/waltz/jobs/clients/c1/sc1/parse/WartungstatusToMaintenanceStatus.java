package com.khartec.waltz.jobs.clients.c1.sc1.parse;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.jobs.clients.c1.sc1.model.MaintenanceStatus;

public class WartungstatusToMaintenanceStatus {

    private static final Aliases<MaintenanceStatus> aliases = new Aliases<>();


    static {
        aliases.register(MaintenanceStatus.EXTENDED,"2-erweiterte wartung");
        aliases.register(MaintenanceStatus.NONE, "3-ohne wartung");
        aliases.register(MaintenanceStatus.SELF, "0-hauptwartung");
    }


    public static MaintenanceStatus apply(String s) {
        return aliases.lookup(s).orElse(MaintenanceStatus.SELF);
    }
}
