
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";

export function aggregatePeopleInvolvements(involvements, people) {
    const involvementsByPerson = _.chain(involvements)
        .groupBy('employeeId')
        .mapValues(xs => _.map(xs, 'kindId'))
        .value();

    return _.chain(people)
            .map(person => ({person, involvements: involvementsByPerson[person.employeeId]}))
            .uniqBy(i => i.person.id)
            .value();
}

