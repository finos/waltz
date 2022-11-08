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

package org.finos.waltz.model;

import org.finos.waltz.model.user.SystemRole;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.finos.waltz.common.FunctionUtilities.alwaysBi;
import static org.finos.waltz.model.user.SystemRole.*;

public class RoleUtilities {

    private static final BiFunction<Operation, EntityKind, SystemRole> REQUIRE_ADMIN = alwaysBi(ADMIN);
    private static final Map<EntityKind, BiFunction<Operation, EntityKind, SystemRole>> REQUIRED_ROLES = new HashMap<>();


    static {
        REQUIRED_ROLES.put(EntityKind.APPLICATION, RoleUtilities::getRequiredRoleForApplication);
        REQUIRED_ROLES.put(EntityKind.CHANGE_INITIATIVE, RoleUtilities::getRequiredRoleForChangeInitiative);
        REQUIRED_ROLES.put(EntityKind.MEASURABLE, RoleUtilities::getRequiredRoleForMeasurable);
        REQUIRED_ROLES.put(EntityKind.ORG_UNIT, RoleUtilities::getRequiredRoleForOrgUnit);
        REQUIRED_ROLES.put(EntityKind.MEASURABLE_CATEGORY, RoleUtilities::getRequiredRoleForMeasurableCategory);
    }


    /**
     * Shorthand for `getRequiredRoleForEntityKind(kind, null, null)`
     *
     * @param kind Primary entity kind
     * @return required role
     */
    public static SystemRole getRequiredRoleForEntityKind(EntityKind kind) {
        return getRequiredRoleForEntityKind(kind, null, null);
    }


    /**
     * Note: this method should correspond with the js file:
     * <code>role-utils.js:getEditRoleForEntityKind</code>
     *
     * @param kind Primary entity kind involved in this request
     * @param op Operation to perform (ignored)
     * @param additionalKind Secondary entity kind involved in request
     * @return SystemRole - required role for this
     */
    public static SystemRole getRequiredRoleForEntityKind(EntityKind kind, Operation op, EntityKind additionalKind) {
        return REQUIRED_ROLES
                .getOrDefault(kind, REQUIRE_ADMIN)
                .apply(op, additionalKind);
    }


    // -- helpers

    private static SystemRole getRequiredRoleForApplication(Operation op, EntityKind additionalKind) {
        return APP_EDITOR;
    }


    private static SystemRole getRequiredRoleForChangeInitiative(Operation op, EntityKind additionalKind) {
        return CHANGE_INITIATIVE_EDITOR;
    }


    /*
     * If the additional kind is set we are more relaxed as the request is probably for something like
     * a relationship or a bookmark.  If it is not given it is a direct edit on the measurable and
     * is restricted to those with the `TAXONOMY_EDITOR` role.
     */
    private static SystemRole getRequiredRoleForMeasurable(Operation op, EntityKind additionalKind) {
        return Optional
                .ofNullable(additionalKind)
                .map(k -> SystemRole.CAPABILITY_EDITOR)
                .orElse(SystemRole.TAXONOMY_EDITOR);
    }


    private static SystemRole getRequiredRoleForMeasurableCategory(Operation op, EntityKind additionalKind) {
        return Optional
                .ofNullable(additionalKind)
                .map(k -> SystemRole.CAPABILITY_EDITOR)
                .orElse(SystemRole.TAXONOMY_EDITOR);
    }


    private static SystemRole getRequiredRoleForOrgUnit(Operation op, EntityKind additionalKind) {
        return SystemRole.ORG_UNIT_EDITOR;
    }

}
