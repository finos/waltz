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

import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.application.ApplicationKind;
import org.finos.waltz.schema.tables.Application;
import org.jooq.Condition;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.minus;
import static org.finos.waltz.schema.tables.Application.APPLICATION;

public class SelectorUtilities {

    public static <T extends IdSelectionOptions> void ensureScopeIsExact(T options) {
        checkTrue(
                options.scope() == HierarchyQueryScope.EXACT,
                "Only EXACT scope supported");
    }



    /***
     * creates a select condition taking into account application specific faces in options
     * Defaults to an un-aliased application table
     * @param options
     * @return
     */
    public static Condition mkApplicationConditions(IdSelectionOptions options) {
        return mkApplicationConditions(APPLICATION, options);
    }

    /***
     * creates a select condition taking into account application specific faces in options
     * @param options
     * @return
     */
    public static Condition mkApplicationConditions(Application appTable, IdSelectionOptions options) {

        Condition cond = appTable.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        if(options.filters().omitApplicationKinds().isEmpty()){
            return cond;
        } else {
            Set<ApplicationKind> applicationKinds = minus(
                    asSet(ApplicationKind.values()),
                    options.filters().omitApplicationKinds());

            return cond.and(appTable.KIND.in(applicationKinds));
        }

    }

}
