/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

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
