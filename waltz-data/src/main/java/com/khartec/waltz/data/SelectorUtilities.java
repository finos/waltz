/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
