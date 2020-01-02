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

package com.khartec.waltz.data;

import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.ApplicationKind;
import org.jooq.Condition;

import java.util.Set;

import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.minus;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;

public class SelectorUtilities {

    public static <T extends IdSelectionOptions> void ensureScopeIsExact(T options) {
        checkTrue(
                options.scope() == HierarchyQueryScope.EXACT,
                "Only EXACT scope supported");
    }



    /***
     * creates a select condition taking into account application specific faces in options
     * @param options
     * @return
     */
    public static Condition mkApplicationConditions(IdSelectionOptions options) {
        Set<ApplicationKind> applicationKinds = minus(
                asSet(ApplicationKind.values()),
                options.filters().omitApplicationKinds());

        return APPLICATION.KIND.in(applicationKinds)
                .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses()));
    }

}
