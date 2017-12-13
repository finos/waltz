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
import com.khartec.waltz.model.user.Role;

import static com.khartec.waltz.model.user.Role.*;

public class RoleUtilities {

    public static Role getRequiredRoleForEntityKind(EntityKind kind) {
        switch (kind) {
            case APPLICATION:
                return APP_EDITOR;
            case CHANGE_INITIATIVE:
                return CHANGE_INITIATIVE_EDITOR;
            case MEASURABLE:
                return CAPABILITY_EDITOR;
            case ORG_UNIT:
                return ORG_UNIT_EDITOR;
            default:
                return ADMIN;
        }
    }
}
