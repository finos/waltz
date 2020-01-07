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
