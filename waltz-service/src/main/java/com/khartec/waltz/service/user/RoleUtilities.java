/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

package com.khartec.waltz.service.user;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.user.Role;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.khartec.waltz.common.FunctionUtilities.alwaysBi;
import static com.khartec.waltz.model.user.Role.*;

public class RoleUtilities {

    private static final BiFunction<Operation, EntityKind, Role> REQUIRE_ADMIN = alwaysBi(ADMIN);
    private static final Map<EntityKind, BiFunction<Operation, EntityKind, Role>> REQUIRED_ROLES = new HashMap<>();


    static {
        REQUIRED_ROLES.put(EntityKind.APPLICATION, RoleUtilities::getRequiredRoleForApplication);
        REQUIRED_ROLES.put(EntityKind.CHANGE_INITIATIVE, RoleUtilities::getRequiredRoleForChangeInitiative);
        REQUIRED_ROLES.put(EntityKind.MEASURABLE, RoleUtilities::getRequiredRoleForMeasurable);
        REQUIRED_ROLES.put(EntityKind.ORG_UNIT, RoleUtilities::getRequiredRoleForOrgUnit);
    }


    /**
     * Shorthand for `getRequiredRoleForEntityKind(kind, null, null)`
     *
     * @param kind Primary entity kind
     * @return required role
     */
    public static Role getRequiredRoleForEntityKind(EntityKind kind) {
        return getRequiredRoleForEntityKind(kind, null, null);
    }


    /**
     * @param kind Primary entity kind involved in this request
     * @param op Operation to perform (ignored)
     * @param additionalKind Secondary entity kind involved in request
     * @return Role - required role for this
     */
    public static Role getRequiredRoleForEntityKind(EntityKind kind, Operation op, EntityKind additionalKind) {
        return REQUIRED_ROLES
                .getOrDefault(kind, REQUIRE_ADMIN)
                .apply(op, additionalKind);
    }


    // -- helpers

    private static Role getRequiredRoleForApplication(Operation op, EntityKind additionalKind) {
        return APP_EDITOR;
    }


    private static Role getRequiredRoleForChangeInitiative(Operation op, EntityKind additionalKind) {
        return CHANGE_INITIATIVE_EDITOR;
    }


    /*
     * If the additional kind is set we are more relaxed as the request is probably for something like
     * a relationship or a bookmark.  If it is not given it is a direct edit on the measurable and
     * is restricted to those with the `TAXONOMY_EDITOR` role.
     */
    private static Role getRequiredRoleForMeasurable(Operation op, EntityKind additionalKind) {
        return Optional
                .ofNullable(additionalKind)
                .map(k -> Role.CAPABILITY_EDITOR)
                .orElse(Role.TAXONOMY_EDITOR);
    }


    private static Role getRequiredRoleForOrgUnit(Operation op, EntityKind additionalKind) {
        return Role.ORG_UNIT_EDITOR;
    }

}
