
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import _ from "lodash";

export function aggregatePeopleInvolvements(involvements, people) {
    const involvementsByPerson = _.chain(involvements)
        .groupBy('employeeId')
        .mapValues(xs => _.map(xs, 'kind'))
        .value();

    return _.chain(people)
            .map(person => ({person, involvements: involvementsByPerson[person.employeeId]}))
            .uniqBy(i => i.person.id)
            .value();
}

