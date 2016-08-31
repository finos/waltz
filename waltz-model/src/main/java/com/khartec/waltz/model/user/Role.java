/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.user;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum Role {
    ADMIN,
    APP_EDITOR,
    ANONYMOUS,
    AUTHORITATIVE_SOURCE_EDITOR,
    BOOKMARK_EDITOR,
    CAPABILITY_EDITOR,
    LOGICAL_DATA_FLOW_EDITOR,
    ORG_UNIT_EDITOR,
    RATING_EDITOR,
    BETA_TESTER;


    public static Set<String> allNames() {
        return Stream
                .of(Role.values())
                .map(r -> r.name())
                .collect(Collectors.toSet());
    }
}
