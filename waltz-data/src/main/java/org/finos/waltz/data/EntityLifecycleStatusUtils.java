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

package org.finos.waltz.data;


import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.LifecycleStatus;
import org.finos.waltz.model.application.LifecyclePhase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EntityLifecycleStatusUtils {

    public static List<Boolean> convertToIsRemovedFlags(Set<EntityLifecycleStatus> entityLifecycleStatuses) {
        boolean includeRemoved = entityLifecycleStatuses.contains(EntityLifecycleStatus.REMOVED);
        boolean includedNonRemoved = !(entityLifecycleStatuses.contains(EntityLifecycleStatus.ACTIVE)
                || entityLifecycleStatuses.contains(EntityLifecycleStatus.PENDING));

        return ListUtilities.newArrayList(includedNonRemoved, includeRemoved);
    }


    public static List<LifecyclePhase> convertToLifecyclePhases(Collection<EntityLifecycleStatus> entityLifecycleStatuses) {

        ArrayList<LifecyclePhase> lifecyclePhases = new ArrayList<>();

        if (entityLifecycleStatuses.contains(EntityLifecycleStatus.ACTIVE)) {
            lifecyclePhases.add(LifecyclePhase.DEVELOPMENT);
            lifecyclePhases.add(LifecyclePhase.PRODUCTION);
        }

        if (entityLifecycleStatuses.contains(EntityLifecycleStatus.PENDING)) {
            lifecyclePhases.add(LifecyclePhase.CONCEPTUAL);
        }

        if (entityLifecycleStatuses.contains(EntityLifecycleStatus.REMOVED)) {
            lifecyclePhases.add(LifecyclePhase.RETIRED);
        }

        return lifecyclePhases;
    }

}
