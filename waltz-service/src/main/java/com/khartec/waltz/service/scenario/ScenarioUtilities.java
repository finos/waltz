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

package com.khartec.waltz.service.scenario;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;

public class ScenarioUtilities {

    public static ChangeLog mkBasicLogEntry(long scenarioId, String message, String userId) {
        return ImmutableChangeLog.builder()
                .message(message)
                .parentReference(EntityReference.mkRef(EntityKind.SCENARIO, scenarioId))
                .operation(Operation.UPDATE)
                .userId(userId)
                .severity(Severity.INFORMATION)
                .build();
    }

}
