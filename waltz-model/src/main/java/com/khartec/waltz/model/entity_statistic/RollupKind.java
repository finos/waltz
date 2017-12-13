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

package com.khartec.waltz.model.entity_statistic;

/**
 * Created by dwatkins on 05/08/2016.
 */
public enum RollupKind {

    /**
     * This means group by outcome, then count distinct on entity references
     */
    COUNT_BY_ENTITY,

    /**
     * This means group by outcome, then sum the values in each group (entity references are discarded)
     */
    SUM_BY_VALUE,

    /**
     * This means group by outcome, then sum the values in each group (entity references are discarded)
     * then divide by the number of records in the group
     */
    AVG_BY_VALUE,

    /**
     * This means take the value as is from the database, no grouping or aggregation
     */
    NONE
}
