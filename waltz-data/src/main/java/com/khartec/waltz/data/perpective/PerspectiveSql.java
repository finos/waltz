/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.perpective;

public interface PerspectiveSql {

    String SELECT = "SELECT code, name, description\n";

    String QUERY_FOR_BASE_PERSPECTIVE
            = SELECT
            + " FROM perspective\n "
            + " WHERE code = :code";

    String QUERY_FOR_MEASURABLES
            = SELECT
            + " FROM perspective_measurable\n "
            + " WHERE perspective_code = :code\n"
            + " ORDER BY name";


}
