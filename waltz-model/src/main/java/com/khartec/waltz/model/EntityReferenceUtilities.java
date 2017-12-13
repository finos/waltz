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

package com.khartec.waltz.model;

/**
 * Created by dwatkins on 06/06/2017.
 */
public class EntityReferenceUtilities {

    public static String pretty(EntityReference ref) {
        return String.format(
                "%s [%s/%d]",
                ref.name().orElse("?"),
                ref.kind().name(),
                ref.id());
    }


    public static String safeName(EntityReference ref) {
        String idStr = "[" + ref.id() + "]";
        return ref
                .name()
                .map(n -> n + " " + idStr)
                .orElse(idStr);
    }

}
